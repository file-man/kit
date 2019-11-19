package com.data.kit.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.data.kit.utils.Log;

public class File implements AutoCloseable {

	private InputStreamReader mReader;
	private OutputStreamWriter mWriter;
	private int mReadIndex;
	private int mReadLength;
	private char[] mReadBuffer;
	private int mReadBufferSize;
	private String mPath;
	private Charset mCharset;
	private boolean mAppend;
	private java.io.File mInternal;

	public File(String path, Charset cs, boolean append, int readBufferSize) {
		mPath = path;
		mCharset = cs;
		mAppend = append;
		mReadBufferSize = readBufferSize > 0 ? readBufferSize : 1024 * 1024 * 10;
	}

	public File(String path, Charset cs, boolean append) {
		this(path, cs, append, 0);
	}

	public File(String path, Charset cs) {
		this(path, cs, true, 0);
	}

	public File(String path) {
		this(path, StandardCharsets.UTF_8, true, 0);
	}

	public long getLength() {
		return getInternal().length();
	}

	public File setCharset(Charset cs) {
		mCharset = cs;
		return this;
	}

	public File setAppend(boolean append) {
		mAppend = append;
		return this;
	}

	public File setReadBufferSize(int size) {
		mReadBufferSize = size;
		return this;
	}

	public boolean exist() {
		return getInternal().exists();
	}

	public void createNewFile() throws IOException {
		getInternal().createNewFile();
	}

	public void createDir() {
		getInternal().mkdirs();
	}

	public void delete() {
		if (exist()) {
			getInternal().delete();
		}
	}

	private java.io.File getInternal() {
		if (mInternal == null) {
			mInternal = new java.io.File(mPath);
		}

		return mInternal;
	}

	public synchronized int getLineCount() throws IOException {
		int count = 0;
		while (true) {
			String line = readLine();
			if (!"".equals(line)) {
				count++;
			} else {
				mReader.close();
				mReader = null;
				return count;
			}
		}
	}

	public FileOutputStream getFileOutputStream() throws FileNotFoundException {
		return new FileOutputStream(getInternal());
	}

	public void writeLine(String line) {
		writeLine(line, true);
	}

	public void writeLine(String line, boolean continueUse) {
		OutputStreamWriter writer = getFileWriter();
		try {
			writer.write(line + "\n");
			if (!continueUse) {
				closeWriter();
			}
		} catch (IOException e) {
			Log.error(this.getClass().getName(), e);
		}
	}

	public final void flush() {
		if (mWriter != null) {
			try {
				mWriter.flush();
			} catch (IOException e) {
				Log.error(this.getClass().getName(), e);
			}
		}
	}

	private final synchronized OutputStreamWriter getFileWriter() {
		if (mWriter == null) {
			java.io.File file = getInternal();
			if (checkFileExist(file, true)) {
				try {
					mWriter = new OutputStreamWriter(new FileOutputStream(file, mAppend), mCharset);
				} catch (FileNotFoundException e) {
					Log.error(this.getClass().getName(), e);
					return null;
				}
			}
		}

		return mWriter;
	}

	private final synchronized InputStreamReader getFileReader() throws IOException {
		if (mReader == null) {
			java.io.File file = getInternal();
			if (checkFileExist(file, false)) {
				mReadBuffer = new char[mReadBufferSize];
				try {
					mReader = new InputStreamReader(new FileInputStream(file), mCharset);
				} catch (FileNotFoundException e) {
					Log.error(this.getClass().getName(), e);
				}
			}
		}

		return mReader;
	}

	private boolean checkFileExist(java.io.File file, boolean createNew) {
		if (!file.exists() && createNew) {
			try {
				if (!file.createNewFile()) {
					return false;
				}
			} catch (IOException e) {
				Log.error(this.getClass().getName(), e);
			}
		}

		return true;
	}

	private int getLineEndIndex(char[] buffer, int start, int length) {
		if (start < length) {
			for (int i = start; i < length; i++) {
				if (buffer[i] == '\n') {
					return i;
				}
			}
		}

		return -1;
	}

	public boolean hasNext() {
		return mReadLength != -1;
	}

	public FileInputStream getFileInputStream() throws FileNotFoundException {
		return new FileInputStream(getInternal());
	}

	public String readLine() {
		return readLine(true);
	}

	public String readLine(boolean continueUse) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader reader = getFileReader();
			if (reader == null) {
				return "";
			}

			do {
				if (mReadLength > 0) {
					int end = getLineEndIndex(mReadBuffer, mReadIndex, mReadLength);
					if (end > -1) {
						sb.append(mReadBuffer, mReadIndex, end - mReadIndex);
						mReadIndex = end + 1;
						return sb.toString();
					} else {
						sb.append(mReadBuffer, mReadIndex, mReadLength - mReadIndex);
						mReadIndex = 0;
					}
				}
			} while ((mReadLength = reader.read(mReadBuffer)) != -1);
		} catch (IOException e) {
			Log.error(this.getClass().getName(), e);
		} finally {
			if (!continueUse) {
				closeReader();
			}
		}

		return sb.toString();
	}

	private void closeReader() {
		try {
			if (mReader != null) {
				mReader.close();
			}
		} catch (IOException e) {
			Log.error(this.getClass().getName(), e);
		} finally {
			mReader = null;
		}
	}

	private void closeWriter() {
		if (mWriter != null) {
			try {
				mWriter.flush();
				mWriter.close();
			} catch (IOException e) {
				Log.error(this.getClass().getName(), e);
			} finally {
				mWriter = null;
			}
		}
	}

	public void close() {
		closeReader();
		closeWriter();
	}

	public static final String getPathSeparator() {
		return java.io.File.separator;
	}

	public static void main(String[] args) {

	}
}
