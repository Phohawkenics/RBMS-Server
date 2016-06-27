package phohawkenics.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import phohawkenics.common.Constants;

public class LogUtil {
	public static void cout(String msg) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
		System.out.println(timeStamp + Constants.SEPERATOR_INPUT + msg);
		writeToFile(timeStamp + Constants.SEPERATOR_INPUT + msg);
	}
	
	private static void writeToFile(String msg){
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Logs.txt", true)))) {;
			out.println(msg);
			out.close();
		}catch (IOException e) {
		    LogUtil.cout("Error to Log file: " + e.getMessage());
		}
	}
	
	public static String geCurrentTime() {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd:HHmm").format(Calendar.getInstance().getTime());
		return timeStamp;
	}
}
