import anyconfig
import logging
from commons.dynamic_import import DynamicImporter
from commons.message_converter import MessageConverter
from pubsub.publisher import Publisher
from pubsub.connection import Connection
from sensors.sensor import Sensor
from typing import List
from concurrent.futures import ThreadPoolExecutor
from commons.schema import SchemaNamingStrategy, SensorBasedSchemaNamingStrategy

MODULE_KEY = 'module'
CLASS_KEY = 'class'
TOPIC_KEY = 'publishing_topic'
POLL_FREQUENCY_KEY = 'poll_frequency_ms'
FLUSH_SIZE_KEY = 'flush_size'
FLUSH_AFTER_S_KEY = 'flush_after_s'


class Configuration:
    FILENAME = 'config.yaml'
    SPEC_FILENAME = 'configspec.json'
    logger = logging.getLogger(__name__)

    def __init__(self):
        config_spec = anyconfig.load(self.SPEC_FILENAME)
        self.config = anyconfig.load(self.FILENAME)
        (rc, err) = anyconfig.validate(self.config, config_spec)
        if rc is False or err != '':
            raise AttributeError('Invalid configuration', err)
        else:
            self.logger.info('Successfully loaded configuration from %s' % self.FILENAME)

    def get_config(self):
        return self.config

    def get_sensors(self):
        return self.config['sensors']

    def is_topic_endpoint_exposed(self):
        return self.config['expose_topic_endpoint']

    def get_root_logger_level(self):
        if 'root_logger_level' in self.config:
            return self.config['root_logger_level']
        else:
            return 'INFO'

    def get_publisher(self):
        return self.config['publisher']

    def get_scheduler_max_threads(self):
        return self.config['scheduler_max_threads']

    def get_converter(self):
        if 'converter' in self.config and 'schema_retriever' in self.config['converter']:
            return self.config['converter']
        else:
            return None


config = Configuration()


class ConfigHelper:

    @staticmethod
    def get_converter() -> [MessageConverter, None]:
        converter_conf = config.get_converter()
        if converter_conf is not None:
            schema_retriever = DynamicImporter(converter_conf['schema_retriever'][MODULE_KEY],
                                               converter_conf['schema_retriever'][CLASS_KEY],
                                               kwargs=converter_conf['schema_retriever']['args']).instance
            return DynamicImporter(converter_conf[MODULE_KEY],
                                   converter_conf[CLASS_KEY], schema_retriever).instance
        else:
            return None

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
        publishing_thread_pool = ThreadPoolExecutor(max_workers=config.get_publisher()['publisher_max_threads'])
        return DynamicImporter(config.get_publisher()[MODULE_KEY], config.get_publisher()[CLASS_KEY],
                               ConfigHelper.get_connection(), publishing_thread_pool).instance

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
        return SensorBasedSchemaNamingStrategy(prefix='', suffix='')
