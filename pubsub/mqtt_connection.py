import logging

import redis

from pubsub import Connection
from paho.mqtt import client as mqtt_client
logger = logging.getLogger('root')

class MqttConnection(Connection):

    def __init__(self, host='broker.emqx.io', port='1883', user="radarbase", password="password", QoS=0):
        self.is_connection = False
        self.client = None
        self.is_connected_flag = False
        super().__init__(host, port, user, password)

    # Create a dummy connection to check if MQTT is available and future connections can be made
    def connect(self):
        def on_connection(user, userdata, flags, rc):
            if rc == 0:
                logger.info("MQTT Broker has been connected!")
                self.is_connected_flag = True
            else:
                logger.error("Failed to connect, return code %d\n", rc)
                raise Exception("Cannot Connect to MQTT broker, please check required configurations.")
        
        def on_disconnect(client, userdata, rc):
            logger.info("MQTT Broker has been Disconnected!")
            self.is_connected_flag = False

        self.client = mqtt_client.Client(self.user)
        self.client.username_pw_set(self.user, self.password)
        self.client.on_connect = on_connection
        self.client.on_disconnect = on_disconnect
        #print(self.host)
        self.client.connect(self.host, self.port)
        return self.client

    def get_connection(self):
        logger.debug('Getting MQTT Connection...')
        # creates and returns a single connection from the Connection Pool.
        if self.is_connected_flag:
            return self.client
        else:
            return self.connect()

    def get_connection_attributes(self):
        logger.debug(f'Returning Connection arguments of {self.__class__.__name__}')
        return dict(type=self.__class__.__name__, host=super().host, port=super().port, user=super().user,
                    password=super().password, QoS=self.QoS)

    def is_connected(self) -> bool:
        logger.debug(f'Checking if connection is successful...')
        return self.is_connected_flag or self.client != None
