# Deployments

This directory contains deployment stacks for production as well as development environments for each of the different components to be deployed.

Currently, there are 3 components-

1. edge - The services that will be deployed on an edge device.
2. dashboard(optional) - The services that are used for creating realtime dashboard. This includes influxdb and grafana. This should be on a backend server for production but can also be deployed locally on the edge device if there is no outside connectivity (for example this is usually the case with hospitals)
3. radar(optional) - The services that form the RADAR-base platform. These should be deployed on a backend server for production.

For more information, look at the `README` files inside each directory.