import json
import logging
from enum import IntEnum
from typing import List

from commons.message_converter import MessageConverter
from commons.schema import SchemaNamingStrategy

logger = logging.getLogger('root')


class ErrorCode(IntEnum):
    UNKNOWN = 0,
    STATUS_OFF = 1


class IoTError(RuntimeError):
    """Object representing an error. This must be used across the system for reporting any errors."""

    def __init__(self, type: str, code: ErrorCode, reason: str, trace: str):
        self.type = type
        self.code = code
        self.reason = reason
        self.trace = trace
        super().__init__(self.__dict__)


class Response:
    """Object representing a Response. A response consists of a response dictionary and a list of errors, if any.
    This must be used for any responses for example, when returning a measurement from the sensors."""

    def __init__(self, response: [dict, None], errors: [List[IoTError], None]):
        self.response = response
        self.errors = errors


class DataProcessor:
    from pubsub import Publisher

    def __init__(self, converter: MessageConverter, publisher: Publisher, schema_naming_strategy: SchemaNamingStrategy,
                 error_handler=None):
        """Supply a

        :param converter: to convert messages read from sensors to a format suitable for the publisher.
        :param publisher: for publishing the messages to a pub/sub system.
        :param schema_naming_strategy: Naming strategy for getting the schemas using topic or sensor name.
        :param error_handler: A function that accepts a :class: list of :class: Error as input for handling.
                              Should additionally have variable positional(*args) and named arguments(**kwargs).


        You can extend this class to add any functionality but it is recommended to use Decorator pattern if
        only adding functionality on top of this.
        """

        self.error_handler = error_handler
        if publisher is None or converter is None:
            raise AttributeError("Converter or Publisher cannot be none.")
        self.converter = converter
        self.publisher = publisher
        self.naming_strategy = schema_naming_strategy

    def process_data(self, msgs: List[Response], *args, **kwargs):
        """
        The name of the thing and the topic needs to be provided either as named args (:param kwargs) or
        as positional args(:param args). The arg[0] and arg[1] should be topic and name respectively.
        This impl may be extended or changed in another data processor.
        """
        name = kwargs.get('name')
        topic = kwargs.get('topic')

        if name is None or topic is None:
            try:
                topic = args[0]
                name = args[1]
            except:
                raise AttributeError('Cannot process data without name and topic being provided.')

        logger.debug(f'Trying publishing {len(msgs)} messages using {self.__class__.__name__}')
        msgs_ = [msg.response for msg in msgs if isinstance(msg, Response) and msg.response is not None]

        if msgs_ is not None and len(msgs_) > 0:
            schema_name = self.naming_strategy.get_schema_name(name=name, topic=topic)
            if self.converter is not None:
                msgs_converted = self.converter.convert_all(msgs_, schema_name)
                logger.debug(f'Converted message is {msgs_converted} using {self.converter.__class__.__name__}')
            else:
                # if converter is none, throw exception.
                raise ConverterNotFoundError(
                    f'The converter cannot be `None` for publisher: {self.__class__.__name__}.'
                    f' Please specify one in the configuration.')

            self.publisher.publish(msgs_converted, topic)

        if self.error_handler is not None:
            self.error_handler([x.errors for x in msgs if x is not None and x.errors is not None], topic=name)


class ConverterNotFoundError(IOError):
    """Raised when the Converter is None or the one defined in configuration is not found"""
    pass


class ErrorHandler:
    from pubsub import Publisher

    def __init__(self, publisher: Publisher, channel_prefix='errors/sensors'):
        self.publisher = publisher
        self.channel_prefix = channel_prefix

    def handle_error(self, errors: List[List[IoTError]], *args, **kwargs):
        topic_name = f'{self.channel_prefix}/{kwargs.get("topic", "unknown")}'
        # Flatten list of list of Error and convert to dict for easy json serialisation.
        errors = [err.__dict__ for error in errors for err in error]
        self.publisher.publish(json.dumps(errors), topic_name) if len(errors) > 0 else logger.debug('Empty error list')
