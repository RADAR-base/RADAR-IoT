from pubsub.connection import Connection
import logging
import redis

logger = logging.getLogger('root')


class RedisConnection(Connection):

    def __init__(self, host='localhost', port='6379', user=None, password=None):
        self.r = redis.Connection(host, port, password)
        super().__init__(host, port, user, password)

    def connect(self) -> None:
        logger.debug(f'Connecting to Redis at {super().host}:{super().port}')

    def get_connection(self):
        logger.debug('Getting Redis Connection...')

    def get_connection_attributes(self):
        logger.debug(f'Returning host and port of the {self.__class__.__name__}')

    def is_connected(self) -> bool:
        logger.debug(f'Checking if connection is successful...')
        return True
