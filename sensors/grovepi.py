import logging
from datetime import datetime

# Install the grovepi library first using instructions here - https://github.com/DexterInd/GrovePi
import grovepi

from commons.data import Response, IoTError, ErrorCode
from sensors import Sensor

logger = logging.getLogger('root')


class AirQualitySensor(Sensor):

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        # Connect the Grove Air Quality Sensor to analog port A0
        # SIG,NC,VCC,GND
        self.air_sensor = 0

        grovepi.pinMode(self.air_sensor, "INPUT")
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_measurement(self):
        logger.debug('air quality sensor data')

        try:
            # Get sensor value
            sensor_value = grovepi.analogRead(self.air_sensor)

            if sensor_value > 700:
                air_quality = "High pollution"
            elif sensor_value > 300:
                air_quality = "Low Pollution"
            else:
                air_quality = "Air fresh"

            return Response({'time': datetime.now().timestamp(), 'value': sensor_value, 'airQuality': air_quality},
                            errors=None)
        except IOError as exc:
            return Response(response=None,
                            errors=[IoTError('IOError', ErrorCode.UNKNOWN, exc.__cause__, exc.__traceback__)])
