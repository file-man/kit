package com.kit.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class StringUtils {

	public static final List<String> splitGetFirstByKey(String line, String sep, boolean withTail) {
		return splitByKey(line, sep, 1, 1, withTail);
	}

	public static final String splitGetFirstByKey(String line, String sep) {
		List<String> list = splitByKey(line, sep, 1, 1, false);
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	public static final List<String> splitByKey(String line, char sep) {
		return splitByKey(line, sep, 1, 0);
	}

	public static final List<String> splitByKey(String line, char key, int groupNum) {
		return splitByKey(line, key, groupNum, 0);
	}

	public static final List<String> splitByKey(String line, char key, int groupNum, int returnCount) {
		return splitByKey(line, String.valueOf(key), groupNum, returnCount, false);
	}

	public static final List<String> splitByKey(String line, String key) {
		return splitByKey(line, key, 1, 0, false);
	}

	private static final List<String> splitByKey(String line, String key, int groupNum, int returnCount,
			boolean withTail) {
		if (line == null || line.length() == 0 || groupNum < 1) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();
		int index = 0;
		int start = 0;
		while (index < line.length()) {
			for (int i = 0; i < groupNum; i++) {
				int end = line.indexOf(key, index + 1);
				if (end != -1) {
					index = end;
				} else {
					if (start < line.length()) {
						list.add(line.substring(start, line.length()));
					}
					return list;
				}
			}

			list.add(line.substring(start, index));
			start = index + key.length();

			if (returnCount > 0 && list.size() == returnCount) {
				if (withTail) {
					list.add(line.substring(start));
				}

				return list;
			}
		}

		return list;
	}

	public static final String conbine(List<String> list, char sep) {
		final StringBuilder sb = new StringBuilder();
		for (String item : list) {
			sb.append(item + sep);
		}

		return sb.substring(0, sb.length() - 1);
	}

	public static final boolean equalsIgnoreCase(String a, String b) {
		if (a == null || b == null) {
			return false;
		}

		return a.equalsIgnoreCase(b);
	}

	public static final boolean equals(String a, String b) {
		if (a == null || b == null) {
			return false;
		}

		return a.equals(b);
	}

	public static boolean isEmpty(String tag) {
		if (tag != null && tag.length() > 0) {
			return false;
		}

		return true;
	}

	public static final byte[] base64Decode(String content) {
		return Base64.getDecoder().decode(content);
	}

	public static final byte[] base64Decode(byte[] content) {
		return Base64.getDecoder().decode(content);
	}
	
	public static final String base64Encode(byte[] content) {
		return Base64.getEncoder().encodeToString(content);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
//		try(File file = new File("a.mp4")) {
//			file.getFileOutputStream().write(base64Decode(content));
//		}
//		String src = "318000698,1582487182,2420719767,2435175873,2432477211,2435981781,2327151196,2419224795,2394705495,2337302460,2242835776,2421907637,2412553023,2047428348,2380589331,501019941,726225844,2426371169,2050745028,1732824648,2411013813,2408199819,2343106220,2412082881,1491801582,2356579696,2405275227,2393398191,2399000565,2417174811,2424977795,2427626939,2384569133,2402308669,949231002,2398167039,2430346707,2408946309,2374341780,2413837041,2394672041,1228770350,2357166442,2409944753,1230944322,2388823935,2336407132,2301832236,2322817900,1135136392,1401312934,1382270786,982729648,2369355802,2373051474,2431622603,2420827503,2418875655,2420091157,2369210396,2400606781,2427513243,2397955009,2239083608,2316119840,2360688024,2320720764,2393250697,282331620,2428615153,2418277649,2355462486,2415426253,2356156742,2318224148,2361884158,2424869297";
//		String src = ",123445,,,,,,990,";
//		Log.print(splitByKey(src, ",").size() + "");

//		Runnable helloService = () -> {
//			String hello = "Hello ";
//			Log.print(hello);
		
//		};
//		helloService.run();
//
//		Consumer<Integer> func = src::charAt;
//		func.accept(1);
//		Log.print(src);
	}
}
