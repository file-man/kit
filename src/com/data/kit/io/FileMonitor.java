package com.data.kit.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.data.kit.concurrent.Task;
import com.data.kit.concurrent.Worker;
import com.data.kit.utils.Log;

public class FileMonitor {

	private boolean stop;

	public interface Listener {
		public void onFileModified(String filename);
	}

	public void startMonitor(String path, String filename, Worker worker, Listener listener) {
		worker.addTask(new Task() {

			@Override
			public void run() {
				WatchService watcher;
				try {
					watcher = FileSystems.getDefault().newWatchService();
					Paths.get(path).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
				} catch (IOException e1) {
					Log.error(this.getClass().getName(), e1);
					return;
				}

				while (!stop) {
					WatchKey key;
					try {
						key = watcher.take();
					} catch (InterruptedException e) {
						Log.error(this.getClass().getName(), e);
						continue;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.context().toString().equals(filename)) {
							if (StandardWatchEventKinds.ENTRY_MODIFY.name().equalsIgnoreCase(event.kind().name())) {
								listener.onFileModified(filename);
							}
						}
					}

					if (!key.reset()) {
						break;
					}
				}
			}
		});
	}

	public void close() {
		stop = true;
	}

	public static final void main(String[] args) throws IOException {
		new FileMonitor().startMonitor("config", "config.prop", new Worker("File monitor"), new Listener() {

			@Override
			public void onFileModified(String filename) {
				System.out.println("file modified");
			}
		});
	}
}
