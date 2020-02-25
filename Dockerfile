FROM alpine AS builder
RUN apk add curl
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
RUN sudo python3 -m pip install RPi.GPIO-0.7.0-cp37-cp37m-linux_armv7l.whl

# Download Wheel to install numpy for faster installation (avoids building from scratch)
RUN curl -o numpy-1.18.1-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/numpy/numpy-1.18.1-cp37-cp37m-linux_armv7l.whl#sha256=24817c750cbb59322d2fd5b1c5ddb444417c7ad86dfd0451b41ba299321198b6
RUN sudo python3 -m pip install numpy-1.18.1-cp37-cp37m-linux_armv7l.whl

# Download Wheel to install scipy (no wheel for armv7 in debian by default)
RUN curl -o scipy-1.3.3-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/scipy/scipy-1.3.3-cp37-cp37m-linux_armv7l.whl#sha256=edda366fda13cfad10c3cf58341297f0ff1255020076a247ce50e594b42849d0
RUN sudo python3 -m pip install scipy-1.3.3-cp37-cp37m-linux_armv7l.whl

RUN curl -o anyconfig-0.9.9-py2.py3-none-any.whl https://www.piwheels.org/simple/anyconfig/anyconfig-0.9.9-py2.py3-none-any.whl#sha256=5edcf98a352d34b8ca2e540b5ce9e6f0beef92b3feb431c9e61570c6f857f57b
RUN sudo python3 -m pip install anyconfig-0.9.9-py2.py3-none-any.whl

RUN curl -o fastavro-0.22.10-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/fastavro/fastavro-0.22.10-cp37-cp37m-linux_armv7l.whl#sha256=11e4d2fa9fe8051310f753cf15dd039bb61d2bfdf5cb563f273b443da18cec39
RUN sudo python3 -m pip install fastavro-0.22.10-cp37-cp37m-linux_armv7l.whl

RUN curl -o cffi-1.14.0-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/cffi/cffi-1.14.0-cp37-cp37m-linux_armv7l.whl#sha256=886886958997f92ac3b478532cd36384965dcff2dacdcc3ff304c894a44a63b5
RUN sudo python3 -m pip install cffi-1.14.0-cp37-cp37m-linux_armv7l.whl

RUN curl -o cryptography-2.8-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/cryptography/cryptography-2.8-cp37-cp37m-linux_armv7l.whl#sha256=493031de65cec3a47cc0ab779693f25406932232ffe5cac707c65f99e09b5063
RUN sudo python3 -m pip install cryptography-2.8-cp37-cp37m-linux_armv7l.whl

RUN curl -o pyrsistent-0.15.7-cp37-cp37m-linux_armv7l.whl https://www.piwheels.org/simple/pyrsistent/pyrsistent-0.15.7-cp37-cp37m-linux_armv7l.whl#sha256=9a4120145e7ea863ff3d5eeb577ea76fa38f68b18e490bd177b363b139da7f39
RUN sudo python3 -m pip install pyrsistent-0.15.7-cp37-cp37m-linux_armv7l.whl

RUN sudo mkdir -p /home/pi/

# Create a new user called pi with sudo privileges (simulating raspbian)
RUN useradd -o -u 0 -g 0 -N -d /home/pi/ -M pi && echo "pi:pi" | chpasswd && adduser pi sudo
RUN echo "pi     ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
USER pi

# Copy source from the RADAR-IoT and add the working dir
COPY ./ source/
WORKDIR source/

# Install grove pi library
RUN [ ${INSTALL_GROVE_PI} = 'True' ] && bash scripts/install_grovepi.sh

# Install requirements for RADAR-IoT
RUN sudo python3 -m pip install -r requirements.txt

# Run the program
CMD [ "sudo", "python3", "main.py" ]