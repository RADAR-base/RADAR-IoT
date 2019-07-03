from commons.dynamic_import import DynamicImporter
from sensors.sensor import Sensor
import logging
from config import Configuration
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.executors.pool import ProcessPoolExecutor
import apscheduler.events as aps_events
from concurrent.futures import ThreadPoolExecutor
from commons.message_converter import MessageConverter
from pubsub.publisher import Publisher, Connection

logger = logging.getLogger('root')

MODULE_KEY = 'module'
CLASS_KEY = 'class'
TOPIC_KEY = 'publishing_topic'
POLL_FREQUENCY_KEY = 'poll_frequency_ms'
FLUSH_SIZE_KEY = 'flush_size'
FLUSH_AFTER_S_KEY = 'flush_after_s'


class SensorHandler:
    sensors: [Sensor] = list()
    scheduler = BackgroundScheduler()

    publishing_thread_pool: ThreadPoolExecutor = None

    def __init__(self, config: Configuration):
        self.publishing_thread_pool = ThreadPoolExecutor(max_workers=config.get_publisher()['publisher_max_threads'])
        conn: Connection = DynamicImporter(config.get_publisher()['connection'][MODULE_KEY],
                                           config.get_publisher()['connection'][CLASS_KEY],
                                           config.get_publisher()['connection']['host'],
                                           config.get_publisher()['connection']['port'],
                                           config.get_publisher()['connection']['user'],
                                           config.get_publisher()['connection']['password']).instance
        publisher: Publisher = DynamicImporter(config.get_publisher()[MODULE_KEY], config.get_publisher()[CLASS_KEY],
                                               conn, self.publishing_thread_pool).instance

        self.converter: MessageConverter = None
        self.initialise_converter(config)

        for sensor in config.get_sensors():
            self.sensors.append(
                DynamicImporter(sensor[MODULE_KEY], sensor[CLASS_KEY], sensor['name'], sensor[TOPIC_KEY],
                                sensor[POLL_FREQUENCY_KEY], sensor[FLUSH_SIZE_KEY], sensor[FLUSH_AFTER_S_KEY],
                                publisher, self.converter).instance)

        # We don't use ProcessPool right now for the jobs but may include in the future for compute intensive tasks.
        executors = {
            'default': {'type': 'threadpool', 'max_workers': config.get_scheduler_max_threads()},
            'processpool': ProcessPoolExecutor(max_workers=5)
        }
        self.scheduler.configure(executors=executors)
        logger.info(f'Successfully initialised {self.__class__.__name__} and {publisher.__class__.__name__}')
        logger.info('Now starting data capture...')
        self.start_data_capture()
        self.scheduler.print_jobs()

    def start_data_capture(self):
        self.scheduler.start()
        self.scheduler.add_listener(self.job_listener)
        # start polling the sensors using threads from the scheduler default thread pool.
        for x in self.sensors:
            self.scheduler.add_job(x.poll, 'interval', seconds=x.poll_freq_ms / 1000, name=f'{x.topic}-poller')
        try:
            while True:
                pass
        except KeyboardInterrupt:
            logging.warning(f'The process {self.__class__.__name__} was interrupted. Gracefully shutting down...')
        finally:
            self._graceful_stop()

    def get_topics(self):
        return [x.topic for x in self.sensors]

    def _graceful_stop(self):
        if self.scheduler.running:
            self.scheduler.shutdown(wait=True)
        for sensor in self.sensors:
            sensor.flush()
        self.publishing_thread_pool.shutdown(wait=True)

    def initialise_converter(self, config):
        converter_conf = config.get_converter()
        if converter_conf is not None:
            schema_retriever = DynamicImporter(converter_conf['schema_retriever'][MODULE_KEY],
                                               converter_conf['schema_retriever'][CLASS_KEY],
                                               kwargs=converter_conf['schema_retriever']['args']).instance
            self.converter = DynamicImporter(converter_conf[MODULE_KEY],
                                             converter_conf[CLASS_KEY], schema_retriever).instance

    @staticmethod
    def job_listener(event):
        # TODO maybe later add error events to an error topic/channel on pub/sub
        if event.code == aps_events.EVENT_JOB_ERROR:
            logging.warning(f'There was an Exception while running the job id {event.job_id} : {event.exception}')
        elif event.code == aps_events.EVENT_SCHEDULER_SHUTDOWN:
            logging.info('The scheduler has successfully shutdown.')
        elif event.code == aps_events.EVENT_JOB_MISSED:
            logging.warning(
                f'The Job with job id {event.job_id} was missed which was supposed to run at {event.scheduled_run_time}')
