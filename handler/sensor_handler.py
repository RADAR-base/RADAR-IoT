import logging
from concurrent.futures import ThreadPoolExecutor

import apscheduler.events as aps_events
from apscheduler.executors.pool import ProcessPoolExecutor
from apscheduler.schedulers.background import BackgroundScheduler

from commons.message_converter import MessageConverter
from config import Configuration, ConfigHelper
from pubsub.publisher import Publisher
from sensors.sensor import Sensor

logger = logging.getLogger('root')


class SensorHandler:
    sensors: [Sensor] = list()
    scheduler = BackgroundScheduler()

    publishing_thread_pool: ThreadPoolExecutor = None

    def __init__(self, config: Configuration):
        self.publisher: Publisher = ConfigHelper.get_publisher()
        self.converter: MessageConverter = ConfigHelper.get_converter()
        self.sensors = ConfigHelper.get_sensors()

        # We don't use ProcessPool right now for the jobs but may include in the future for compute intensive tasks.
        executors = {
            'default': {'type': 'threadpool', 'max_workers': config.get_scheduler_max_threads()},
            'processpool': ProcessPoolExecutor(max_workers=5)
        }
        self.scheduler.configure(executors=executors)
        logger.info(f'Successfully initialised {self.__class__.__name__} and {self.publisher.__class__.__name__}')
        logger.info('Now starting data capture...')
        self.start_data_capture()
        self.scheduler.print_jobs()

    def start_data_capture(self):
        self.scheduler.start()
        self.scheduler.add_listener(self.job_listener)
        # start polling the sensors using threads from the scheduler default thread pool.
        for x in self.sensors:
            self.scheduler.add_job(x.poll, 'interval', seconds=x.poll_freq_ms / 1000, name=f'{x.topic}-poller')

    def get_topics(self):
        return [x.topic for x in self.sensors]

    def graceful_stop(self):
        if self.scheduler.running:
            self.scheduler.shutdown(wait=True)
        for sensor in self.sensors:
            sensor.flush()
        if self.publishing_thread_pool is not None:
            self.publishing_thread_pool.shutdown(wait=True)

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
