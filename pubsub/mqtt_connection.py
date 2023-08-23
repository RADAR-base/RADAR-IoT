import logging

import redis

from pubsub import Connection
from paho.mqtt import client as mqtt_client
logger = logging.getLogger('root')

class MqttConnection(Connection):

    def __init__(self, host='broker.emqx.io', port='1883', user="radarbase", password="password", QoS=0):
        super().__init__(host, port, user, password, QoS)
        self.is_connection = False

    # Create a dummy connection to check if MQTT is available and future connections can be made
    def connect(self):
        def on_connection(user, userdata, flags, rc):
            if rc == 0:
                logger.info("MQTT Broker has been connected!")
            else:
                logger.info("Failed to connect, return code %d\n", rc)

        client = mqtt_client.Client(self.user)
        client.username_pw_set(self.user, self.password)
        client.on_connect = on_connection
        #print(self.host)
        client.connect(self.host, self.port)
        return client

    def get_connection(self):
        logger.debug('Getting MQTT Connection...')
        # creates and returns a single connection from the Connection Pool.
        return None

    def get_connection_attributes(self):
        logger.debug("connection debugger")

    def is_connected(self) -> bool:
        logger.debug(f'Checking if connection is successful...')
        return True

    def on_connect(self, conn):
        logger.info(f'Connection to MQTT established.')


    def get_connection_pool(self):
        logger.info(f'get coonection pool')

