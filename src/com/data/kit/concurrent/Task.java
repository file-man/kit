package com.data.kit.concurrent;

import java.util.ArrayList;

public abstract class Task {

	private State mState;
	private ArrayList<TaskListener> mTaskListener;

	public class TaskListener {
		public void onStart() {
			// do nothing;
		}

		public void onFinish() {
			// do nothing;
		}
	}

	public enum State {
		PENDING, RUNNING, FINISHED
	}

	public Task() {
		mTaskListener = new ArrayList<Task.TaskListener>();
		mState = State.PENDING;
	}

	public void addTaskListener(TaskListener listener) {
		if (listener != null) {
			mTaskListener.add(listener);
		}
	}

	abstract public void run();

	/**
	 * The task processor.
	 */
	public synchronized final boolean start() {
		mState = State.RUNNING;

		for (TaskListener listener : mTaskListener) {
			listener.onStart();
		}

		run();
		mState = State.FINISHED;

		for (TaskListener listener : mTaskListener) {
			listener.onFinish();
		}

		return true;
	}

	public State getStatus() {
		return mState;
	}
}
