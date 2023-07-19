import logging
import random
import time

from paho.mqtt import client as mqtt_client

logger = logging.getLogger('root')

from pubsub import Connection

class MqttSubscribe(Connection):

    def __init__(self, broker, client_id,port):
        self.client_id = client_id
        self.broker = broker
        self.port = port

    # Create a dummy connection to MQTT
    def connection_mqtt(self):
        def on_connection(client, userdata, flags, rc):
            if rc == 0:
                logger.info(f'Connecting to MQTT at {self.broker}:{self.port}')
            else:
                logger.info(f'Failed to connect, return code {rc}\n')

        client = mqtt_client.Client(self.client_id)
        client.on_connect = on_connection
        client.connect(self.broker, self.port)
        return client

    def subscribe(self,topic):
        def on_message(self, client, userdata, message):
            # just for test of the MQTT
            logger.info(f"Received `{message.payload.decode()}` from `{message.topic}` topic")
        client = self.connect_mqtt()
        client.subscribe(self.topic)
        client.on_message = on_message