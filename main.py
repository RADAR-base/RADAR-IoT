from config import Configuration
import logging
from sensors.sensor_handler import SensorHandler

logger = logging.getLogger(__name__)

def main():
    config = Configuration()
    logger.info(config.get_config())
    sensor_handler = SensorHandler(Configuration())

if __name__== "__main__":
    main()
