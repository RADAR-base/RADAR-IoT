import logging

from .sensor import Sensor

logger = logging.getLogger('root')


class CoralEnviroHumiditySensor(Sensor):
    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_data(self):
        logger.debug('humidity data')
        return 35.7
