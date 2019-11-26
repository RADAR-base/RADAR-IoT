import random
from datetime import datetime

from sensors.sensor import Sensor


class MockSensor(Sensor):

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_data(self):
        return {'time': datetime.now().timestamp(), 'value': random.random() * 1000}
