## Background:
The [RADAR-IoT](https://github.com/RADAR-base/RADAR-IoT) is a lightweight, flexible, configurable and highly extensible framework for IoT devices (like a raspberry pi)
that allows for capturing sensor data (and potentially other devices) and consuming it in different ways
including sending it to the [RADAR-base mHealth platform](https://radar-base.org/) backend. The gateway framework is highly decoupled and extensible. The architecture is shown below.


![RADAR-IoT Single device multi sensorSample flow](https://user-images.githubusercontent.com/11093544/154696409-2db70900-cd86-4af1-8b6f-e80f40890452.jpg)



Presently, the RADAR-base platform focuses on personal sensing, these devices are typically battery powered and carried on the user. 
Wearable devices available for integration are limited by the vendor availability of SDKs and REST-APIs, 
however, for static IoT sensors there is a very large range of sensor modalities and providers, a significant improvement to the RADAR-base platform
would enable the use of these sensors opening up a wide array of use cases within the health and other domains. 
A single platform to collect, and analyse in real-time, ambulatory personal data (phone active/passive RMT, wearable data) 
in parallel with static IoT sensor data provides a holistic 360° view of both personal and environmental state previously not possible.

The IoT gateway framework for RADAR-base differentiates itself from related work by being device, sensor and programming language agnostic, 
supporting all types of common IoT input-output protocols, being open-source, modular and easily extensible, providing support for multiple data sinks 
(like mHealth platform, on-device AI and ML, dashboard and more), interoperability, providing industry-leading security and medical level privacy, 
and providing integration to a well established open-source mHealth cloud platform for data collection, aggregation, 
transformation and heavyweight analytics with different types of data sources like wearables, IoT sensors, mobile apps, eCRFs.

The framework is currently in the Proof-Of-Concept (POC) stage and has been tested working in a staging environment. 
We want to finalise the framework and make it production-ready. 
This will involve working on core aspects of the framework like sensor states machines, M2M communication protocols and 
implementing advanced visibility into the framework.

## Goals:


| Goals                                                                                                   | Related Issues                                                                                                                                                             | Requirements                                                                                                                                                                                                                                            |
|---------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Extend support for industry-standard IoT protocols.                                                     | [#21](https://github.com/RADAR-base/RADAR-IoT/issues/21)                                                                                                                   | - Implement an MQTT producer and a consumer to capture and utilise sensor data. - Deploy an MQTT broker locally using docker and build and test the implementations using Mock sensor.                                                                  |
| Provide visibility and insight into the framework and sensors allowing for more robust fault isolation. | [#5](https://github.com/RADAR-base/RADAR-IoT/issues/5), [#8](https://github.com/RADAR-base/RADAR-IoT/issues/8)                                                             | - Add State machine to the sensor to capture and track sensor events lifecycle. - Publish device/sensor events and logs to pubsub system                                                                                                                |
| Making the framework easier to develop and deploy.                                                      | [#1](https://github.com/RADAR-base/RADAR-IoT/issues/1), [#14](https://github.com/RADAR-base/RADAR-IoT/issues/14), [#16](https://github.com/RADAR-base/RADAR-IoT/issues/16) | - Continuous Integration using Github Actions - Build and test the code, Build Docker images (on arm architectures) and push to Dockerhub - Improve Unit testing in the project - Make configurations needed for deployment easier and well documented. |
| Add more abstractions and extensions to the framework.                                                  | [#4](https://github.com/RADAR-base/RADAR-IoT/issues/4)                                                                                                                     | - Add a new abstraction layer in the form of DeviceHandlers to support new types of devices alongside traditional sensors. - Add support for new sensors                                                                                                |
| Make the framework production-ready and to be used in various research studies.                         | [#19](https://github.com/RADAR-base/RADAR-IoT/issues/19), [#17](https://github.com/RADAR-base/RADAR-IoT/issues/17)                                                         | - Improve the production deployment docker-compose stack. - Make minor updates and fix bugs reported in the issues.                                                                                                                                     |


Apart from the goals above, the following general tasks are expected:
- General understanding of the RADAR-IoT framework architecture
- Understand the association of classes and modules in the code with the components in the architecture.
- Build and run the RADAR-IoT framework with Mock Sensor on your local machine. 

## Skills: 
**Must have:**
IoT, Python

**Good-to-have:**
Kotlin/Java, Docker, Automation, I/O protocols like GPIO


