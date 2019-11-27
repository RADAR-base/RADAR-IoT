from typing import List


class Error(object):
    """Object representing an error. This must be used across the system for reporting any errors."""

    def __init__(self, type: str, code: int, reason: str, trace: str):
        self.type = type
        self.code = code
        self.reason = reason
        self.trace = trace


class Response(object):
    """Object representing a Response. A response consists of a response dictionary and a list of errors, if any.
    This must be used for any responses for example, when returning a measurement from the sensors."""

    def __init__(self, response: dict, errors: [List[Error], None]):
        self.response = response
        self.errors = errors
