from abc import ABC, abstractmethod
from datetime import datetime
from pubsub.publisher import Publisher
from pubsub.redis_publisher import RedisPublisher
import queue
import logging

logger = logging.getLogger('root')


class Sensor(ABC):
    _FLUSH_OFFSET_S = 200
    _last_flush = datetime.now().timestamp()

    def __init__(self, topic, poll_freq_ms, flush_size=100, flush_after_s=2000,
                 publisher: Publisher = RedisPublisher()):
        self.topic = topic
        self.poll_freq_ms = poll_freq_ms
        self.flush_size = flush_size
        if flush_after_s > flush_size * poll_freq_ms / 1000:
            self.flush_after_s = flush_after_s
        else:
            self.flush_after_s = self._FLUSH_OFFSET_S + (flush_size * poll_freq_ms / 1000)
        self.publisher = publisher
        self.queue = queue.Queue(int(flush_size + flush_size / 2))
        super().__init__()
        logger.info(f'Successfully initialised Sensor of type : {self.__class__.__name__}')

    def poll(self) -> None:
        if datetime.now().timestamp() - self._last_flush > self.flush_after_s:
            self.flush()
        if self.queue.qsize() >= self.flush_size:
            self.flush()
        data = self.get_data()
        self.queue.put(data)
        logger.debug(f'Queue Size for {self.__class__.__name__} is {self.queue.qsize()}')

    def flush(self) -> None:
        msgs = list()
        if self.flush_size <= self.queue.qsize():
            max_range = self.flush_size
        else:
            max_range = self.queue.qsize()
        for msg in range(0, max_range):
            msgs.append(self.queue.get())
        self._publish(msgs)
        self._last_flush = datetime.now().timestamp()

    # Private as it's working is internal
    def _publish(self, msgs) -> None:
        self.publisher.publish_threaded(msgs)

    @abstractmethod
    def get_data(self):
        pass
