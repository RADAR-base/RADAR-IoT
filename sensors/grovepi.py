import logging
from abc import ABC
from datetime import datetime
from threading import RLock

# Install the grovepi library first using instructions here - https://github.com/DexterInd/GrovePi
import grovepi

from commons.data import Response, IoTError, ErrorCode
from sensors import Sensor

logger = logging.getLogger('root')

grovepi_lock: RLock = RLock()


class GrovePiSensor(Sensor, ABC):
    """The base class for requesting data from any sensors connected vai the Grove Pi hat.
    The GrovePi Python library is not Thread-Safe and this ensures that any sensor that queries the grovepi should first
    acquire the Lock to prevent concurrent access.

    Some other configs necessary to get the GrovePi to work better on Raspberry Pi 4(although could apply to other
    Raspberry pi models too)-

    1. Turn off the One-wire Interface (1-W). This interferes with the grovepi and could make it unresponsive.
    2. Make sure not other external program/application is accessing the grovepi at the same time.
    3. Anytime you need to access the grovepi library in your sensor implementation, please acquire the lock first using

            with grovepi_lock:
                // DO you stuff with grovepi

        Note that you do not need to do this in the get_measurement() method as that is already handled
         in the poll function here.
    """

    def __init__(self, name, topic, poll_freq_ms, flush_size=100, flush_after_s=2000):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def poll(self) -> None:
        logger.debug('Acquiring Lock for GrovePi')
        with grovepi_lock:
            super().poll()
        logger.debug('Lock released.')


class AirQualitySensor(GrovePiSensor):
    # http://wiki.seeedstudio.com/Grove-Air_Quality_Sensor_v1.3/

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        # Connect the Grove Air Quality Sensor to analog port A0
        # SIG,NC,VCC,GND
        self.air_sensor = 0
        with grovepi_lock:
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
