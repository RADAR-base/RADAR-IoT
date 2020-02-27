FROM arm32v7/alpine:3.11.3 AS builder
RUN apk add --no-cache curl=7.68.0
RUN curl -L https://github.com/balena-io/qemu/releases/download/v3.0.0%2Bresin/qemu-3.0.0+resin-arm.tar.gz | tar zxvf - -C . && mv qemu-3.0.0+resin-arm/qemu-arm-static .

FROM arm32v7/python:3.7-slim-buster
COPY --from=builder qemu-arm-static /usr/bin
ARG INSTALL_GROVE_PI=True

# Install required base libraries
RUN export DEBIAN_FRONTEND=noninteractive && \
        apt-get update && \
        apt-get install --no-install-recommends -y apt-utils nodejs gfortran gcc curl git sudo lsb-release build-essential && \
        apt-get install --no-install-recommends -y libi2c-dev i2c-tools libnode64:armhf libuv1:armhf libc-ares2:armhf libatlas-base-dev libatlas3-base libssl-dev libusb-0.1-4 libtinfo5 libreadline7:armhf libncurses5 libffi-dev libopenblas-base libopenblas-dev && \
        apt-get install --no-install-recommends -y python-setuptools python-pip python-dev cython python-numpy python-scipy python-rpi.gpio python-smbus python-serial && \
        apt-get install --no-install-recommends -y python3-setuptools python3-pip python3-dev cython3 python3-numpy python3-scipy python3-rpi.gpio python3-serial python3-smbus && \
       rm -rf /var/lib/apt/lists/*

# Download Wheel to install rpi.gpio for faster installation (avoids building from scratch)
RUN curl -o RPi.GPIO-0.7.0-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/rpi-gpio/RPi.GPIO-0.7.0-cp37-cp37m-linux_armv7l.whl#sha256=6a4791f41cafc2ee6e4cb70e5bd31fadc66a0cfab29b38df8723a98f6f73ad5a
RUN python3 -m pip install RPi.GPIO-0.7.0-cp37-cp37m-linux_armv7l.whl

# Download Wheel to install numpy for faster installation (avoids building from scratch)
RUN curl -o numpy-1.18.1-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/numpy/numpy-1.18.1-cp37-cp37m-linux_armv7l.whl#sha256=24817c750cbb59322d2fd5b1c5ddb444417c7ad86dfd0451b41ba299321198b6
RUN python3 -m pip install numpy-1.18.1-cp37-cp37m-linux_armv7l.whl

# Download Wheel to install scipy (no wheel for armv7 in debian by default)
RUN curl -o scipy-1.3.3-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/scipy/scipy-1.3.3-cp37-cp37m-linux_armv7l.whl#sha256=edda366fda13cfad10c3cf58341297f0ff1255020076a247ce50e594b42849d0
RUN python3 -m pip install scipy-1.3.3-cp37-cp37m-linux_armv7l.whl

RUN mkdir -p /home/pi/

# Create a new user called pi with sudo privileges (simulating raspbian)
RUN useradd -o -u 0 -g 0 -N -d /home/pi/ -M pi && echo "pi:pi" | chpasswd && adduser pi sudo
RUN echo "pi     ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
USER pi

# Copy source from the RADAR-IoT and add the working dir
COPY ./ /source/
WORKDIR /source/

# Install grove pi library
RUN [ ${INSTALL_GROVE_PI} = 'True' ] && bash scripts/install_grovepi.sh

# Install requirements for RADAR-IoT
RUN python3 -m pip install -r requirements.txt

# Run the program
CMD [ "python3", "main.py" ]