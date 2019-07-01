from pubsub.publisher import Publisher
from pubsub.redis_connection import RedisConnection
import logging

logger = logging.getLogger(__name__)


class RedisPublisher(Publisher):

    def __init__(self, connection: RedisConnection = RedisConnection()):
        super().__init__(connection)

    async def publish(self, msg) -> None:
        # await do publish
        logger.debug(f'Publishing the Message ({msg}) using connection {self.__class__.__name__}')
