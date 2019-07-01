# RADAR-base IoT framework


This is to be deployed on IoT devices (like raspberry pi ) and allows for capturing sensor data and sending it to the RADAR-base platform backend.

## Usage

### Configuration
The template for configuration is located at `config.yaml.template`. Copy this to the `config.yaml` and modify as required.

Note that `config.yaml` is validated against the json spec at `configspec.json`

### Installation
TODO
-

### Docker
TODO
-

## Extending

The framework is very decoupled and it should be easy to extend different components.
In particular, there are 3 major components in the framework - 

- The sensor module
- The pub/sub module
- The Data uploader or data consumer module.

### Extending the sensor module
- Sensor module can be extended by adding new sensors by extending the `sensor.Sensor` abstract base class (ABC) and implementing the appropriate abstract functions.
- After creating the subclass, you just need to add it to the `config.yaml` file as follows - 
    ```yaml
      - name: "your-sensor-name"
        # Name of your python module which contains the sensor class
        module: "sensors.your_module" 
        # Name of the class of the sensor in the module
        class: "YourSensorClass"
        # topic to publish the data to in pub/sub
        publishing_topic: "your-sensor-topic" 
        # polling frequency in milliseconds 
        poll_frequency_ms: 1000 
        # Flush size for flushing the records
        flush_size: 100
        # Flush after seconds if flush size is not reached
        flush_after_s: 1000
    ```

For an example, take a look at various sensors in the `sensors` package.

### Extending the Pub/Sub module
TODO
-

### Extending the Data Upload module
TODO
-
