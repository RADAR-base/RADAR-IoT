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
            logger.debug(f'Using the connection {connection.__class__.__name__} for publishing.')
        else:
            raise ConnectionError(f'Connection failed: {connection.__class__.__name__}')

    @abstractmethod
    def _publish(self, msgs: list, topic: str, validate_only=False,
                 schema_name=None) -> None:
        pass

    def publish(self, msgs: list, topic: str, validate_only=False,
                schema_name=None):
        self.publisher_thread_pool.submit(self._publish, msgs, topic, validate_only, schema_name)
