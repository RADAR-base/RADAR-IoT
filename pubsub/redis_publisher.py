import logging
from concurrent.futures import ThreadPoolExecutor

import redis

from pubsub import Publisher
from pubsub.redis_connection import RedisConnection

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        super().__init__(connection, publisher_thread_pool)

    def _publish(self, msgs, topic: str) -> None:
        try:
            # publish messages
            if msgs is not None:
                num_subscribers = self.redis_client.publish(topic, msgs)
                logger.info(f'Published messages using publisher {self.__class__.__name__}'
                            f' on channel {topic}. It was delivered to {num_subscribers} subscribers')
            else:
                logger.warning(f'Messages cannot be none.')
        except redis.PubSubError as error:
            logger.warning(f'Could not publish message {msgs} due to {error}')
            # TODO: Cache the data instead of discarding
            pass
        except redis.exceptions.DataError as error:
            logger.warning(f'Could not publish message {msgs} due to {error}. Please perform necessary '
                           f'conversions first.')
            pass
