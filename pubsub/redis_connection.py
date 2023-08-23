import logging

import redis

from pubsub import Connection

logger = logging.getLogger('root')


class RedisConnection(Connection):

    def __init__(self, host='localhost', port='6379', user=None, password=None, QoS=0):
        self.redis_conn_pool = redis.ConnectionPool(host=host, port=port, password=password)
        self.is_connection_available = False
        super().__init__(host, port, user, password, QoS)

    # Create a dummy connection to check if REDIS is available and future connections can be made
    def connect(self) -> None:
        logger.debug(f'Connecting to Redis at {self.host}:{self.port}')
        dummy_connection = self.redis_conn_pool.make_connection()
        dummy_connection.register_connect_callback(self.on_connect)
        dummy_connection.connect()

    def get_connection(self):
        logger.debug('Getting Redis Connection...')
        # creates and returns a single connection from the Connection Pool.
        return self.redis_conn_pool.make_connection()

    def get_connection_attributes(self):
        logger.debug(f'Returning Connection arguments of {self.__class__.__name__}')
        return dict(type=self.__class__.__name__, host=super().host, port=super().port, user=super().user,
                    password=super().password)

    def is_connected(self) -> bool:
        logger.debug(f'Checking if connection is successful...')
        return self.is_connection_available

    def on_connect(self, conn: redis.Connection):
        logger.info(f'Connection to Redis established. {conn}')
        self.is_connection_available = True

    def get_connection_pool(self):
        return self.redis_conn_pool

    def release_connection(self, redis_conn):
        self.redis_conn_pool.release(redis_conn)
