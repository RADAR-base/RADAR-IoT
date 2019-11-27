# RADAR-base IoT framework


The lightweight, flexible, configurable and highly extensible framework for IoT devices (like raspberry pi ) that allows for capturing sensor data (and potentially other devices) and consuming it in different ways including sending it to the [RADAR-base platform](https://github.com/RADAR-base/RADAR-Docker) backend.
The framework is highly decoupled and extensible. There are 4 major components in the framework - 

* Sensors
* Converters
* Publishers
* Data Consumers

The only external dependency is a pub/sub broker or messaging queue that can be easily run using docker images either on the IoT device or elsewhere(like the cloud).

Here is it's architecture. The data flow is from left to right. To know more about each component please take a look at the [Configuration section](#configuration)

```
                                                                                                                  .───────────.
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                        (             )
                                                                                                                 │`───────────'│
│                                    Device Handlers                                    │                         Configuration│
                                                                                                                 │             │
│                                                                                       │                        │.───────────.│
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                         (             )
                                            ▲                                                                     `───────────'
                         ┌──────────────────┴──────────────────────────┐
                         │                                             │
┌────────────────────────────────────────────────┐   ┌──────────────────────────────────┐  ┌──────────────────────────────────┐  ┌────────────────────────────────────────────┐  ┌─────┐   ┌──────────────────────────────────────────────────────────────────────┐
│                                                │   │                                  │  │                                  │  │                                            │  │     │   │                                                                      │
│                 Sensor Handler                 │   │        Other Handlers...         │  │            Converter             │  │                 Publisher                  │  │     │   │                            Data Consumer                             │
│                                                │   │                                  │  │                                  │  │                                            │  │     │   │                                                                      │
└────────────────────────────────────────────────┘   └──────────────────────────────────┘  └──────────────────────────────────┘  └────────────────────────────────────────────┘  │     │   └──────────────────────────────────────────────────────────────────────┘
╔════════════════════════════════════════════════╗   ╔══════════════════════════════════╗  ┌──────────────────────────────────┐  ┌────────────────────────────────────────────┐  │     │    ╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   ╳ ┌──────────────────────────────────────────────────────────────────┐ ╳
║                                                ║   ║                                  ║  │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │     │   ╳ │  Essentially the data consumer can be any application or system  │ ╳
║                                                ║   ║                                  ║  │                                │ │  │                                          │ │  │     │   ╳ │that can subscribe to topics/channels on the pub/sub system. This │ ╳
║                                                ║   ║                                  ║  │ │      Message Converter         │  │ │                                          │  │     │   ╳ │  makes it language agnostic and helps connect external systems.  │ ╳
║ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  ║   ║    ┌───────────────────────┐     ║  │     Abstract Base class for    │ │  │                 Publisher                │ │  │     │   ╳ └──────────────────────────────────────────────────────────────────┘ ╳
║                    Sensor                      ║   ║    │                       │     ║  │ │ serialisation and validation   │  │ │   Abstract Base class for publishing     │  │     │   ╳ ┌──────────────────────────────────────────────────────────────────┐ ╳
║ │Abstract Base class. Has implementation for│  ║   ║    │                       │     ║  │             of data.           │ │  │  records to the pub/sub broker or system │ │  │     │   ╳ │    Here we provide a general model which should be used when     │ ╳
║   polling and flushing the data. The actual    ║   ║    │                       │     ║  │ │                                │  │ │                                          │  │     │   ╳ │     developing such data consumer to provide flexibility and     │ ╳
║ │    data is provided by the subclasses     │  ║   ║    │                       │     ║  │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │  │                                          │ │  │     │   ╳ │    extensibility in line with the other parts of the system.     │ ╳
║                                                ║   ║    │                       │     ║  │                 ▲                │  │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │     │   ╳ └──────────────────────────────────────────────────────────────────┘ ╳
║ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  ║   ║    │   Handler Specific    │     ║  │        extends──┴──extends┐      │  │                      ▲                     │  │  P  │    ╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳╳
║                       ▲                        ║   ║    │    Implementations    │     ║  │         │                 │      │  │       ┌extends─────┬─┴──────extends─┐      │  │  u  │   ┌──────────────────────────────────────────────────────────────────────┐
║       ┌────extends───┬┴─────extends────┐       ║   ║    │                       │     ║  │         │                 │      │  │       │            │                │      │  │  b  │   │ ┌──────────────────────────────────────────────────────────────────┐ │
║       │              │                 │       ║   ║    │                       │     ║  │┌────────────────┐   ┌───────────┐│  │       │            │                │      │  │  l  │   │ │                           Data Handler                           │ │
║       │              │                 │       ║   ║    │                       │     ║  ││                │   │           ││  │  ┌─────────┐  ┌─────────┐      ┌─────────┐ │  │  i  │   │ │                                                                  │ │
║┌─────────────┐┌─────────────┐   ┌─────────────┐║   ║    │                       │     ║  ││                │   │   Other   ││  │  │         │  │         │      │         │ │  │  s  │   │ └──────────────────────────────────────────────────────────────────┘ │
║│             ││             │   │             │║   ║    │                       │     ║  ││ AvroConverter  │   │ Converter ││  │  │  Redis  │  │  MQTT   │      │  Other  │ │  │  h  │   │   ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      │
║│  Sensor 1   ││  Sensor 2   │   │  Sensor N   │║   ║    └───────────────────────┘     ║  ││                │   │           ││  │  │Publisher│  │Publisher│      │Publisher│ │  │  e  │   │                                                                │     │
║│fun get_data ││fun get_data │   │fun get_data │║   ║                                  ║  ││                │   │           ││  │  │         │  │         │..... │         │ │  │  r  │   │   │                                                                  │
║│             ││             │   │             │║   ║                                  ║  │└────────────────┘   └───────────┘│  │  └─────────┘  └─────────┘      └─────────┘ │  │  /  │   │                           Data Consumer                        │     │
║└─────────────┘└─────────────┘   └─────────────┘║   ║                                  ║  │         △                 △      │  │       △            △                △      │  │  S  │   │   │   Abstract base class for consuming the data. The actual    ◁─┐  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  u  │   │       processing of the consumed data is done by subclasses    │  │  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  b  │   │   │                                                               │  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  s  │   │                                                                │  │  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  c  │   │   └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   │  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  r  │   │                                  ▲                                │  │
║                                                ║   ║                                  ║  │         │                 │      │  │       │            │                │      │  │  i  │   │          ┌──extends───────┬──────┴──extends────────┐              │  │
║                                                ║   ║                                  ║  │         └───────┬─────────┘      │  │       │            │                │      │  │  b  │   │          │                │                        │              │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │  e  │   │    ┌───────────┐  ┌──────────────┐         ┌──────────────┐       │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │     │   │    │           │  │              │         │              │       │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │  b  │   │    │Radar Data │  │Visualisation │         │  Mobile App  │       │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │  r  │   │    │ Consumer  │  │Data Consumer │         │   Consumer   │       │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │  o  │   │    │           │  │              │  ...... │              │       │  │
║                                                ║   ║                                  ║  │                 │                │  │       │            │                │      │  │  k  │   │    └───────────┘  └──────────────┘         └──────────────┘       │  │
║                                                ║   ║                                  ║  │                 │                │  │       △            △                △      │  │  e  │   │          ▣                ▣                        ▣              │  │
║                                                ║   ║                                  ║  │                 △                │  │ ┌──────────┐ ┌──────────┐     ┌───────────┐│  │  r  │   │          └────────────────┴────┬───────────────────┘              │  │
║                                                ║   ║                                  ║  │     ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      │  │ │          │ │          │     │           ││  │     │   │                                │                                  │  │
║                                                ║   ║                                  ║  │                            │     │  │ │  Redis   │ │   MQTT   │     │   Other   ││  │  o  │   │                                ◎                                  │  │
║                                                ║   ║                                  ║  │     │                            │  │ │Connection│ │Connection│     │Connection ││  │  r  │   │         .─────────────────────────────────────────────.           │  │
║                                                ║   ║                                  ║  │         Schema Retriever   │     │  │ │          │ │          │.....│           ││  │     │   │        (               External Services               )          │  │
║                                                ║   ║                                  ║  │     │                            │  │ └──────────┘ └──────────┘     └───────────┘│  │  s  │   │         `─────────────────────────────────────────────'           │  │
║                                                ║   ║                                  ║  │                            │     │  │       │            │                │      │  │  y  │   │                                                                   │  │
║                                                ║   ║                                  ║  │     │                            │  │       │            │                │      │  │  s  │   │                                                                   │  │
║                                                ║   ║                                  ║  │      ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘     │  │       └─extends────┴─┬────extends───┘      │  │  t  │   │                                                                   │  │
║                                                ║   ║                                  ║  │                 ▲                │  │                      │                     │  │  e  │   │              ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                │  │
║                                                ║   ║                                  ║  │      ┌─extends──┴┬───extends┐    │  │                      ▼                     │  │  m  │   │                                                   │               │  │
║                                                ║   ║                                  ║  │      │           │          │    │  │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │     │   │              │                                                    │  │
║                                                ║   ║                                  ║  │ ┌────────┐  ┌────────┐ ┌────────┐│  │                                          │ │  │     │   │                            Subscriber             │               │  │
║                                                ║   ║                                  ║  │ │        │  │        │ │        ││  │ │                                          │  │     │   │              │Abstract base class for subscribing  ▷──────────────┘  │
║                                                ║   ║                                  ║  │ │        │  │        │ │        ││  │                 Connection               │ │  │     │   │                      to the pub/sub system        │                  │
║                                                ║   ║                                  ║  │ │  File  │  │  URL   │ │ Schema ││  │ │ Represents a connection to the pub/sub   │  │     │   │              │                                                       │
║                                                ║   ║                                  ║  │ │ System │  │        │ │Registry││  │                   system                 │ │  │     │   │                                                   │                  │
║                                                ║   ║                                  ║  │ │        │  │        │ │        ││  │ │                                          │  │     │   │              └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                   │
║                                                ║   ║                                  ║  │ │        │  │        │ │        ││  │                                          │ │  │     │   │                                 ▲                                    │
║                                                ║   ║                                  ║  │ └────────┘  └────────┘ └────────┘│  │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │     │   │                ┌──extends───────┴──┬─────extends────┐                │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │                │                   │                │                │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │          ┌──────────┐        ┌──────────┐     ┌──────────┐           │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │          │  Redis   │        │   MQTT   │     │  Other   │           │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │          │Subscriber│        │Subscriber│     │Subscriber│           │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │          └──────────┘        └──────────┘     └──────────┘           │
║                                                ║   ║                                  ║  │                                  │  │                                            │  │     │   │                                                                      │
╚════════════════════════════════════════════════╝   ╚══════════════════════════════════╝  └──────────────────────────────────┘  └────────────────────────────────────────────┘  └─────┘   └──────────────────────────────────────────────────────────────────────┘
```

## Usage

### Configuration
The template for configuration is located at [config.yaml.template](config.yaml.template). Copy this to the `config.yaml` and modify as required.

Currently, the configuration can be divided in to 4 main components. Each of the components has some **sensible defaults** but it is recommended to understand this section thoroughly.

1. **Sensors**: Represented by the key `sensors` in the config file consists of an array of sensor configurations.
Each sensor is configured as follows - 
    ```yaml
     -  name: "your-sensor-name"
        # Name of your python module which contains the sensor class
        module: "your_package.your_module" 
        # Name of the class of the sensor in the module
        class: "YourSensorClass"
        # topic/channel to publish the data to in pub/sub paradigm
        publishing_topic: "your-sensor-topic" 
        # polling frequency in milliseconds 
        poll_frequency_ms: 1000 
        # Flush size for flushing the records
        flush_size: 100
        # Flush after [value] seconds if flush size is not reached
        flush_after_s: 1000
    ```
    Currently, implementations for the following sensors are provided -
    - [Google Coral Environment Board Sensors](https://coral.withgoogle.com/products/environmental/): In the module - [coral_enviro](sensors/coral_enviro.py)
    
    By **Default** , No Sensors are added to the Configuration. This is because it is hardware dependent and thus we cannot have a default sensor config. If sensor config is not provided, the program will fail with an exception.

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
              extension: '.avsc'
    ```
    
    - If `validate_only` is `True`, then no conversion of the message is performed. It is only validated against the schema.
    - The **Default** value of the converter is `None`, so the messages are neither serialised nor validated unless explicitly configured.
    
    1. **Schema Retrievers**: These are used for retrieving schemas to be used for validation and serialisation.

        * Currently, support for [Avro](https://avro.apache.org/) is provided out of the box with schema retrievers from Filesystem, URL or [Confluent Schema Registry](https://www.confluent.io/confluent-schema-registry/) (Which is a part of the RADAR-base platform).
        * Each schema retriever is has its own set of required arguments. These can be specified using the `args` key under `schema_retriever`. For example, the `FileAvroSchemaRetriever` needs a base path where all schemas are stored in the filesystem(`filepath`) and an extension of the files(`extension`) as shown in the above example
        * Right now, the name of the schema is taken from the sensor name as can be seen in class `SensorBasedSchemaNamingStrategy` in the [commons.schema](commons/schema.py) module.
        * For more information take look at the [Schema Retriever section](#schema-retrievers)

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
    - The above mentioned values are the **defaults**. Only add these to the config file if need to update. 
    - The `expose_config_endpoint`, if set to `True`, exposes an http endpoint for getting the config of the system. Could be useful if other systems need to use this config.
    - `scheduler_max_threads` is the max workers for the [ThreadPoolExecutor](https://docs.python.org/3/library/concurrent.futures.html#concurrent.futures.ThreadPoolExecutor) used by the scheduler for polling sensor data. Increasing this may be beneficial if using a large number of sensors.
    - The `root_logger_level` define the log level for the `root` logger which is used across the whole application.
#### Other means of Configuration
Other than the defaults and the `config.yaml` file, The framework also supports configuration using the OS Environment Variables. For using these, the keys used in the above config files needs to be prefixed with `radar_iot_`. This is done to ensure the names never conflict with other environment variables.
For example, to set the `root_logger_level` value using environment variables, use - 
```bash
radar_iot_root_logger_level=INFO
```

##### Precedence of the Configurations
With so many config options, it's important to clarify the order in which the configuration is read.
It is as follows-
- First the Default Config is loaded.
- This is overwritten with matching keys from the OS environment variables.
- This is further overwritten with values from the `config.yaml` file.

So in descending order of precedence,
```bash
config.yaml > OS environment vars > default
```

#### Validation of Configuration
The Configuration is validated against the [json-schema](https://json-schema.org/) spec at [configspec](configspec.json)


### Installation
TODO
-

### Docker
TODO
-

## Extending

The framework is decoupled and it is easy to extend different components.
In particular, there are 4 major components in the framework that can be easily extended - 

- The sensor module
- The publisher module
- The Message Converter (Serialisation) module
- The Data uploader or data consumer module.

### Requirements

- Basic knowledge of IoT devices like Raspberry Pi and their hardware and interfacing like sensors.
- Basic knowledge of Python and docker.
- Optional but a basic knowledge about the RADAR-base platform and its components and client apps.

Depending on which part of the framework is being extended, the requirements will vary.


### Extending the sensor module
- Sensor module can be extended by adding new sensors by extending the `sensor.Sensor` abstract base class (ABC) and implementing the appropriate abstract functions.
- After creating the subclass, you just need to add it to the `config.yaml` file as specified in the [Configuration](#configuration) section above.

For example, following is implementation of a test sensor. You just need to specify the `get_data()` method and the rest will be taken care by the framework.

```python
import logging

from sensors import Sensor

logger = logging.getLogger('root')


class YourTestSensor(Sensor):
    def __init__(self, name, topic, poll_freq_ms, flush_size, flush_after_s):
        super().__init__(name, topic, poll_freq_ms, flush_size, flush_after_s)

    def get_data(self):
        logger.debug('test data')
        # Your logic for getting data from the sensor
        return 35.7
```
Remember to pass all the required constructor values to the super class.
Also notice the use of the root logger which was discussed earlier in the [configuration](#configuration) section.

Additionally, you can also extend other methods of sensor used for polling, flushing, etc. These can be found in the super class [Sensor](sensors/__init__.py)

For already available sensor implementations, take a look at various sensors in the [sensors](/sensors) package.

### Extending the Publisher module
This can be extended by extending the `Publisher` abstract class in [pubsub.publisher](pubsub/__init__.py) module.
You will need to provide implementation of the `_publish` method which will handle all the logic of publishing the messages. Ideally these should also convert the messages before publishing. 
Take a look at `RedisPublisher` in [pubsub.redis_publisher](pubsub/redis_publisher.py) module for an example implementation.

### Extending the Message Converter module
Just extend the `MessageConverter` abstract class in [commons.message_converter](commons/message_converter.py) module.
You will need to provide implementation for `convert` (for a single message) and `convert_all` (for a list of messages) methods.
Take a look at `AvroValidatedJsonConverter` in the same module for an example implementation.

### Extending the Data Upload module
Please read the data uploader module [documentation](/data/kotlin/data-uploader). This is currently provided in Kotlin programming language but can be created in any language desired (which has clients for subscribing to the pub/sub system).


## Additional Info

### Schema Retrievers

There are 3 types of schema retrievers provided which are located in the [schema](/commons/schema.py) module -

* **FileAvroSchemaRetriever**: This will retrieve the schemas from the local filesystem. It can configured as follows-
    ```yaml
          schema_retriever:
            module: 'commons.schema'
            class: 'FileAvroSchemaRetriever'
            args:
              filepath: '/etc/schemas/avro/sensors'
              extension: '.avsc'
    ```
    This will walk down the path in the `filepath` specified and load all the files with the `extension` provided.
    The example Avro schemas are located in the [/etc/schemas/avro/sensors](/etc/schemas/avro/sensors)
    
* **GithubAvroSchemaRetriever**: This will retrieve the schemas from a Github Repository. It can configured as follows-
    ```yaml
          schema_retriever:
            module: 'commons.schema'
            class: 'GithubAvroSchemaRetriever'
            args:
              repo_owner: 'RADAR-base'
              repo_name: 'RADAR-Schemas'
              branch: 'sensors'
              basepath: 'commons/iot/sensor'
              extension: '.avsc'
              git_user: 'username'
              git_password: '*******'
    ```
    Like the file retriever, this will also walk down the `basepath` in the repository and retrieve schemas with the `extension` provided. 
    If the repository is public there is no requirement to specify the `git_user` and `git_password` but it is still recommended as it increases the Github Api limits.
    
* **SchemaRegistrySchemaRetriever**: This will retrieve schemas from the Confluent Schema Registry. It can be configured as follows. The `schema_registry_url` is a required argument.
    ```yaml
          schema_retriever:
            module: 'commons.schema'
            class: 'SchemaRegistrySchemaRetriever'
            args:
              schema_registry_url: 'https://radar-cns-platform.rosalind.kcl.ac.uk/schema'
    ```