## RADAR-IoT edge docker-compose stack

These services are to be deployed on an edge device like Raspberry Pi for accessing sensors and using their data.
This is the production stack and hence uses the stable Docker images published on Docker Hub.

### Usage

1. Configure the iot python service by copying the `etc/iot-python/config.yaml.template` to `etc/iot-python/config.yaml` and updating the properties as required.
2. Configure the data uploader service by copying the `etc/data-uploader/radar_iot_config.yaml.template` to `etc/data-uploader/radar_iot_config.yaml` and updating the properties as required.
3. Make an external docker network named `redis` using `docker network create redis`
4. Run the stack using `docker-compose up -d`