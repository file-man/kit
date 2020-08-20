package com.kit.flow;

import java.util.ArrayList;
import java.util.List;

import com.kit.concurrent.Queue;
import com.kit.concurrent.Task;
import com.kit.concurrent.WorkGroup;
import com.kit.concurrent.Worker;
import com.kit.config.Configuration;
import com.kit.utils.Log;
import com.kit.utils.Utils;

public abstract class Flow<T> {

	private Queue<T> mDataQueue;
	private List<DataHandler> mHandlerList;
	private String mName;
	private int mIndex;

	public static final class Config {
		public static final String FLOW_QUEUE_GET_DATA_TIMEOUT = "flow.queue.getdata.timeout";
		public static final String FLOW_QUEUE_SIZE = "flow.queue.size";
	}

	public static final class LogTag {
		public static final String FLOW_MONITOR = "flow.monitor";
	}

	public void addData(T in) {
		mDataQueue.add(in);
	}

	public void close() {
		setHandlerThread(0, null);
	}

	public static final void enableMonitorLog() {
		Log.addTag(LogTag.FLOW_MONITOR);
	}

	public Flow(String name, int queueSize) {
		mName = name;
		mDataQueue = new Queue<T>(name,
				queueSize > 0 ? queueSize : Configuration.getIntConfig(Config.FLOW_QUEUE_SIZE, 100));
		mHandlerList = new ArrayList<Flow<T>.DataHandler>();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Log.print(LogTag.FLOW_MONITOR, "data handler count =\t" + mHandlerList.size());
					Utils.sleep(1000);
				}
			}
		}, name + "-monitor").start();
	}

	public String getName() {
		return mName;
	}

	public void onDataHandleStart(DataHandler handler) {

	}
	public abstract void onDataHandle(T data);

	protected boolean onDataFinish() {
		// do nothing;

		return false;
	}

	public int getQueueSize() {
		return mDataQueue.size();
	}

	public synchronized void setHandlerThread(int size, WorkGroup group) {
		final int before = mHandlerList.size();
		final int diff = size - before;
		if (diff == 0) {
			return;
		} else if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				increaseHandlerThread(group.getWorker(getName() + "-thread-" + mIndex++));
			}
			Log.print(Log.Tag.MONITOR,
					mName + " increase " + diff + " threads, before = " + before + ", total = " + size);
		} else {
			decreaseHandlerThread(-diff);
			Log.print(Log.Tag.MONITOR,
					mName + " decrease " + diff + " threads, before = " + before + ", total = " + size);
		}
	}

	private synchronized void increaseHandlerThread(Worker worker) {
		final Flow<T>.DataHandler handler = new DataHandler();
		addHandler(handler);
		worker.setName(mName);
		worker.addTask(new Task() {
			@Override
			public void run() {
				handler.start();
				removeHandler(handler);
			}
		});
	}

	public synchronized void increaseHandlerThread(int increaseNum, WorkGroup group) {
		if (increaseNum > 0) {
			for (int i = 0; i < increaseNum; i++) {
				increaseHandlerThread(group.getWorker(mName + "-thread-" + mIndex++));
			}
		}
	}

	private synchronized void addHandler(DataHandler handler) {
		mHandlerList.add(handler);
	}

	private synchronized void removeHandler(DataHandler handler) {
		mHandlerList.remove(handler);
	}

	public synchronized void decreaseHandlerThread(int decreaseNum) {
		if (decreaseNum > mHandlerList.size()) {
			decreaseNum = mHandlerList.size();
		} else if (decreaseNum < 1) {
			return;
		}

		for (int i = 0; i < decreaseNum; i++) {
			mHandlerList.get(i).stop();
		}
	}

	public T getData() {
		return mDataQueue.get(Configuration.getIntConfig(Config.FLOW_QUEUE_GET_DATA_TIMEOUT, 500));
	}

	public final class DataHandler {
		private boolean stop = false;

		public void stop() {
			stop = true;
		}

		public void start() {
			onDataHandleStart(this);
			while (!stop) {
				T data = getData();
				if (data != null) {
					onDataHandle(data);
				} else {
					if (onDataFinish()) {
						stop = true;
					}
				}
			}
		}
	}

	public static void main(String[] args) {

	}
}