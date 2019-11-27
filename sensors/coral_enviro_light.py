import logging
from datetime import datetime

from commons.data import Response
from sensors.sensor import Sensor

logger = logging.getLogger('root')


class CoralEnviroLightSensor(Sensor):
    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_measurement(self):
        logger.debug('light data')
        return Response({'time': datetime.now().timestamp(), 'value': 105}, errors=None)
