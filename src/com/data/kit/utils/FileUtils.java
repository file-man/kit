package com.data.kit.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.data.kit.io.File;

public class FileUtils {

	public interface ForEachLine<T> {
		public boolean onProcess(T t);
	}

	public interface LineProcessor {
		public String onProcess(String line);
	}

	public static final File open(String path) {
		return new File(path);
	}

	public static final File open(String path, Charset cs) {
		return new File(path, cs);
	}

	public static final File open(String path, Charset cs, boolean append) {
		return new File(path, cs, append);
	}

	public static final File open(String path, Charset cs, boolean append, int readBufferSize) {
		return new File(path, cs, append, readBufferSize);
	}

	public static final boolean move(String from, String to) {
		return move(from, to, true);
	}

	public static final boolean move(String from, String to, boolean force) {
		try (File target = new File(to);) {
			if (target.exist()) {
				if (force) {
					target.delete();
					target.createNewFile();
				} else {
					return false;
				}
			} else {
				target.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (File source = new File(from);
				File target = new File(to);
				FileInputStream reader = source.getFileInputStream();
				FileOutputStream writer = target.getFileOutputStream()) {
			if (source.exist()) {
				long size = source.getLength() < (1024 * 1024 * 2) ? source.getLength() : (1024 * 1024 * 2);
				byte[] buf = new byte[(int) size];
				while (true) {
					int length = reader.read(buf, 0, (int) size);
					if (length > 0) {
						writer.write(buf);
						writer.flush();
					} else {
						return true;
					}
				}
			}
		} catch (IOException e) {
			Log.error(FileUtils.class.getName(), e);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static final void scan(String path, ForEachLine<String> forEach, int bufferSize) {
		InputStream in = null;
		Scanner sc = null;

		try {
			in = new BufferedInputStream(new FileInputStream(path), bufferSize);
			sc = new Scanner(in);
			while (sc.hasNextLine()) {
				if (!forEach.onProcess(sc.nextLine())) {
					return;
				}
			}
		} catch (FileNotFoundException e) {
			Log.error(FileUtils.class.getName(), e);
			return;
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
	}

	public static final void splitFileByLines(String source, String out, int lines, String ext) {
		splitFileByLines(source, out, lines, 1024 * 1024 * 100, ext, null);
	}

	public static final void splitFileByLines(String source, String out, int lines, int buffer, String ext) {
		splitFileByLines(source, out, lines, buffer, ext, null);
	}

	public static final void splitFileByLines(String source, String out, int lines, int buffer, String ext,
			LineProcessor forEach) {
		OutputStreamWriter writer = null;
		int index = 0;

		try (InputStream in = new BufferedInputStream(new FileInputStream(source), buffer);
				Scanner sc = new Scanner(in)) {
			String line = "";

			while (sc.hasNextLine()) {
				if (index++ % lines == 0) {
					if (writer != null) {
						writer.flush();
						writer.close();
					}

					java.io.File outFile = createFile(getSplitFileName(out, index, lines, ext));
					writer = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
				}

				line = sc.nextLine();
				if (forEach != null) {
					line = forEach.onProcess(line);
					if (line == null) {
						continue;
					}
				}

				writer.write(line + "\n");
			}
		} catch (IOException e) {
			Log.error(FileUtils.class.getName(), e);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					Log.error(FileUtils.class.getName(), e);
				}
			}
		}
	}

	public static final void writeToFile(String name, byte[] data) {
		try (File file = new File(name); FileOutputStream writer = file.getFileOutputStream()) {
			writer.write(data);
		} catch (IOException e) {
			Log.error(FileUtils.class.getName(), e);
			e.printStackTrace();
		}
	}

	private static final String getSplitFileName(String path, int index, int size, String ext) {
		return path + java.io.File.separatorChar + index + "-" + (index + size - 1) + "." + ext;
	}

	private static final java.io.File createFile(String name) {
		java.io.File newFile = new java.io.File(name);
		if (!newFile.exists()) {
			try {
				if (!newFile.createNewFile()) {
					System.err.println("File IO error, file " + name + " create failed.");
					return null;
				}
			} catch (IOException e) {
				Log.error(FileUtils.class.getName(), e);
			}
		}

		return newFile;
	}

	public static void main(String[] args) {
		try (File file = open("/Users/kit/Downloads/test.txt")) {
//			file.writeLine("wahaha", false);
			while (file.hasNext()) {
//				Log.print("Debug", file.readLine());
			}
		}
	}
}
