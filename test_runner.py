from sys import argv
from time import sleep

from commons import log
from config import Factory
from handler.sensor_handler import SensorHandler


def test(run_time):
    config = Factory.get_configuration()
    logger = log.setup_custom_logger('root', config.get_root_logger_level())
    logger.info(config.get_config())

    sensor_handler = SensorHandler(config)

    sleep(run_time)
    logger.info(f'Testing ran for {run_time} seconds. Exiting now...')
    sensor_handler.graceful_stop()
    exit(0)


if __name__ == "__main__":
    run_duration = int(argv[1])
    test(run_duration)
