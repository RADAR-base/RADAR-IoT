## RADAR-base platform

The RADAR-IoT framework supports various destinations for consumed data. One of the core one is [RADAR-Base platform](https://radar-base.org). 
This contains the core services inherent to the RADAR-base platform to mock the platform for easy local development and testing.

### Usage

Just start the stack using - 
```shell script
docker-compose up -d
```

The following services will be exposed -

* Rest Proxy - Port 8082
* Schema Registry - Port 8083
* Gateway - Port 8090
* Management Portal - Port 8081

An OAuth client with Id `radar_iot` and secret `secret` is also created in Management portal for easy testing of authorization.