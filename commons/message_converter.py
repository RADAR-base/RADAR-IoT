import logging
from abc import ABC, abstractmethod

from fastavro import validation

from commons.schema import SchemaRetriever

logger = logging.getLogger('root')


class MessageConverter(ABC):

    def __init__(self, schema_retriever, validate_only):
        super().__init__()

    @abstractmethod
    def convert(self, msg, schema_name):
        pass

    @abstractmethod
    def convert_all(self, msgs, schema_name):
        pass

    @abstractmethod
    def validate(self, msg, schema_name):
        pass

    @abstractmethod
    def validate_all(self, schema_name, msgs):
        pass


class AvroConverter(MessageConverter):

    def __init__(self, schema_retriever: SchemaRetriever, validate_only=True):
        self.schema_retriever = schema_retriever
        self.validate_only = validate_only
        super().__init__(schema_retriever, validate_only)

    def convert(self, msg, schema_name):
        logger.debug(f'Converting {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate_only:
            # If validate only is true don't do any conversion and return the message.
            return msg
        else:
            # TDOD Do the conversion.
            return msg

    def convert_all(self, msgs, schema_name):
        logger.debug(f'Converting {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate_only:
            # If validate only is true don't do any conversion and return the message.
            return msgs
        else:
            # TDOD Do the conversion.
            return msgs

    def validate(self, msg, schema_name):
        logger.debug(f'Validating {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        return validation.validate(msg, self.schema_retriever.get_schema(sensor_name=schema_name), raise_errors=False)

    def validate_all(self, msgs, schema_name):
        logger.debug(f'Validating {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        schema = self.schema_retriever.get_schema(schema_name=schema_name)
        return validation.validate_many(msgs, schema, raise_errors=False)
