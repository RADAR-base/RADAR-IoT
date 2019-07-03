from config import ConfigHelper
from sensors.sensor_handler import SensorHandler
import commons.log as log


def main():
    config = ConfigHelper.get_configuration()
    logger = log.setup_custom_logger('root', config.get_root_logger_level())
    logger.info(config.get_config())
    sensor_handler = SensorHandler(config)

if __name__== "__main__":
    main()
