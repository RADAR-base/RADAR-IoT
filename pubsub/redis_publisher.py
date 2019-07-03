from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection
import logging
from concurrent.futures import ThreadPoolExecutor
import redis
from config import ConfigHelper

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        self.converter = ConfigHelper.get_converter()
        super().__init__(connection, publisher_thread_pool)

    # if validate_only is True, then only validation is performed and no conversion.
    def publish(self, msgs: list, topic: str, validate_only=False, schema_name=None) -> None:
        if self.converter is not None and schema_name is not None and validate_only:
            # do validation
            if self.converter.validate_all(msgs, schema_name):
                logger.debug(f' Validated {len(msgs)} messages using converter {self.converter.__class__.__name__}')
            else:
                logger.warning(f'Validation failed for messages {msgs}. Please look for any errors.')
                return
        elif self.converter is not None and schema_name is not None:
            msgs = self.converter.convert(msgs, schema_name)
            logger.debug(f'Converted message is {msgs} using {self.converter.__class__.__name__}')

        for msg in msgs:
            try:
                # publish messages
                self.redis_client.publish(topic, msg)
            except redis.PubSubError as error:
                logger.warning(f'Could not publish message {msg} due to {error}')
                pass

        logger.info(f'Published {len(msgs)} messages using publisher {self.__class__.__name__}')
