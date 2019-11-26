import logging
from concurrent.futures import ThreadPoolExecutor
from os import environ
from typing import List

import anyconfig

from commons.dynamic_import import DynamicImporter
from commons.message_converter import MessageConverter
from commons.schema import SchemaNamingStrategy, SchemaRegistryBasedSchemaNamingStrategy
from pubsub.connection import Connection
from pubsub.publisher import Publisher
from sensors.sensor import Sensor

MODULE_KEY = 'module'
CLASS_KEY = 'class'
TOPIC_KEY = 'publishing_topic'
POLL_FREQUENCY_KEY = 'poll_frequency_ms'
FLUSH_SIZE_KEY = 'flush_size'
FLUSH_AFTER_S_KEY = 'flush_after_s'
PUBLISHER_KEY = 'publisher'
ROOT_LOGGER_LEVEL_KEY = 'root_logger_level'
CONVERTER_KEY = 'converter'
SCHEDULER_MAX_THREADS_KEY = 'scheduler_max_threads'
EXPOSE_TOPIC_ENDPOINT_KEY = 'expose_topic_endpoint'

# Default values
CONNECTION = {
    MODULE_KEY: 'pubsub.redis_connection',
    CLASS_KEY: 'RedisConnection',
    'host': 'localhost',
    'port': '6379',
    'user': '',
    'password': ''
}

PUBLISHER = {
    MODULE_KEY: 'pubsub.redis_publisher',
    CLASS_KEY: 'RedisPublisher',
    'connection': CONNECTION,
    'publisher_max_threads': 5
}

LOGGER_LEVEL = 'DEBUG'
EXPOSE_TOPIC_ENDPOINT = True
SCHEDULER_MAX_THREADS = 10

CONVERTER = None

DEFAULT_CONF = {
    PUBLISHER_KEY: PUBLISHER,
    ROOT_LOGGER_LEVEL_KEY: LOGGER_LEVEL,
    CONVERTER_KEY: CONVERTER,
    SCHEDULER_MAX_THREADS_KEY: SCHEDULER_MAX_THREADS,
    EXPOSE_TOPIC_ENDPOINT_KEY: EXPOSE_TOPIC_ENDPOINT
}


class Configuration:
    FILENAME = 'config.yaml'
    SPEC_FILENAME = 'configspec.json'
    logger = logging.getLogger(__name__)

    def __init__(self, file_path=FILENAME, spec_file_path=SPEC_FILENAME):
        self.config = DEFAULT_CONF
        conf_ = anyconfig.load(file_path)
        config_spec = anyconfig.load(spec_file_path)

        import re
        os_environ = environ.copy()
        re_exp = re.compile('radar_iot_.*')
        allowed_keys = filter(re_exp.match, os_environ.keys())
        environ_vars = {re.sub("radar_iot_", "", key, count=1): os_environ[key] for key in allowed_keys}

        anyconfig.merge(self.config, environ_vars)
        anyconfig.merge(self.config, conf_)
        (rc, err) = anyconfig.validate(self.config, config_spec)
        if rc is False or err != '':
            raise AttributeError('Invalid configuration', err)
        else:
            self.logger.info(f'Successfully loaded configuration from {self.FILENAME}')

    def get_config(self):
        return self.config

    def get_sensors(self):
        return self.config['sensors']

    def is_topic_endpoint_exposed(self):
        return self.config['expose_topic_endpoint']

    def get_root_logger_level(self):
        return self.config['root_logger_level']

    def get_publisher(self):
        return self.config['publisher']

    def get_scheduler_max_threads(self):
        return self.config['scheduler_max_threads']

    def get_converter(self):
        return self.config['converter']


config = Configuration()


class ConfigHelper:
    converter = None
    publisher = None

    @staticmethod
    def get_converter() -> [MessageConverter, None]:
        if ConfigHelper.converter is None:
            converter_conf = config.get_converter()
            if converter_conf is not None:
                schema_retriever = DynamicImporter(converter_conf['schema_retriever'][MODULE_KEY],
                                                   converter_conf['schema_retriever'][CLASS_KEY],
                                                   kwargs=converter_conf['schema_retriever']['args']).instance

                ConfigHelper.converter = DynamicImporter(converter_conf[MODULE_KEY],
                                                         converter_conf[CLASS_KEY], schema_retriever).instance
                return ConfigHelper.converter
            else:
                return None
        else:
            return ConfigHelper.converter

    @staticmethod
    def get_connection() -> Connection:
        return DynamicImporter(config.get_publisher()['connection'][MODULE_KEY],
                               config.get_publisher()['connection'][CLASS_KEY],
                               config.get_publisher()['connection']['host'],
                               config.get_publisher()['connection']['port'],
                               config.get_publisher()['connection']['user'],
                               config.get_publisher()['connection']['password']).instance

    @staticmethod
    def get_publisher() -> Publisher:
        if ConfigHelper.publisher is None:
            publishing_thread_pool = ThreadPoolExecutor(max_workers=config.get_publisher()['publisher_max_threads'])
            ConfigHelper.publisher = DynamicImporter(config.get_publisher()[MODULE_KEY],
                                                     config.get_publisher()[CLASS_KEY],
                                                     ConfigHelper.get_connection(), publishing_thread_pool).instance
            return ConfigHelper.publisher
        else:
            return ConfigHelper.publisher

    @staticmethod
    def get_sensors() -> List[Sensor]:
        sensor_list = list()
        for sensor_config in config.get_sensors():
            sensor_list.append(
                DynamicImporter(sensor_config[MODULE_KEY], sensor_config[CLASS_KEY], sensor_config['name'],
                                sensor_config[TOPIC_KEY], sensor_config[POLL_FREQUENCY_KEY],
                                sensor_config[FLUSH_SIZE_KEY], sensor_config[FLUSH_AFTER_S_KEY]).instance)
        return sensor_list

    @staticmethod
    def get_configuration():
        return config

    @staticmethod
    def get_default_naming_strategy() -> SchemaNamingStrategy:
        return SchemaRegistryBasedSchemaNamingStrategy()
