## Grafana Dashboard using Influx DB

The RADAR-IoT framework supports various destinations for consumed data. One of them is InfluxDb. 
This contains the docker-compose file for deployment of InfluxDB and Grafana for development environments.

### Usage

Run the following command to start the services -

```shell script
docker-compose up -d
```

Grafana will be available on `localhost:3000` with username `admin` and password `radar`
InfluxDB will be available on `localhost:8086` with username `root` and password `root`