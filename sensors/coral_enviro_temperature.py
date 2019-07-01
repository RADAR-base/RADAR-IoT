from .sensor import Sensor


class CoralEnviroTemperatureSensor(Sensor):
    def __init__(self, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(topic, poll_freq_ms, flush_size, flush_after_s)

    def get_data(self):
        print('temp data')