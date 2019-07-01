from abc import ABC, abstractmethod


class Subscriber(ABC):
    @abstractmethod
    def subscribe(self):
        pass
