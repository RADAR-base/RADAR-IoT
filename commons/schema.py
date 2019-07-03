from abc import ABC, abstractmethod
import logging

logger = logging.getLogger('root')


class SchemaRetriever(ABC):
    def __init__(self):
        super().__init__()


class AvroSchemaRetriever(SchemaRetriever):

    # TODO use ExpiringDict
    cached_schemas = dict()

    def __init__(self, **kwargs):
        # call get schema for each config sensor
        # add the schema to the dict
        super().__init__()

    @abstractmethod
    def get_schema(self, topic=None, sensor_name=None, **kwargs):
        pass


class FileAvroSchemaRetriever(AvroSchemaRetriever):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    def get_schema(self, topic=None, sensor_name=None, **kwargs):
        pass


class SchemaRegistrySchemaRetriever(AvroSchemaRetriever):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    def get_schema(self, topic=None, sensor_name=None, **kwargs):
        pass


class UrlAvroSchemaRetriever(AvroSchemaRetriever):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    def get_schema(self, topic=None, sensor_name=None, **kwargs):
        pass


class SchemaNamingStrategy(ABC):
    def __init__(self):
        super().__init__()

    @abstractmethod
    def get_schema_name(self, **kwargs):
        pass


class SensorBasedSchemaNamingStrategy(SchemaNamingStrategy):
    def __init__(self, prefix='', suffix=''):
        self.prefix = prefix
        self.suffix = suffix
        super().__init__()

    def get_schema_name(self, **kwargs):
        if 'name' in kwargs:
            return f'{self.prefix}{kwargs["name"]}{self.suffix}'

        if 'topic' in kwargs:
            return f'{self.prefix}{kwargs["topic"]}{self.suffix}'
