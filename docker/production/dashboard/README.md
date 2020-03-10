## Grafana Dashboard using Influx DB

The RADAR-IoT framework supports various destinations for consumed data. One of them is InfluxDb. 
This contains the docker-compose file for deployment of InfluxDB and Grafana for Production environments. 
Thus, a webserver is also included as a reverse-proxy to avoid the need to open ports on a production environment.

### Usage

Configure the properties of the deployment -
1. Copy the `etc/env.template` file to `.env`
2. Edit the values as desired. Note to specify the `GRAFANA_SERVER_ROOT_URL` as `<your-root-url>/grafana/`.

Run the following command to start the services -

```shell script
docker-compose up -d
```

Grafana will be available on `<root-url>/grafana/` with the username and password specified in the `.env` file.
InfluxDB will be available on `<root-url>/influxdb` with the username and password specified in the `.env` file.