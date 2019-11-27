import logging
from abc import ABC, abstractmethod
from json import load, loads

from expiringdict import ExpiringDict
from fastavro import parse_schema

logger = logging.getLogger('root')


class SchemaRetriever(ABC):
    def __init__(self):
        super().__init__()

    @abstractmethod
    def get_schema(self, schema_name=None, **kwargs):
        pass


class AvroSchemaRetriever(SchemaRetriever):

    def __init__(self, **kwargs):
        self.cached_schemas = self.get_all_schemas()
        from config import ConfigHelper
        self.schema_naming_strategy = ConfigHelper.get_default_naming_strategy()
        logger.debug(f'Using the schemas: {self.cached_schemas.items()}')
        super().__init__()

    @abstractmethod
    def get_all_schemas(self, **kwargs) -> ExpiringDict:
        pass

    def get_schema(self, schema_name=None, **kwargs):
        if schema_name is not None and schema_name in self.cached_schemas.keys():
            if self.cached_schemas.ttl(schema_name) is None:
                # key has expired, fetch the schemas again
                logger.debug(f'Schema expired for {schema_name}. Updating all schemas...')
                self.cached_schemas = self.get_all_schemas()

            logger.debug(f'Found schema for {schema_name} : {self.cached_schemas.get(schema_name)}')
            return self.cached_schemas.get(schema_name, None)
        logger.warning(f'Schema not found for {schema_name}')
        return None


class FileAvroSchemaRetriever(AvroSchemaRetriever):
    def __init__(self, **kwargs):
        self.filepath = kwargs.get('filepath')
        self.extension = kwargs.get('extension')
        super().__init__()

    def get_all_schemas(self, **kwargs) -> ExpiringDict:
        import os
        import re
        logger.info('Fetching all schemas...')
        filepaths = list()
        re_exp = re.compile(f'.*.{self.extension}')
        for (dirpath, dirnames, filenames) in os.walk(self.filepath):
            logger.debug(f'Dirpath: {dirpath}, Dirnames: {dirnames}, Filenames: {filenames}')
            filenames = filter(re_exp.match, filenames)
            for filename in filenames:
                filepaths.append(os.path.join(dirpath, filename))

        logger.debug(f'Found following {self.extension} file paths : {filepaths}')
        schemas = ExpiringDict(max_len=100, max_age_seconds=86400)
        for file in filepaths:
            with open(file, 'r') as schema:
                schemas[os.path.basename(file).split(f"{self.extension}")[0]] = parse_schema(load(schema))
        return schemas


class SchemaRegistrySchemaRetriever(AvroSchemaRetriever):
    def __init__(self, **kwargs):
        try:
            self.schema_registry_url = kwargs.get('schema_registry_url')
        except KeyError:
            raise AttributeError('schema_registry_url needed to use the this schema retriever. Please configure it.')
        super().__init__()

    def get_all_schemas(self, **kwargs) -> ExpiringDict:
        schemas = ExpiringDict(max_len=200, max_age_seconds=86400)
        logger.info(f'Loading schemas from Schema registry at {self.schema_registry_url}')
        import requests as req
        schema_names = req.get(f'{self.schema_registry_url}/subjects').json()
        for schema in schema_names:
            if '-key' not in schema and 'org.radarcns.stream' not in schema:
                schemas[schema] = parse_schema(
                    loads(req.get(f'{self.schema_registry_url}/subjects/{schema}/versions/latest').json()['schema']))

        return schemas

    def get_schema(self, schema_name=None, **kwargs):
        import requests as req
        try:
            return parse_schema(
                loads(req.get(f'{self.schema_registry_url}/subjects/{schema_name}/versions/latest').json()['schema']))
        except:
            # If cannot get directly from schema registry, try getting it from the cache. The cache is updated daily.
            return super().get_schema(schema_name)


class GithubAvroSchemaRetriever(AvroSchemaRetriever):

    def __init__(self, **kwargs):
        from github3 import login, GitHub
        self.repo_owner = kwargs.get('repo_owner')
        self.repo_name = kwargs.get('repo_name')
        self.branch = kwargs.get('branch')
        self.base_path = kwargs.get('basepath')
        self.extension = kwargs.get('extension')

        if 'git_user' in kwargs and 'git_password' in kwargs:
            self.git_user = kwargs.get('git_user')
            self.git_password = kwargs.get('git_password')
            self.git = login(self.git_user, self.git_password)
        else:
            self.git_user = None
            self.git_password = None
            self.git = GitHub()

        super().__init__()

    def get_all_schemas(self, **kwargs) -> ExpiringDict:
        repo = self.git.repository(self.repo_owner, self.repo_name)
        contents = repo.directory_contents(self.base_path, self.branch, return_as=dict)

        schemas = ExpiringDict(max_len=100, max_age_seconds=86400)
        schemas_final = self.get_schema_content(repo, contents, schemas)
        return schemas_final

    def get_schema_content(self, repo, contents, schema_dict):
        for key, content in contents.items():
            if content.type == 'file' and self.extension in key:
                # read schema
                schema_dict[key.split(f"{self.extension}")[0]] = parse_schema(loads(
                    repo.file_contents(content.path, self.branch).decoded))
            elif content.type == 'dir':
                self.get_schema_content(repo, repo.directory_contents(content.path, self.branch, return_as=dict),
                                        schema_dict)
        return schema_dict


class SchemaNamingStrategy(ABC):
    def __init__(self):
        super().__init__()

    @abstractmethod
    def get_schema_name(self, **kwargs):
        pass


class SchemaRegistryBasedSchemaNamingStrategy(SchemaNamingStrategy):
    def __init__(self):
        super().__init__()

    def get_schema_name(self, **kwargs):
        if 'topic' in kwargs.keys():
            return f'{kwargs.get("topic")}-value'
        if 'name' in kwargs.keys():
            return f'{kwargs.get("name")}-value'


class SensorBasedSchemaNamingStrategy(SchemaNamingStrategy):
    def __init__(self, prefix='', suffix=''):
        self.prefix = prefix
        self.suffix = suffix
        super().__init__()

    def get_schema_name(self, **kwargs):
        if 'name' in kwargs.keys():
            return f'{self.prefix}{kwargs.get("name")}{self.suffix}'

        if 'topic' in kwargs.keys():
            return f'{self.prefix}{kwargs.get("topic")}{self.suffix}'
