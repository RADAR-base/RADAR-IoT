from .sensor import Sensor
from pubsub.publisher import Publisher
from pubsub.redis_publisher import RedisPublisher
from commons.message_converter import MessageConverter
import logging

logger = logging.getLogger('root')


class CoralEnviroHumiditySensor(Sensor):
    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s, publisher: Publisher = RedisPublisher(),
                 converter: MessageConverter = None):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s, publisher, converter)

    def get_data(self):
        logger.debug('humidity data')
        return 35.7
