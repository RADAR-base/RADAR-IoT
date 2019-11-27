import logging
from concurrent.futures import ThreadPoolExecutor
from typing import List

import redis

from commons.data import Response
from commons.message_converter import MessageConverter
from config import ConfigHelper
from pubsub import Publisher
from pubsub.redis_connection import RedisConnection

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        self.converter: MessageConverter = ConfigHelper.get_converter()
        super().__init__(connection, publisher_thread_pool)

    def _publish(self, msgs: List[Response], topic: str, schema_name=None) -> None:
        # TODO take appropriate action if there are any errors. In first instance report it to the pub/sub channel
        logger.debug(f'Trying publishing {len(msgs)} messages using {self.__class__.__name__}')
        msgs_ = list()
        for msg in msgs:
            if isinstance(msg, Response):
                msgs_.append(msg.response)
            else:
                raise TypeError(f'Cannot publish the messages as they must be of {Response.__class__.__name__} type.')

        if self.converter is not None:
            msgs_converted = self.converter.convert_all(msgs_, schema_name)
            logger.debug(f'Converted message is {msgs_} using {self.converter.__class__.__name__}')
        else:
            # if converter is none, throw exception.
            raise ConverterNotFoundError(
                f'The converter cannot be `None` for publisher: {self.__class__.__name__}.'
                f' Please specify one in the configuration.')

        try:
            # publish messages
            if msgs_converted is not None:
                num_subscribers = self.redis_client.publish(topic, msgs_converted)
                logger.info(f'Published {len(msgs_)} messages using publisher {self.__class__.__name__}'
                            f'. It was delivered to {num_subscribers} subscribers')
            else:
                logger.warning(f'{len(msgs_)} Messages could not be published due to errors.')
        except redis.PubSubError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}')
            # TODO: Cache the data instead of discarding
            pass
        except redis.exceptions.DataError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}. Please perform necessary '
                           f'conversions first.')
            pass


class ConverterNotFoundError(IOError):
    """Raised when the Converter is None or the one defined in configuration is not found"""
    pass
