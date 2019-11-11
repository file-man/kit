package com.data.kit.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.data.kit.concurrent.Task;
import com.data.kit.concurrent.Worker;
import com.data.kit.io.FileMonitor;
import com.data.kit.io.FileMonitor.Listener;
import com.data.kit.utils.Log;
import com.data.kit.utils.StringUtils;

public class Configuration {

	private static Properties PROP = new Properties();
	private static final ArrayList<OnConfigChangeListener> mListener = new ArrayList<OnConfigChangeListener>();
	private static long sPropertyLastModifyTime = 0;
	private static FileMonitor monitor;

	public interface OnConfigChangeListener {
		public void onConfigChange();
	}

	static {
		loadProperties();
	}

	public static final void addConfigChangeListener(OnConfigChangeListener listener) {
		mListener.add(listener);
		// notify while the listener first added;
		listener.onConfigChange();
	}

	public static final List<String> getArrayConfig(String key) {
		return StringUtils.splitByKey(PROP.getProperty(key), ',', 1);
	}

	public static final String getStringConfig(String key) {
		return getStringConfig(key, null);
	}

	public static final boolean getBooleanConfig(String key) {
		return Boolean.valueOf(PROP.getProperty(key, "false"));
	}

	public static final String getStringConfig(String key, String defaultValue) {
		return PROP.getProperty(key, defaultValue);
	}

	public static final int getIntConfig(String key, int defaultValue) {
		return Integer.valueOf(PROP.getProperty(key, String.valueOf(defaultValue)));
	}

	public static final long getLongConfig(String key, long defaultValue) {
		return Long.valueOf(PROP.getProperty(key, String.valueOf(defaultValue)));
	}

	public static final void startPropertyMonitor(Worker worker) {
		monitor = new FileMonitor();
		monitor.startMonitor("config", "config.prop", worker, new Listener() {
			@Override
			public void onFileModified(String filename) {
				loadProperties();
			}
		});
	}

	public static final void startPropertyMonitor() {
		monitor.close();
	}

	public static final void loadProperties() {
		try {
			final File file = new File("config/config.prop");
			if (!file.exists()) {
				Log.error(Configuration.class.getName(), "config.properties file is not existed!!!");
				return;
			}

			final long time = file.lastModified();
			if (time > sPropertyLastModifyTime) {
				sPropertyLastModifyTime = time;
				final BufferedReader br = new BufferedReader(new FileReader(file));
				PROP.load(br);
				for (Object key : PROP.keySet()) {
					if (PROP.containsKey(key)) {
						final String value = PROP.getProperty(key.toString());
						Log.print(Log.Tag.MONITOR, "load property " + key + " = " + value);
					}
				}

				if (!mListener.isEmpty()) {
					Worker worker = new Worker("Config-Change");
					Task task = new Task() {
						@Override
						public void run() {
							for (OnConfigChangeListener l : mListener) {
								l.onConfigChange();
							}
						}
					};
					
					task.addTaskListener(task.new TaskListener() {
						@Override
						public void onFinish() {
							worker.quit();
						}
					});
					
					worker.addTask(task);
				}
			}
		} catch (IOException e) {
			Log.error(Configuration.class.getName(), e, "read config file failed.");
		}
	}

	public static final void main(String[] args) {
		final Properties properties = new Properties();

		try {
			final BufferedReader br = new BufferedReader(new FileReader("config.properties"));
			properties.load(br);
			String hosts = properties.getProperty("hosts");
			for (String host : StringUtils.splitByKey(hosts, ',', 1)) {
				System.out.println(host.trim());
			}
		} catch (IOException e) {
			Log.error(Configuration.class.getName(), e);
		}
	}
}
