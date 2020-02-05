# DATA UPLOADER
This is an application for consuming data exposed by the pub/sub system in the [RADAR-IoT](https://github.com/RADAR-base/RADAR-IoT) framework and processing/uploading to to different destinations.
The application consumes data from channels in a pub/sub system and can consume it in various ways. One such implementation provided is uploading the data to the [RADAR-base platform's backend](https://github.com/RADAR-base/RADAR-Docker).

## Terminology
These are the explanation of the main terms used in the context of this application - 

1. `Handler` - This is used for Handling data coming in from the pub/sub system. This is also responsible for subscribing to the topics/channels of the pub/sub system. It usually forwards data to the `DataConsumer`s.
2. `Consumer` or `DataConsumer` - These are the components that handle the actual processing of the data that is received from the `Handler`s.
3. `Converter` - These are used to convert the message from one format to another. So that data can be read from the pub/sub system and sent/processed by the destination system(for DataConsumers). Hence these are specific to a particular sensor for a particular consumer. One implementation of a converter can be - deserializing a message received in JSON format from the pub/sub system and serializing it to AVRO format for uploading to kafka.
4. `Connection` - Connection represents a connection to a external entity (like pub/sub system or a destination entity like influxdb).

## Configuration
Configuration files can be added to classpath or it's location can be defined by the environment variable `RADAR_IOT_CONFIG_LOCATION`.
An example file can be found in [radar_iot_config.yaml](/radar_iot_config.yaml.template).

## Usage
This can be run as a normal jvm application by running the main class `org.radarbase.iot.DataUploaderApplication.kt`

## Docker

A [Dockerfile](../Dockerfile) is provided for convenience of deploying the application. A docker-compose file is also provided to enable the application and it's dependencies to be deployed together.
The docker-compose file is present [here](../docker/data-uploader.yml). 

You will first need to configure the application by copying the [radar_iot_config.yaml.template](../docker/etc/data-uploader/radar_iot_config.yaml.template) to [radar_iot_config.yaml](../docker/etc/data-uploader/radar_iot_config.yaml) and updating the configuration values.

You can then run the applications as `docker-compose -f docker/data-uploader.yml up -d`.

## Authorization
For the [Rest Proxy Data Consumer](/src/main/kotlin/org/radarbase/iot/consumer/RestProxyDataConsumer.kt), by default, authorization is enabled using [Management Portal](https://github.com/RADAR-base/ManagementPortal) and hence the uploader will work with [Gateway](https://github.com/RADAR-base/RADAR-Gateway) as well as [Rest Proxy](https://docs.confluent.io/current/kafka-rest/index.html)

## Contributing
For contributing, please take a look at the `commons` module and `data-uploader` module. Code should be formatted using the Kotlin Style Guide. If you want to contribute a feature or fix, browse our issues and please make a pull request.
For simple additions like new sensors, please take a look in the [converter](/src/main/kotlin/org/radarbase/iot/converter) package where you will need to add new Converters for your sensor based on the consumers.

### Extending
Various bits of the application can be extended - 

1. To extend the authorizer, look at extending the `Authorizer` interface in the `commons` module. Currently, only `ManagementPortalAuthorizer` is implemented.
2. To add another data consumer/processor (like [RestProxyDataConsumer](/src/main/kotlin/org/radarbase/iot/consumer/RestProxyDataConsumer.kt)), extend the [DataConsumer](/src/main/kotlin/org/radarbase/iot/consumer/DataConsumer.kt) abstract class and specify how you want to process the data. Also see [InfluxDb consumer](/src/main/kotlin/org/radarbase/iot/consumer/InfluxDbDataConsumer.kt).
3. To add a new type of data format for reading data from the pub/sub system (Currently json is supported via [JsonMessageParser](/src/main/kotlin/org/radarbase/iot/converter/messageparser/JsonMessageParser.kt) class), you can just implement the `Parser.kt` interface from the `util` package in `commons` module and then pass that in the [Converters](/src/main/kotlin/org/radarbase/iot/converter)
4. Currently, a Redis based handler for data is provided in [RedisDataHandler](/src/main/kotlin/org/radarbase/iot/handler/RedisDataHandler.kt), but other handlers can be added by implementing the [Handler](/src/main/kotlin/org/radarbase/iot/handler/Handler.kt) interface. This will then need to be added to the array of handlers in the [main class](/src/main/kotlin/org/radarbase/iot/DataUploaderApplication.kt).
5. The communication with the pub/sub system is handled using a [connection](/src/main/kotlin/org/radarbase/iot/pubsub/connection) and a [subscriber](/src/main/kotlin/org/radarbase/iot/pubsub/subscriber). Both of which can be be extended by implementing their respective interfaces.