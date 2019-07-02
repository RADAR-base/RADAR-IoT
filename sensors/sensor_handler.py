from commons.dynamic_import import DynamicImporter
from sensors.sensor import Sensor
import logging
from config import Configuration
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.executors.pool import ProcessPoolExecutor
import apscheduler.events as aps_events
import concurrent
from pubsub.redis_publisher import RedisPublisher

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

    publishing_thread_pool: concurrent.futures.ThreadPoolExecutor = None

    def __init__(self, config: Configuration):
        self.publishing_thread_pool = concurrent.futures.ThreadPoolExecutor(max_workers=config.get_publisher_max_threads())
        publisher = RedisPublisher(publisher_thread_pool=self.publishing_thread_pool)

        for sensor in config.get_sensors():
            self.sensors.append(
                DynamicImporter(sensor[MODULE_KEY], sensor[CLASS_KEY], sensor[TOPIC_KEY], sensor[POLL_FREQUENCY_KEY],
                                sensor[FLUSH_SIZE_KEY], sensor[FLUSH_AFTER_S_KEY], publisher).instance)

        executors = {
            'default': {'type': 'threadpool', 'max_workers': 20},
            'processpool': ProcessPoolExecutor(max_workers=5)
        }
        self.scheduler.configure(executors=executors)
        logger.info(f'Successfully initialised {self.__class__.__name__}. Now starting data capture...')
        self.start_data_capture()
        self.scheduler.print_jobs()

    def start_data_capture(self):
        self.scheduler.start()
        self.scheduler.add_listener(self.job_listener)
        # start polling the sensors using threads from the scheduler thread pool.
        for x in self.sensors:
            self.scheduler.add_job(x.poll, 'interval', seconds=x.poll_freq_ms/1000, name=f'{x.topic}-poller')
        try:
            while True:
                pass
        except KeyboardInterrupt:
            logging.warning(f'The process {self.__class__.__name__} was interrupted. Gracefully shutting down...')
            if self.scheduler.running:
                self.scheduler.shutdown(wait=True)
            self.publishing_thread_pool.shutdown(wait=True)

    def get_topics(self):
        return [x.topic for x in self.sensors]

    @staticmethod
    def job_listener(event):
        if event.code == aps_events.EVENT_JOB_ERROR:
            logging.warning(f'There was an Exception while running the job id {event.job_id} : {event.exception}')
        elif event.code == aps_events.EVENT_SCHEDULER_SHUTDOWN:
            logging.info('The scheduler has successfully shutdown.')
        elif event.code == aps_events.EVENT_JOB_MISSED:
            logging.warning(f'The Job with job id {event.job_id} was missed which was supposed to run at {event.scheduled_run_time}')

