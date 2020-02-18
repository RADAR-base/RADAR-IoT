import logging


def setup_custom_logger(name, level):
    formatter = logging.Formatter(
        fmt='%(asctime)s - %(levelname)s - %(filename)s: %(module)s: %(funcName)s : %(lineno)d - %(message)s')

    handler = logging.StreamHandler()
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)
    return logger
