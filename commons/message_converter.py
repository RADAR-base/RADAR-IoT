from abc import ABC, abstractmethod
import logging
from commons.schema import SchemaRetriever

logger = logging.getLogger('root')
converter_cache = dict()


class MessageConverter(ABC):

    def __init__(self, schema_retriever):
        super().__init__()

    @abstractmethod
    def convert(self, msg, schema_name, use_cached_schema=True):
        pass

    @abstractmethod
    def validate(self, msg, schema_name, use_cached_schema=True):
        pass

    @abstractmethod
    def validate_all(self, schema_name, msgs, use_cached_schema=True):
        pass


class AvroConverter(MessageConverter):

    def __init__(self, schema_retriever: SchemaRetriever):
        self.schema_retriever = schema_retriever
        super().__init__(schema_retriever)

    def convert(self, msg, schema_name, use_cached_schema=True):
        logger.debug(f'Converting {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        return msg

    def validate(self, msg, schema_name, use_cached_schema=True):
        return True

    def validate_all(self, msgs, schema_name, use_cached_schema=True):
        # use fastavro for validation
        return True
