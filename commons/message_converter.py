import json
import logging
from abc import ABC, abstractmethod

from fastavro import validation

from commons.schema import SchemaRetriever

logger = logging.getLogger('root')


class MessageConverter(ABC):

    def __init__(self):
        super().__init__()

    @abstractmethod
    def convert(self, msg, schema_name):
        pass

    @abstractmethod
    def convert_all(self, msgs, schema_name):
        pass


class AvroConverter(MessageConverter):

    def __init__(self, schema_retriever: SchemaRetriever):
        self.schema_retriever = schema_retriever
        super().__init__()

    def convert(self, msg, schema_name):
        logger.debug(f'Converting {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        # TDOD Do the conversion.
        return msg

    def convert_all(self, msgs, schema_name):
        logger.debug(f'Converting {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        # TDOD Do the conversion.
        return msgs


class AvroValidatedJsonConverter(MessageConverter):

    def __init__(self, schema_retriever: SchemaRetriever):
        self.schema_retriever = schema_retriever
        super().__init__()

    def convert(self, msg, schema_name):
        logger.debug(f'Converting {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate(msg, schema_name):
            # For consistency we write a single message also as a list
            return json.dumps([msg])
        else:
            logger.warning(
                f'Validation failed for messages {msg}. Please look for any errors. Not publishing...')
            return None

    def convert_all(self, msgs, schema_name):
        logger.debug(f'Converting {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        if self.validate_all(msgs, schema_name):
            return json.dumps(msgs)
        else:
            logger.warning(
                f'Validation failed for messages {msgs}. Please look for any errors. Not publishing...')
            return None

    def validate(self, msg, schema_name):
        logger.debug(f'Validating {msg} using the class {self.__class__.__name__} and schema {schema_name}')
        schema = self.schema_retriever.get_schema(schema_name=schema_name)
        if schema is None:
            return False
        else:
            return validation.validate(msg, schema, raise_errors=True)

    def validate_all(self, msgs, schema_name):
        logger.debug(f'Validating {msgs} using the class {self.__class__.__name__} and schema {schema_name}')
        schema = self.schema_retriever.get_schema(schema_name=schema_name)
        if schema is None:
            return False
        else:
            return validation.validate_many(msgs, schema, raise_errors=True)
