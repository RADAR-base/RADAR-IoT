from .sensor import Sensor
from pubsub.publisher import Publisher
from pubsub.redis_publisher import RedisPublisher

import logging

logger = logging.getLogger('root')


class CoralEnviroHumiditySensor(Sensor):
    def __init__(self, topic, poll_freq_ms, flush_size, flush_after_s, publisher: Publisher = RedisPublisher()):
        super().__init__(topic, poll_freq_ms, flush_size, flush_after_s, publisher)

    def get_data(self):
        logger.debug('humidity data')
        return 35.7
