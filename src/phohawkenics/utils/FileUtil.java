package phohawkenics.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import phohawkenics.common.Constants;
import phohawkenics.common.DatabaseConstants;
import phohawkenics.common.MeetingConstants;

public class FileUtil {
	
	@SuppressWarnings("resource")
	protected static String[] fileToArray(String fileName) {

		File read = new File(fileName);
		Scanner in = null;
		try {
			in = new Scanner(read);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		List<String> fileContents = new LinkedList<String>();

		while (in.hasNextLine()) {
			fileContents.add(in.nextLine());
		}

		String[] data = fileContents.toArray(new String[fileContents.size()]);
		
		return data;
	}
	
	protected static void update(String fileName, int lineNumber, String text) throws FileNotFoundException {
		String fileContents[] = fileToArray(fileName);
		if (fileContents.length > lineNumber) {
		fileContents[lineNumber] = text;
			PrintWriter writer;
			try {
				writer = new PrintWriter(fileName, "UTF-8");
				for (String line: fileContents) {
					writer.println(line);
				}
				writer.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LogUtil.cout("File length is shorter the desired line");
		}
	}
	
	@SuppressWarnings("resource")
	protected static int findIDLine(String fileName, String ID) {
		File read = new File(fileName);
		Scanner in = null;
		try {
			in = new Scanner(read);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line;
		int i = 0;
		while (in.hasNextLine()) {
			line = in.nextLine();
			if (ID.equals(line.replace(DatabaseConstants.KEY_ID, Constants.KEY_EMPTY)))
				return i;
			++i;
		}
		return -1;
	}
	
	@SuppressWarnings("resource")
	protected static int findNameLine(String fileName, String name) {
		File read = new File(fileName);
		Scanner in = null;
		try {
			in = new Scanner(read);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line;
		int i = 0;
		while (in.hasNextLine()) {
			line = in.nextLine();
			if (name.equals(line.replace(DatabaseConstants.KEY_NAME, Constants.KEY_EMPTY)))
				return i;
			++i;
		}
		return -1;
	}
	
	@SuppressWarnings("resource")
	protected static int findIDLineByMeetingNumber(String meetingNumber) {
		File read = new File(DatabaseConstants.FILE_NAME_MODEL_DETAILS);
		Scanner in = null;
		try {
			in = new Scanner(read);
		} catch (FileNotFoundException e) {
			LogUtil.cout("Error Reading file: " + e.getMessage());
		}
		String line = Constants.KEY_EMPTY;
		int i = 0;
		while (in.hasNextLine()) {
			line = in.nextLine();
			if (line.contains(MeetingConstants.STATUS_INVITE)) {
				if (meetingNumber.equals(line.split(Constants.SEPERATOR_INPUT)[1]))
					return i - DatabaseConstants.DETAILS_INVITATION_ROW;
				++i;
			}
		}
		return -1;
	}
	
	public static String join(String separator, String... values) {
	    StringBuilder sb = new StringBuilder(128);
	    int end = 0;
	    for (String s : values) {
	      if (s != null) {
	        sb.append(s);
	        end = sb.length();
	        sb.append(separator);
	      }
	    }
	    return sb.substring(0, end);
	  }
	
	public static String join(String[] array) {
		StringBuilder builder = new StringBuilder();
		for(String s : array) {
		    builder.append(s);
		}
		return builder.toString();
	}
}
