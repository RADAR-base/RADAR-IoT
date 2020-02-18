import logging
from datetime import datetime

from commons.data import Response
from sensors import Sensor

logger = logging.getLogger('root')


class Th02TemperatureSensor(Sensor):
    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_measurement(self):
        logger.debug('TH02 temp data')
        return Response({'time': datetime.now().timestamp(), 'value': 40}, errors=None)
