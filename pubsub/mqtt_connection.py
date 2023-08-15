import logging

import redis

from pubsub import Connection
from paho.mqtt import client as mqtt_client
logger = logging.getLogger('root')

class MqttConnection(Connection):

    def __init__(self, host='localhost', port='6379', user=None, password=None):
        super().__init__(host, port, user, password)
        self.is_connection = False

    # Create a dummy connection to check if REDIS is available and future connections can be made
    def connect(self):
        def on_connection(user, userdata, flags, rc):
            if rc == 0:
                print("MQTT Broker has been connected!")
            else:
                print("Failed to connect, return code %d\n", rc)

        client = mqtt_client.Client(self.user)
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

    def on_connect(self, conn: redis.Connection):
        logger.info(f'Connection to MQTT established.')


    def get_connection_pool(self):
        logger.info(f'get coonection pool')

    def release_connection(self, redis_conn):
        logger.info(f'get coonection pool')