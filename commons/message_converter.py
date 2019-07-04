from abc import ABC, abstractmethod
import logging
from commons.schema import SchemaRetriever

logger = logging.getLogger('root')
converter_cache = dict()


class MessageConverter(ABC):

    def __init__(self, schema_retriever, validate_only):
        super().__init__()

    @abstractmethod
    def convert(self, msg, schema_name, use_cached_schema=True):
        pass

    @abstractmethod
    def convert_all(self, msgs, schema_name, use_cached_schema=True):
        pass

    @abstractmethod
    def validate(self, msg, schema_name, use_cached_schema=True):
        pass

    @abstractmethod
    def validate_all(self, schema_name, msgs, use_cached_schema=True):
        pass


class AvroConverter(MessageConverter):

    def __init__(self, schema_retriever: SchemaRetriever, validate_only=True):
        self.schema_retriever = schema_retriever
        self.validate_only = validate_only
        super().__init__(schema_retriever, validate_only)

    def convert(self, msg, schema_name, use_cached_schema=True):
        logger.debug(f'Converting {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate_only:
            # If validate only is true don't do any conversion and return the message.
            return msg
        else:
            # TDOD Do the conversion.
            return msg

    def convert_all(self, msgs, schema_name, use_cached_schema=True):
        logger.debug(f'Converting {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate_only:
            # If validate only is true don't do any conversion and return the message.
            return msgs
        else:
            # TDOD Do the conversion.
            return msgs

    def validate(self, msg, schema_name, use_cached_schema=True):
        logger.debug(f'Validating {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        return True

    def validate_all(self, msgs, schema_name, use_cached_schema=True):
        logger.debug(f'Validating {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        # TODO use fastavro for validation of multiple msgs
        return True
