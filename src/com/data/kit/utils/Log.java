package com.data.kit.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.data.kit.config.Configuration;
import com.data.kit.config.Configuration.OnConfigChangeListener;
import com.data.kit.io.File;

public class Log {

	private static boolean sLog = true;
	private static boolean sTime = true;
	private static boolean sThread = true;
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static List<String> mLogTags = new ArrayList<String>();

	public static final class Tag {
		public static final String ERROR = "error";
		public static final String DEBUG = "debug";
		public static final String TEST = "test";
		public static final String MONITOR = "monitor";
	}

	public static final class Config {
		public static final String LOG_ERROR_PATH = "log.error.path";
		public static final String LOG_ENABLE_TAGS = "log.enable.tags";
	}

	static {
		Configuration.addConfigChangeListener(new OnConfigChangeListener() {
			@Override
			public void onConfigChange() {
				mLogTags.clear();
				final String news = Configuration.getStringConfig(Config.LOG_ENABLE_TAGS);
				if (news != null && news.length() > 0) {
					mLogTags.addAll(StringUtils.splitByKey(news, ',', 1));
				}

				print(Tag.MONITOR, "new log tag = " + news);
			}
		});
	}

	public static final void addTag(String tag) {
		if (!StringUtils.isEmpty(tag)) {
			mLogTags.add(tag);
		}
	}

	public static final void error(String tag, Throwable e, String message) {
		if (message != null) {
			error(tag, message);
		}

		error(tag, e.toString());
		for (StackTraceElement trace : e.getStackTrace()) {
			error(tag, "\t" + trace.toString());
		}
	}

	public static final void error(String tag, Throwable e) {
		error(tag, e, null);
	}

	public static final void error(String tag, String log) {
		String msg = print(Tag.ERROR, tag + ": " + log);
		if (!StringUtils.isEmpty(msg)) {
			final File file = FileUtils.open(Configuration.getStringConfig(Config.LOG_ERROR_PATH));
			printToFile(msg, file, false);
		}
	}

	public static final void printToFile(String log, File logFile, boolean continueUse) {
		logFile.writeLine(log.toString(), continueUse);
	}

	public static final void printToFile(String log, File logFile) {
		printToFile(log, logFile, true);
	}

	public static final String print(String tag, String log) {
		return print(tag, log, "\n");
	}

	public static final void print(String line) {
		System.out.println(line);
	}

	public static final <T> void print(Collection<T> collection) {
		for (T t : collection) {
			print(t.toString());
		}
	}

	public static final String print(String tag, String log, String end) {
		if (
		// log content;
		log == null ||
		// log on/off;
				!sLog ||
				// tag filter;
				(StringUtils.isEmpty(tag) || (!mLogTags.contains(tag) && !Tag.ERROR.equals(tag)))) {
			return "";
		}

		final String time = sTime ? (DF.format(new Date()) + ": ") : "";
		final String thread = sThread ? ("[" + Thread.currentThread().getName() + "] ") : "";
		tag = tag == null ? "" : ("[" + tag + "] ");
		final String tid = sThread ? ("[" + Thread.currentThread().getId() + "] ") : "";

		final String line = time + thread + tid + tag + log + end;
		System.out.print(line);
		return line;
	}

	public static final void print(String tag, Iterable<String> list) {
		print(tag, list, "\n");
	}

	public static final <T> void print(String tag, Iterable<String> list, String sep) {
		for (String log : list) {
			print(tag, log, sep);
		}
	}

	public static final void main(String[] args) {
		String s = null;
		try {
			s.length();
		} catch (Exception e) {
			Log.error(Log.class.getName(), e);
		}
	}
}
