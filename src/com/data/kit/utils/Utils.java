package com.data.kit.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import com.data.kit.io.File;

public class Utils {

	public static final void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Log.error(Utils.class.getName(), e);
		}
	}

	public static final byte[] toByteArray(char[] data) {
		byte[] bytes = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			bytes[i] = (byte) data[i];
		}

		return bytes;
	}

	public static final void exec(String cmd) {
		System.out.println("run cmd: " + cmd);
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Process p = pb.start();
			BufferedReader out = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(p.getInputStream()), Charset.forName("utf-8")));
			BufferedReader err = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getErrorStream())));
			String ostr;

			while ((ostr = out.readLine()) != null) {
				System.out.println(ostr);
			}

			String estr;
			while ((estr = err.readLine()) != null) {
				System.out.println("\nError Info");
				System.out.println(estr);
			}
		} catch (Exception e) {
			Log.error(Utils.class.getName(), e);
		}
	}

	public static String getMD5(File file) {
		return getMD5(file, 1024 * 1024);
	}
	
	public static String getMD5(File file, int cacheSize) {
		try (FileInputStream fileInputStream = file.getFileInputStream()) {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[cacheSize];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}
			return DatatypeConverter.printHexBinary(MD5.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final String getMD5(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			return DatatypeConverter.printHexBinary(md.digest());
		} catch (NoSuchAlgorithmException e) {
			Log.error(Utils.class.getName(), e);
			e.printStackTrace();
		}

		return null;
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
//		cmd("ps");
//		byte[] b = new byte[] { 1, 2, 3, 4, 5 };
//		System.out.println(getMD5Checksum(b));
//		byte[] b1 = new byte[] { 1, 2, 3, 4, 6 };
//		System.out.println(getMD5Checksum(b1));
//		byte[] b2 = new byte[] { 1, 2, 3, 4, 5, 6 };
//		System.out.println(getMD5Checksum(b2));
		File file = new File("/Volumes/Data/raw/viber/avatar/000/000540800-8615894633464.jpeg");
		byte[] 
				b = new byte[(int) file.getLength()];
		file.getFileInputStream().read(b);
		System.out.println(getMD5(b));
	}
}
