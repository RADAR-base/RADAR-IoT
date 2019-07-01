from abc import ABC, abstractmethod


class Connection(ABC):

    host='localhost'
    port='8080'

    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.connect()

    @abstractmethod
    def connect(self) -> None:
        pass

    @abstractmethod
    def get_connection(self):
        pass

    @abstractmethod
    def is_connected(self) -> bool:
        pass

    @abstractmethod
    def get_connection_attributes(self):
        pass

