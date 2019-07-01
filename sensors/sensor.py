from abc import ABC, abstractmethod
from datetime import datetime
import logging
from pubsub.publisher import Publisher

logger = logging.getLogger(__name__)

class Sensor(ABC):
    # TODO Add publisher
    # TODO Add cache/queue and flushing logic
    # TODO Add publishing logic

    _FLUSH_OFFSET_S = 200
    _last_flush = datetime.now()

    def __init__(self, topic, poll_freq_ms, flush_size, flush_after_s, publisher: Publisher):
        self.topic = topic
        self.poll_freq_ms = poll_freq_ms
        self.flush_size = flush_size
        if flush_after_s > flush_size * poll_freq_ms / 1000:
            self.flush_after_s = flush_after_s
        else:
            self.flush_after_s = self._FLUSH_OFFSET_S + (flush_size * poll_freq_ms / 1000)
        super().__init__()
        self.publisher = publisher
        logger.info(f'Successfully initialised Sensor of type : {self.__class__.__name__}')

    # TODO add timer/scheduler
    def poll(self) -> None:
        pass

    # Private as it's working is internal
    def _flush(self) -> None:
        pass

    # Private as it's working is internal
    # Use aysyncio for cooperative multi-tasking
    async def _publish(self) -> None:
        await self.publisher.publish()

    @abstractmethod
    def get_data(self):
        pass
