from abc import ABC, abstractmethod
from pubsub.connection import Connection
import logging

logger = logging.getLogger(__name__)


class Publisher(ABC):

    def __init__(self, connection: Connection):
        self.connection = connection
        if connection.is_connected():
            logger.debug('Connected to the ')
        else:
            raise ConnectionError(f'Connection {connection.__class__.__name__}')

    @abstractmethod
    async def publish(self, msg) -> None:
        pass
