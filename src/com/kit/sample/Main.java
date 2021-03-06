package com.kit.sample;

import java.io.IOException;

import com.kit.concurrent.WorkGroup;
import com.kit.concurrent.Worker;
import com.kit.config.Configuration;
import com.kit.config.Configuration.OnConfigChangeListener;
import com.kit.flow.Flow;
import com.kit.utils.Log;

public class Main {

	public static void main(String[] args) throws IOException {
		Log.addTag(Log.Tag.MONITOR);
		Flow.enableMonitorLog();
		Flow.enableMonitorLog();

		WorkGroup group = new WorkGroup(100);
		Configuration.startPropertyMonitor(new Worker("prop.monitor"));

		Flow<String> flow = new Flow<String>("test-flow", 0) {

			@Override
			public void onDataHandleStart(DataHandler handler) {

			}

			@Override
			public void onDataHandle(String data) {
				while (getQueueSize() < 10) {
					addData("hello");
				}
			}
		};

		Configuration.addConfigChangeListener(new OnConfigChangeListener() {

			@Override
			public void onConfigChange() {
				flow.setHandlerThread(Configuration.getIntConfig("test.thread.size", 1), group);
			}
		});

		flow.addData("hello");
	}
}
