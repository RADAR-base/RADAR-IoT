import anyconfig
import logging


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
        return self.config['root_logger_level']

    def get_publisher_max_threads(self):
        return self.config['publisher_max_threads']
