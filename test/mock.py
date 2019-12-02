import random
from datetime import datetime

from commons.data import Response, Error, ErrorCode
from sensors import Sensor


class MockSensor(Sensor):

    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)
        self.global_counter = 0

    def get_measurement(self):
        self.global_counter += 1
        if self.global_counter % 10 == 0:
            return Response(response=None, errors=[
                Error('MockError', ErrorCode.STATUS_OFF, 'The MockSensor mocks an error every 10 iterations',
                      'blah->nooooo->save me->dead')])
        else:
            return Response({'time': datetime.now().timestamp(), 'value': random.random() * 1000}, errors=None)
