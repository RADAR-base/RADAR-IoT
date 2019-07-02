from abc import ABC, abstractmethod
from pubsub.connection import Connection
import logging
from concurrent.futures import ThreadPoolExecutor

logger = logging.getLogger('root')


class Publisher(ABC):

    def __init__(self, connection: Connection, publisher_thread_pool: ThreadPoolExecutor):
        self.connection = connection
        self.publisher_thread_pool = publisher_thread_pool
        if connection.is_connected():
            logger.debug('Connected to the ')
        else:
            raise ConnectionError(f'Connection {connection.__class__.__name__}')

    @abstractmethod
    def publish(self, msgs) -> None:
        pass

    def publish_threaded(self, msgs):
        self.publisher_thread_pool.submit(self.publish, msgs)