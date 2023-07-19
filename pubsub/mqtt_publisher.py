import logging
from concurrent.futures import ThreadPoolExecutor
import random
from pubsub import Publisher
from paho.mqtt import client as mqtt_client
from time import time
logger = logging.getLogger('root')


class MqttPublisher(Publisher):

    def __init__(self, broker, client_id,port):
        self.client_id = client_id
        self.broker = broker
        self.port = port


    def connect_mqtt(self):
        def on_connection(client, userdata, flags, rc):
            if rc == 0:
                print("MQTT Broker has been connected!")
            else:
                print("Failed to connect, return code %d\n", rc)

        client = mqtt_client.Client(self.client_id)
        client.on_connect = on_connection
        client.connect(self.broker, self.port)
        return client

    def publish(self,msgs,topic):
        try:
            # publish messages
            client = self.connect_mqtt()
            client.loop_start()
            result = self.client.publish(topic, msgs)
            if result[0] == 0:
                logger.info(f'Published messages using publisher MQTT'
                            f' on channel {topic}.')
            else:
                logger.warning(f'Messages cannot be none.')
        except:
            logger.warning("something error in publisher")
            pass
