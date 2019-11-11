package com.data.kit.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.data.kit.utils.Log;
import com.data.kit.utils.Utils;

public class Queue<E> {

	private ArrayBlockingQueue<E> mQueue;
	private int mCapacity;
	private int mNotifyInterval = 1000;
	private String mName;

	public void levelNotify() {
		Log.print(Log.Tag.MONITOR, "queue-" + mName + ":\t" + getLevel() + "%");
	}

	public Queue(String name) {
		this(name, 100);
	}

	public Queue(String name, int capacity) {
		mQueue = new ArrayBlockingQueue<>(capacity);
		mCapacity = capacity >= 0 ? capacity : 100;
		mName = name;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					levelNotify();
					Utils.sleep(mNotifyInterval);
				}
			}
		}, name + "-monitor").start();
	}

	public int getLevel() {
		return (mQueue.size() * 100) / mCapacity;
	}

	public boolean add(E t) {
		try {
			mQueue.put(t);
			return true;
		} catch (InterruptedException e) {
			Log.error(this.getClass().getName(), e);
			return false;
		}
	}

	public E get(long timeout) {
		try {
			return mQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Log.error(this.getClass().getName(), e);
			return null;
		}
	}

	public E getWithoutWait() {
		return mQueue.poll();
	}

	public E peek() {
		return mQueue.peek();
	}

	public int size() {
		return mQueue.size();
	}

	public void forEach(Consumer<E> action) {
		mQueue.forEach(action);
	}
	
	public static final void main(String[] args) {
		Log.addTag(Log.Tag.MONITOR);
		new Queue<String>("my queue");
	}
}
