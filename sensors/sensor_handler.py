from commons.dynamic_import import DynamicImporter
from sensors.sensor import Sensor
import logging
from config import Configuration

logger = logging.getLogger(__name__)

MODULE_KEY = 'module'
CLASS_KEY = 'class'
TOPIC_KEY = 'publishing_topic'
POLL_FREQUENCY_KEY = 'poll_frequency_ms'
FLUSH_SIZE_KEY = 'flush_size'
FLUSH_AFTER_S_KEY = 'flush_after_s'


class SensorHandler:
    sensors: [Sensor] = list()

    def __init__(self, config: Configuration):
        for sensor in config.get_sensors():
            self.sensors.append(
                DynamicImporter(sensor[MODULE_KEY], sensor[CLASS_KEY], sensor[TOPIC_KEY], sensor[POLL_FREQUENCY_KEY],
                                sensor[FLUSH_SIZE_KEY], sensor[FLUSH_AFTER_S_KEY]).instance)
        logger.warning(self.sensors)

    def get_topics(self):
        return [x.topic for x in self.sensors]
