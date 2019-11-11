package com.data.kit.concurrent;

import java.util.concurrent.ArrayBlockingQueue;

import com.data.kit.concurrent.WorkGroup.WorkerStatusListener;
import com.data.kit.utils.Log;

public class Worker extends Thread {

	private int mQueueSize = 100;
	private ArrayBlockingQueue<Task> mTasks;
	private boolean mIsOffWork;
	private boolean mGetReady;
	private WorkerStatusListener mStatusListener;

	public Worker(String name) {
		this(name, 100, null);
	}

	public Worker(String name, WorkerStatusListener listener) {
		this(name, 100, listener);
	}

	public Worker(String name, int queueSize) {
		this(name, queueSize, null);
	}

	public Worker(String name, int queueSize, WorkerStatusListener listener) {
		mStatusListener = listener;
		setName(name);
		mQueueSize = queueSize > 0 ? queueSize : mQueueSize;
		setPriority(MAX_PRIORITY);
//		setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
//			
//			@Override
//			public void uncaughtException(Thread t, Throwable e) {
//				Log.error(this.getClass().getName(), e);
//				try {
//					throw e;
//				} catch (Throwable e1) {
//					e1.printStackTrace();
//				}
//			}
//		});
	}

	@Override
	public void run() {
		// Working loop;
		while (true) {
			// Doing the actual work;
			working();

			if (timeToOffWork()) {
				// Finally exit;
				if (mStatusListener != null) {
					mStatusListener.OnWorkerFinished(this);
				}
				Log.print("Debug", "off work, task size = " + mTasks.size());
				return;
			}
		}
	}

	private Task getTask() {
		Task task = null;
		while (task == null) {
			try {
				task = mTasks.take();
			} catch (InterruptedException e) {
				Log.error(this.getClass().getName(), e);
				continue;
			}
		}

		Log.print("Debug", "get task, new size = " + mTasks.size());
		return task;
	}

	private void working() {
		Task task = getTask();
		task.start();
		if (mStatusListener != null) {
			mStatusListener.OnTaskFinished(this);
		}
	}

	private boolean timeToOffWork() {
		return mIsOffWork && mTasks.isEmpty();
	}

	public void quit() {
		addTask(new Task() {
			@Override
			public void run() {
				mIsOffWork = true;
				Log.print("Debug", " bye");
			}
		});
	}

	public boolean isIdle() {
		return mTasks.isEmpty();
	}

	public void addTask(Task task) {
		startWork();
		try {
			mTasks.put(task);
			Log.print("Debug", "add task, new size = " + mTasks.size());
		} catch (InterruptedException e) {
			Log.error(this.getClass().getName(), e);
		}
	}

	private synchronized void startWork() {
		if (!mGetReady) {
			mGetReady = true;
			mTasks = new ArrayBlockingQueue<Task>(mQueueSize);
			start();
			Log.print("Debug", getName() + " thread started.");
		}
	}

	public static void main(String[] args) {

	}
}
