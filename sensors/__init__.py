import logging
import queue
from abc import ABC, abstractmethod
from datetime import datetime

from commons.data import Response, IoTError, ErrorCode

logger = logging.getLogger('root')


class Sensor(ABC):
    """The base class for all Sensor implementations. The abstract nature allows for specifying sensor specific impl.
    This is also responsible for the flushing logic and passing the data to the data processor.

    The `get_measurement` method needs to be overridden by the sub-classes and should return a :class: Response object.
    This class also allows for adding a list errors (:class: Error) and hence any errors while fetching data from
    the sensor needs to be caught properly and then wrapped in the Response object.
    """

    def __init__(self, name, topic, poll_freq_ms, flush_size=100, flush_after_s=2000):
        self.name = name
        self.topic = topic
        self.poll_freq_ms = poll_freq_ms
        self.flush_size = flush_size
        if flush_after_s > flush_size * poll_freq_ms / 1000:
            self.flush_after_s = flush_after_s
        else:
            _FLUSH_OFFSET_S = 200
            self.flush_after_s = _FLUSH_OFFSET_S + (flush_size * poll_freq_ms / 1000)
        from config import Factory
        self.data_processor = Factory.get_data_processor()
        self.queue = queue.Queue(int(flush_size + flush_size / 2))
        self._last_flush = datetime.now().timestamp()
        super().__init__()
        logger.info(f'Successfully initialised Sensor of type : {self.__class__.__name__}')

    def poll(self) -> None:
        if datetime.now().timestamp() - self._last_flush > self.flush_after_s:
            self.flush()
        if self.queue.qsize() >= self.flush_size:
            self.flush()
        data = self.get_measurement()
        self.queue.put(data)
        logger.debug(f'Queue Size for {self.__class__.__name__} is {self.queue.qsize()}')

    def flush(self) -> None:
        logger.debug('Flushing messages now...')
        msgs = list()
        if self.flush_size <= self.queue.qsize():
            max_range = self.flush_size
        else:
            max_range = self.queue.qsize()
        for msg in range(0, max_range):
            msgs.append(self.queue.get())
        self._publish(msgs)
        self._last_flush = datetime.now().timestamp()
        logger.debug(f'Successfully flushed {len(msgs)} messages.')

    # Private as it's working is internal
    def _publish(self, msgs) -> None:
        self.data_processor.process_data(msgs, topic=self.topic, name=self.name)

    @abstractmethod
    def get_measurement(self) -> Response:
        pass


class GPIOSensor(Sensor, ABC):
    """To be extended by any sensor that is supposed to use General purpose I/O (GPIO).
    This will allow interfacing in both directions.

    :param pins: can be a dictionary of all the pins supported by the sensor.
    This will include mapping a string value to an actual hardware pin number
    example :

    pins = {
        'VALUE_X': 20,
        'VALUE_Y': 25,
        'VALUE_Z': 19,
        'CONTROL': 21
    }
    :type pins: dict

    When overriding this class, one must also override the "get_measurement" method of the superclass :class: Sensor
    In this case, it will need to read all the pins and wrap it in a :class: Response object.
    A very basic implementation is provided here but in most cases this will need to be overridden.
    """

    def __init__(self, pins: dict, name, topic, poll_freq_ms, flush_size, flush_after_s):
        self.pins = pins
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_pins(self) -> dict:
        return self.pins

    @abstractmethod
    def read_pin(self, pin) -> Response:
        pass

    @abstractmethod
    def write_pin(self, pin, value):
        pass

    def get_measurement(self) -> Response:
        res = {}
        errs = []
        for key, value in self.pins:
            try:
                res[key] = self.read_pin(value)
            except Exception as exc:
                errs.append(IoTError(type=exc.__class__.__name__, code=ErrorCode.UNKNOWN, reason=str(exc.__cause__),
                                     trace=str(exc.__traceback__)))
                pass
        return Response(response=res, errors=errs)
