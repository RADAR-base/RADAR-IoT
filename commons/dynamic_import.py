import importlib


class DynamicImporter:
    def __init__(self, module_name, class_name, *args):
        module = importlib.import_module(module_name)
        class_ = getattr(module, class_name)
        self.instance = class_(*args)
