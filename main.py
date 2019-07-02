from config import Configuration
from sensors.sensor_handler import SensorHandler
import commons.log as log


def main():
    config = Configuration()
    logger = log.setup_custom_logger('root', config.get_root_logger_level())
    logger.info(config.get_config())
    sensor_handler = SensorHandler(config)

if __name__== "__main__":
    main()
