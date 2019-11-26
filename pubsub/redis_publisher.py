import logging
from concurrent.futures import ThreadPoolExecutor

import redis

from commons.message_converter import MessageConverter
from config import ConfigHelper
from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        self.converter: MessageConverter = ConfigHelper.get_converter()
        super().__init__(connection, publisher_thread_pool)

    def _publish(self, msgs: list, topic: str, schema_name=None) -> None:
        msgs_converted = None
        if self.converter is not None:
            msgs_converted = self.converter.convert_all(msgs, schema_name)
            logger.debug(f'Converted message is {msgs} using {self.converter.__class__.__name__}')

        try:
            # publish messages
            if msgs_converted is not None:
                self.redis_client.publish(topic, msgs_converted)
        except redis.PubSubError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}')
            pass
        except redis.exceptions.DataError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}. Please perform necessary '
                           f'conversions first.')
            pass

        logger.info(f'Published {len(msgs)} messages using publisher {self.__class__.__name__}')
