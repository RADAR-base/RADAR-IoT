import logging
import random
import time

from paho.mqtt import client as mqtt_client

logger = logging.getLogger('root')


# Create a dummy connection to MQTT
def connection_mqtt(broker,port,client_id):
    def on_connection(client, userdata, flags, rc):
        if rc == 0:
            print(f'Connecting to MQTT at {broker}:{port}')
        elif rc==1:
            print(f'Connection refused - incorrect protocol version')
        elif rc==2:
            print(f'Connection refused - invalid client identifier')
        elif rc==3:
            print(f'Connection refused - server unavailable')
        elif rc==4:
            print(f'Connection refused - bad username or password')
        else:
            print(f'Connection refused - not authorised 6-255: Currently unused')
    client = mqtt_client.Client(client_id)
    client.username_pw_set("radarbase", "password")
    client.on_connect = on_connection
    client.connect(broker, port)
    return client

def subscribe(client,topic):
    def on_message(client,userdata,message):
        # just for test of the MQTT
        print(f"Received `{message.payload.decode()}` from `{message.topic}` topic")
    client.subscribe(topic)
    client.on_message = on_message

if __name__ == '__main__':
    broker = 'broker.emqx.io'
    port = 1883
    topic = "data-stream/sensors/mock"
    # generate client ID with pub prefix randomly
    client_id = f'radarbase-{random.randint(0, 100)}'
    client = connection_mqtt(broker,port,client_id)
    subscribe(client,topic)
    client.loop_forever()
