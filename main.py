from sys import exit
from time import sleep

import commons.log as log
from config import ConfigHelper
from handler.sensor_handler import SensorHandler


def main():
    config = ConfigHelper.get_configuration()
    logger = log.setup_custom_logger('root', config.get_root_logger_level())
    logger.info(config.get_config())
    sensor_handler = SensorHandler(config)

    if config.is_travis():
        sleep(200)
        exit(0)


if __name__ == "__main__":
    main()
