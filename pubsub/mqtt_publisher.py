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
                 publisher_thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=4),**kwargs):
        super().__init__(connection, publisher_thread_pool)
        if "QoS" in kwargs:
            self.QoS = int(kwargs.get('QoS'))
            
    def _publish(self,msgs,topic):
        try:
            # publish messages
            if msgs is not None:
                client = self.connection.connect()
                client.loop_start()
                result = client.publish(topic, msgs, qos=self.QoS)
                logger.info(f"Quality of service is {self.QoS}")
                if result[0] == 0:
                    logger.info(f'Published messages using publisher MQTT'
                                f' on channel {topic}.')
                elif result[0] == 1:
                    logger.info(f'Connection refused - incorrect protocol version')
                elif result[0] == 2:
                    logger.info(f'Connection refused - invalid client identifier')
                elif result[0] == 3:
                    logger.info(f'Connection refused - server unavailable')
                elif result[0] == 4:
                    logger.info(f'Connection refused - bad username or password')
                else:
                    logger.info(f'Connection refused - not authorised 6-255: Currently unused')
            else:
                logger.warning(f'Messages cannot be none.')
        except:
            logger.warning("something error in publisher")
            pass
