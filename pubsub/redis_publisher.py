import logging
from concurrent.futures import ThreadPoolExecutor

import redis

from config import ConfigHelper
from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection
import json

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        self.converter = ConfigHelper.get_converter()
        super().__init__(connection, publisher_thread_pool)

    # if validate_only is True, then only validation is performed and no conversion.
    def _publish(self, msgs: list, topic: str, schema_name=None) -> None:
        if self.converter is not None:
            if self.converter.validate_only:
                # do validation
                if self.converter.validate_all(msgs, schema_name):
                    logger.debug(f' Validated {len(msgs)} messages for {schema_name} using converter {self.converter.__class__.__name__}')
                    msgs_converted = json.dumps(msgs)
                else:
                    logger.warning(f'Validation failed for messages {msgs}. Please look for any errors. Not publishing...')
                    return
            else:
                msgs_converted = self.converter.convert(msgs, schema_name)
                logger.debug(f'Converted message is {msgs} using {self.converter.__class__.__name__}')

        try:
            # publish messages
            self.redis_client.publish(topic, msgs_converted)
        except redis.PubSubError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}')
            pass
        except redis.exceptions.DataError as error:
            logger.warning(f'Could not publish message {msgs_converted} due to {error}. Please perform necessary conversions first.')
            pass

        logger.info(f'Published {len(msgs)} messages using publisher {self.__class__.__name__}')
