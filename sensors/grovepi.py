import logging
import math
from abc import ABC
from datetime import datetime
from threading import RLock

# Install the grovepi library first using instructions here - https://github.com/DexterInd/GrovePi
import grovepi

from commons.data import Response, IoTError, ErrorCode
from sensors import Sensor

logger = logging.getLogger('root')

# A global lock for accessing the GrovePi
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

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s, **kwargs):
        # Connect the Grove Air Quality Sensor to analog port A0
        # SIG,NC,VCC,GND
        if 'port' in kwargs:
            self.air_sensor = int(kwargs.get('port'))
        else:
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
                            errors=[get_error_from_exc(exc)])


class DustSensor(GrovePiSensor):
    # USAGE - https://seeeddoc.github.io/Grove-Dust_Sensor/
    #
    # Connect the dust sensor to Port D2 on the GrovePi. The dust sensor only works on that port
    # The dust sensor takes 30 seconds to update the new values
    #
    # It returns the LPO time, the percentage (LPO time divided by total period, in this case being 30000 ms)
    # and the concentration in pcs/0.01cf

    import atexit

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        # default pin is 2 and default update period is 30000 ms
        with grovepi_lock:
            grovepi.dust_sensor_en()
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_measurement(self) -> Response:
        try:
            values = grovepi.dust_sensor_read()
            lpo_time = values[0]
            percent_lpo = values[1]
            pcs_per_0_1cf = values[2]

            return Response({'time': datetime.now().timestamp(), 'LPOValue': lpo_time, 'percentLPO': percent_lpo,
                             'pcsPer0.1cf': pcs_per_0_1cf}, errors=None)
        except IOError as exc:
            return Response(response=None,
                            errors=[get_error_from_exc(exc)])

    @atexit.register
    def disable_dust_sensor(self):
        """
        Register to disable interrupts for the dust sensor on exit of the application.
        :return: None
        """
        with grovepi_lock:
            grovepi.dust_sensor_dis()


def get_error_from_exc(exc: BaseException):
    return IoTError(exc.__class__.__name__, ErrorCode.UNKNOWN, str(exc.__cause__), str(exc.__traceback__))


class TemperatureAndHumiditySensor(GrovePiSensor):
    # http://wiki.seeedstudio.com/Grove-TemperatureAndHumidity_Sensor/
    # Connect the Grove Temperature & Humidity Sensor (or Pro) to digital port D4
    # This example uses the blue colored sensor.
    # SIG,NC,VCC,GND

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s, **kwargs):
        if 'port' in kwargs:
            self.sensor = int(kwargs.get('port'))
        else:
            self.sensor = 4  # The Sensor goes on digital port 4.

        # temp_humidity_sensor_type
        # Grove Base Kit comes with the blue sensor.
        self.blue = 0  # The Blue colored sensor. Grove Temp & Humidity Sensor
        self.white = 1  # The White colored sensor. Grove Temp & Humidity Sensor Pro

        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_measurement(self) -> Response:
        try:
            [temp, humidity] = grovepi.dht(self.sensor, self.blue)
            if not math.isnan(temp) and not math.isnan(humidity):
                return Response({'time': datetime.now().timestamp(), 'temperature': temp, 'humidity': humidity},
                                errors=None)
        except IOError as exc:
            return Response(response=None, errors=get_error_from_exc(exc))
