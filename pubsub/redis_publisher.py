from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection
import logging
from concurrent.futures import ThreadPoolExecutor

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        super().__init__(connection, publisher_thread_pool)

    def publish(self, msg) -> None:
        # do publish
        logger.info(f'Publishing {len(msg)} messages using connection {self.__class__.__name__}')
