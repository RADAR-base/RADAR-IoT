from time import sleep

import commons.log as log
from config import ConfigHelper
from handler.sensor_handler import SensorHandler


def main():
    config = ConfigHelper.get_configuration()
    logger = log.setup_custom_logger('root', config.get_root_logger_level())
    logger.info(config.get_config())

    sensor_handler = SensorHandler(config)

    # We need to block the main thread so that the program does not exit.
    # This will allow other threads to continue working for as long as required.
    try:
        while True:
            sleep(1)
    except KeyboardInterrupt:
        logger.warning(f'The process was interrupted. Gracefully shutting down...')
    finally:
        sensor_handler.graceful_stop()


if __name__ == "__main__":
    main()
