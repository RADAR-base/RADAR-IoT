import logging
from abc import ABC, abstractmethod
from concurrent.futures import ThreadPoolExecutor

logger = logging.getLogger('root')


class Connection(ABC):
    host = 'localhost'
    port = '8080'

    def __init__(self, host, port, user, password):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.connect()

    @abstractmethod
    def connect(self) -> None:
        pass

    @abstractmethod
    def get_connection(self):
        pass

    @abstractmethod
    def is_connected(self) -> bool:
        pass

    @abstractmethod
    def get_connection_attributes(self):
        pass


class Publisher(ABC):

    def __init__(self, connection: Connection, publisher_thread_pool: ThreadPoolExecutor):
        self.connection = connection
        self.publisher_thread_pool = publisher_thread_pool
        self.topic_prefix = 'data-stream/sensors/'
        if connection.is_connected():
            logger.debug(f'Using the connection {connection.__class__.__name__} for publishing.')
        else:
            raise ConnectionError(f'Connection failed: {connection.__class__.__name__}')

    @abstractmethod
    def _publish(self, msgs, topic: str) -> None:
        pass

    def publish(self, msgs, topic: str):
        self.publisher_thread_pool.submit(self._publish, msgs, topic)


class Subscriber(ABC):
    @abstractmethod
    def subscribe(self):
        pass
