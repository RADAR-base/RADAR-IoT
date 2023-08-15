import logging
from concurrent.futures import ThreadPoolExecutor
import random
from pubsub import Publisher
from pubsub.mqtt_connection import MqttConnection
from time import time
logger = logging.getLogger('root')
import json

class MqttPublisher(Publisher):

    def __init__(self, connection: MqttConnection,
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4)):
        super().__init__(connection, publisher_thread_pool)

    def _publish(self,msgs,topic):
        try:
            # publish messages
            if msgs is not None:
                client = self.connection.connect()
                client.loop_start()
                print("msgs is {}".format(msgs))
                print("tps  is {}".format(topic))
                result = client.publish(topic, msgs)
                if result[0] == 0:
                    logger.info(f'Published messages using publisher MQTT'
                                f' on channel {topic}.')

            else:
                logger.warning(f'Messages cannot be none.')
        except:
            logger.warning("something error in publisher")
            pass
