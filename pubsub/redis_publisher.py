from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection
import logging
from concurrent.futures import ThreadPoolExecutor
from commons.message_converter import MessageConverter
import redis

logger = logging.getLogger('root')


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection(),
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        self.redis_client = redis.Redis(connection_pool=connection.get_connection_pool())
        super().__init__(connection, publisher_thread_pool)

    # if validate_only is True, then only validation is performed and no conversion.
    def publish(self, msgs: list, topic: str, converter: MessageConverter, validate_only=False,
                schema_name=None) -> None:
        if converter is not None and schema_name is not None and validate_only:
            # do validation
            if converter.validate_all(msgs, schema_name):
                logger.debug(f' Validated {len(msgs)} messages using converter {converter.__class__.__name__}')
            elif converter is not None and schema_name is not None:
                msgs = converter.convert(msgs, schema_name)
                logger.debug(f'Converted message is {msgs} using {converter.__class__.__name__}')
            else:
                logger.warning(f'Validation or Conversion failed for msgs {msgs}. Please look for any errors.')
                return

        for msg in msgs:
            try:
                # publish messages
                self.redis_client.publish(topic, msg)
            except redis.PubSubError as error:
                logger.warning(f'Could not publish message {msg} due to {error}')
                pass

        logger.info(f'Published {len(msgs)} messages using publisher {self.__class__.__name__}')
