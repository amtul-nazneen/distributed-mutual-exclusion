package mutex.app.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Utils {
	public static String FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String ZONE = "GMT-05:00";

	public static HashMap<String, String> hosttoprocess = new HashMap<String, String>();

	public static String getTimestamp() {
		LocalDateTime date = LocalDateTime.now(ZoneId.of(ZONE));
		return "[" + date.format(DateTimeFormatter.ofPattern(FORMAT)) + "]";
	}

	public static void log(String message) {
		System.out.println(getTimestamp() + " " + message);
	}

	public static void log(String message, boolean display) {
		if (display)
			System.out.println(getTimestamp() + " " + message);
	}

	public static String getProcessFromHost(String host) {
		hosttoprocess.put("dc04.utdallas.edu", "Process:1");
		hosttoprocess.put("dc05.utdallas.edu", "Process:2");
		hosttoprocess.put("dc06.utdallas.edu", "Process:3");
		hosttoprocess.put("dc07.utdallas.edu", "Process:4");
		hosttoprocess.put("dc08.utdallas.edu", "Process:5");
		return hosttoprocess.get(host.toLowerCase());

	}

}
