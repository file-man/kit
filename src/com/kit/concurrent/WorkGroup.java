package com.kit.concurrent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.kit.utils.Log;
import com.kit.utils.Utils;

public class WorkGroup {

	private ArrayBlockingQueue<Worker> mAvailable;
	private ArrayList<Worker> mWorking;
	private int mIndex;
	private Worker mInternalWorker;
	private boolean mIsMonitorOn;

	public static final class LogTag {
		public static final String WORKGROUP_MONITOR = "workgroup.monitor";
	}

	public interface WorkerStatusListener {
		void OnTaskFinished(Worker worker);

		void OnWorkerFinished(Worker worker);
	}

	public static final class Config {
		public static final String WORKGROUP_MONITOR = "workgroup.monitor";
	}

	public static final void enableMonitorLog() {
		Log.addTag(LogTag.WORKGROUP_MONITOR);
	}

	public void quit() {
		mInternalWorker.addTask(new Task() {
			@Override
			public void run() {
				Log.print(Log.Tag.DEBUG, "quit, available worker size = " + mAvailable.size());
				for (Worker worker : mAvailable) {
					worker.quit();
				}

				Log.print(Log.Tag.DEBUG, "quit, working worker size = " + mWorking.size());
				for (Worker worker : mWorking) {
					worker.quit();
				}

				if (mInternalWorker != null) {
					mInternalWorker.quit();
				}
			}
		});
	}

	public Worker getWorker(String name, int timeout) {
		try {
			Worker worker = mAvailable.poll(timeout, TimeUnit.MILLISECONDS);
			if (worker != null) {
				enqueue(worker);
			}
			
			worker.setName(name);
			return worker;
		} catch (InterruptedException e) {
			Log.error(this.getClass().getName(), e);
		}
		
		return null;
	}

	public Worker getWorker(String name) {
		Worker worker = null;
		while (worker == null) {
			try {
				worker = mAvailable.take();
			} catch (InterruptedException e) {
				Log.error(this.getClass().getName(), e);
				continue;
			}
		}

		worker.setName(name);
		enqueue(worker);

		return worker;
	}

	private void enqueue(Worker worker) {
		final Worker fWorker = worker;
		mInternalWorker.addTask(new Task() {
			@Override
			public void run() {
				if (!mWorking.contains(fWorker)) {
					mWorking.add(fWorker);
				}
			}
		});
	}

	public WorkerStatusListener mWorkerStatusListener = new WorkerStatusListener() {
		@Override
		public void OnTaskFinished(final Worker worker) {
			mInternalWorker.addTask(new Task() {
				@Override
				public void run() {
					Log.print(Log.Tag.DEBUG, worker.getName() + " task finished");
					mWorking.remove(worker);
					if (!mAvailable.contains(worker)) {
						mAvailable.add(worker);
					}
				}
			});
		}

		@Override
		public void OnWorkerFinished(final Worker worker) {
			mInternalWorker.addTask(new Task() {
				@Override
				public void run() {
					mAvailable.remove(worker);
					mWorking.remove(worker);
				}
			});
		}
	};

	public WorkGroup(int size) {
		if (size < 1) {
			throw new RuntimeException("size must greater than 0");
		}
		mAvailable = new ArrayBlockingQueue<Worker>(size, true);
		mWorking = new ArrayList<Worker>(size);
		for (int i = 0; i < size; i++) {
			mAvailable.add(new Worker("Worker " + mIndex++, mWorkerStatusListener));
		}
		mInternalWorker = new Worker("WorkGroup", size * 2 + 100);
//		Configuration.addConfigChangeListener(new OnConfigChangeListener() {
//
//			@Override
//			public void onConfigChange() {
//				System.out.println("group 7");
//				if (Configuration.getBooleanConfig(Config.WORKGROUP_MONITOR)) {
//					System.out.println("group 8");
//					if (!mIsMonitorOn) {
//						printWorkerInfo();
//						System.out.println("group 9");
//						mInternalWorker.addTask(getTimerTask(5000));
//						System.out.println("group 10");
//					}
//				} else {
//					mIsMonitorOn = false;
//				}
//				System.out.println("group 11");
//			}
//		});
	}

	private void printWorkerInfo() {
		Log.print(Log.Tag.MONITOR, "working thread:\t" + mWorking.size());
		Log.print(Log.Tag.MONITOR, "available thread:\t" + mAvailable.size());
	}

	private Task getTimerTask(int delay) {
		return new Task() {
			@Override
			public void run() {
				Utils.sleep(delay);
			}
		};
	}

	public static void stat() {
		final ThreadMXBean threadManagement = ManagementFactory.getThreadMXBean();
		int count = threadManagement.getThreadCount();
		Log.error("Test", "thread count = " + count);

		ThreadInfo[] responses = threadManagement.dumpAllThreads(false, false);
		for (ThreadInfo info : responses) {
			Log.error("Test", info.toString());
		}

		Log.error("Test", ManagementFactory.getThreadMXBean().getTotalStartedThreadCount() + "");
	}

	public static final void main(String[] args) {
		stat();
	}
}