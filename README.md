# RADAR-base IoT framework


This is supposed to be deployed on IoT devices (like raspberry pi ) and allows for capturing sensor data and sending it to the RADAR-base platform backend.
The framework is highly decoupled and extensible. Here is it's architecture - 

TODO
-

## Usage

### Configuration
The template for configuration is located at [config.yaml.template](config.yaml.template). Copy this to the `config.yaml` and modify as required.

Currently, there the configuration can be divided in to 3 main components. Each of the components has some sensible defaults but it is recommended to understand this section thoroughly.

1. **Sensors**: Represented by the key `sensors` in the config file consists of an array of sensor configurations.
Each sensor is configured as follows - 
    ```yaml
     -  name: "your-sensor-name"
        # Name of your python module which contains the sensor class
        module: "sensors.your_module" 
        # Name of the class of the sensor in the module
        class: "YourSensorClass"
        # topic to publish the data to in pub/sub paradigm
        publishing_topic: "your-sensor-topic" 
        # polling frequency in milliseconds 
        poll_frequency_ms: 1000 
        # Flush size for flushing the records
        flush_size: 100
        # Flush after seconds if flush size is not reached
        flush_after_s: 1000
    ```
    Currently, implementations for the following sensors are provided -
    - [Google Coral Environment Board Sensors](https://coral.withgoogle.com/products/environmental/): In the modules - [coral_enviro_humidity](sensors/coral_enviro_humidity.py), [coral_enviro_light](coral_enviro_light.py), [coral_enviro_temperature](coral_enviro_temperature.py)
    
    By default , No Sensors are added to the Configuration. This is because this has no value of running without any sensors and also it is hardware dependent and thus we cannot have a default sensor config. If sensor config is not provided, the program will fail with an exception.

2. **Converters**: Represented by the key `converter` in the config file. This is for Serialisation of the messages captured by the sensors and before publishing them. It comprises of the following fields - 

    ```yaml
      converter:
          name: 'avro'
          module: 'commons.message_converter'
          class: 'AvroConverter'
          validate_only: True
          schema_retriever:
            module: 'commons.schema'
            class: 'FileAvroSchemaRetriever'
            args:
              filepath: '/base/path/to/the/schemas'
              extension: '.avro'
    ```
    
    - If `validate_only` is `True`, then no conversion of the message is performed. It is only validated against the schema.
    - The **Default** value of the converter is `None`, so the messages are neither serialised nor validated unless explicitly configured.
    
    1. **Schema Retrievers**: These are used for retrieving schemas to be used for validation and serialisation.

        * Currently, support for [Avro](https://avro.apache.org/) is provided out of the box with schema retrievers from Filesystem, URL or [Confluent Schema Registry](https://www.confluent.io/confluent-schema-registry/) (Which is a part of the RADAR-base platform).
        * Each schema retriever is has its own set of required arguments. These can be specified using the `args` key under `schema_retriever`. For example, the `FileAvroSchemaRetriever` needs a base path where all schemas are stored in the filesystem(`filepath`) and an extension of the files(`extension`) as shown in the above example
        * Right now, the name of the schema is taken from the sensor name as can be seen in class `SensorBasedSchemaNamingStrategy` in the [commons.schema](commons/schema.py) module.

3. **Publisher**: This is for configuring the [pubsub](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) broker/messaging queue for sending the captured sensor data. This provides extensibility to the framework so that other services, devices, etc can also capture the data which are outside of this framework. Some examples include mobile applications (like [RADAR-base passive RMT](https://github.com/RADAR-base/radar-prmt-android)), language agnostic data consumers (like using your existing libraries in other language like java to consume the messages), etc. [MQTT](http://mqtt.org/) is a widely used pubsub protocol for IoT devices and frameworks. [Redis pubsub](https://redis.io/topics/pubsub) is another lightweight pub/sub system and [perform well](https://redis.io/topics/ARM) on ARM architectures(like Raspberry PI) too. Plus it provides [other features](https://redis.io/topics/introduction) which maybe useful in the future.
 Hence, there is an out of the box implementation for publishing the data to Redis Pub/Sub but can easily add MQTT(see the [Extending](#extending-the-pubsub-module) section below). It can configured as follows-
    ```yaml
    publisher:
      module: 'pubsub.redis_publisher'
      class: 'RedisPublisher'
      connection:
        module: 'pubsub.redis_connection'
        class: 'RedisConnection'
        host: 'localhost'
        port: '6379'
        password: ''
      publisher_max_threads: 5
    ```
    Most of the options are self-explanatory. The `publisher_max_threads` is the max workers for the [ThreadPoolExecutor](https://docs.python.org/3/library/concurrent.futures.html#concurrent.futures.ThreadPoolExecutor) used for publishing messages. If you have large number of sensors and experience slow performance, then increasing the number of Threads for publisher may help.
    
4. **Others**: Other config options include the following - 
    ```yaml
    expose_config_endpoint: True
    root_logger_level: INFO
    scheduler_max_threads: 10
    ```
    - The above mentioned values are the defaults. Only add these to the config file if need to update. 
    - The `expose_config_endpoint`, if set to `True`, exposes an http endpoint for getting the config of the system. Could be useful if other systems need to use this config.
    - `scheduler_max_threads` is the max workers for the [ThreadPoolExecutor](https://docs.python.org/3/library/concurrent.futures.html#concurrent.futures.ThreadPoolExecutor) used by the scheduler for polling sensor data. Increasing this may be beneficial if using a large number of sensors.

#### Other means of Configuration
Other than the defaults and the `config.yaml` file, The framework also supports configuration using the OS Environment Variables.

#### Validation of Configuration
The `config.yaml` file is validated against the [json-schema](https://json-schema.org/) spec at [configspec](configspec.json`)


### Installation
TODO
-

### Docker
TODO
-

## Extending

The framework is decoupled and it is easy to extend different components.
In particular, there are 3 major components in the framework - 

- The sensor module
- The pub/sub module
- The Message Converter (Serialisation) module
- The Data uploader or data consumer module.

### Extending the sensor module
- Sensor module can be extended by adding new sensors by extending the `sensor.Sensor` abstract base class (ABC) and implementing the appropriate abstract functions.
- After creating the subclass, you just need to add it to the `config.yaml` file as specified in the [Configuration](#configuration) section above.

For example,


For already available sensor implementations, take a look at various sensors in the [sensors](/sensors) package.

### Extending the Pub/Sub module
TODO
-

### Extending the Data Upload module
TODO
-
