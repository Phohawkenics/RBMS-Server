package phohawkenics.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import phohawkenics.common.Constants;
import phohawkenics.common.DatabaseConstants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.models.AttendanceModel;
import phohawkenics.models.InviteModel;

public class ModelDetailsUtil extends FileUtil{
	
	private static String getModelString (int IDLineNumber) {
		String modelInfo [] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_MODEL_DETAILS);
		String modelString = Constants.KEY_EMPTY;
		
		if(IDLineNumber + DatabaseConstants.DETAILS_ATTENDANCE_ROW < modelInfo.length) {
			modelString += modelInfo[IDLineNumber + DatabaseConstants.DETAILS_STATUS_ROW] + Constants.SEPERATOR_LINE
					+ modelInfo[IDLineNumber + DatabaseConstants.DETAILS_REQUEST_ROW] + Constants.SEPERATOR_LINE
					+ modelInfo[IDLineNumber + DatabaseConstants.DETAILS_INVITATION_ROW] + Constants.SEPERATOR_LINE
					+ modelInfo[IDLineNumber + DatabaseConstants.DETAILS_ATTENDANCE_ROW];
		}
		return modelString;
	}
	
	public static String getStatus(String ID) {
		int lineNumber = findIDLine(DatabaseConstants.FILE_NAME_MODELS, ID);
		String models[] = fileToArray(DatabaseConstants.FILE_NAME_MODEL_DETAILS);
		
		return models[lineNumber + DatabaseConstants.DETAILS_STATUS_ROW];
	}
	
	public static String getModelDetailsStringByID (String ID) {
		int IDLineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_MODEL_DETAILS, ID);
		return getModelString(IDLineNumber);
	}
	
	@SuppressWarnings("resource")
	public static boolean requestExists(String request) {
		try {
			File file = new File(DatabaseConstants.FILE_NAME_MODELS);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(DatabaseConstants.KEY_ID + request))
					return true;
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static synchronized void addNewEntryDetails (String ID, String request) {
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DatabaseConstants.FILE_NAME_MODEL_DETAILS, true)))) {
		    out.println(DatabaseConstants.KEY_ID + ID);
		    out.println(MeetingConstants.STATUS_NOT_STARTED);
			out.println(request);
			for(int i = DatabaseConstants.DETAILS_OFFSET; i < DatabaseConstants.DETAILS_ROWS_PER_ENTRY; i++) {
				out.println("");
			}
			out.close();
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
	
	public static void writeToFile(InviteModel invite){
		if (invite != null) {
			String ID = invite.getRequestNumber();
			int lineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_MODEL_DETAILS, ID) + DatabaseConstants.DETAILS_INVITATION_ROW;
			try {
				FileUtil.update(DatabaseConstants.FILE_NAME_MODEL_DETAILS, lineNumber, invite.getString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void writeToFile(AttendanceModel attendance){
		if (attendance != null) {
			String ID = attendance.getRequestNumber();
			int lineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_MODEL_DETAILS, ID) + DatabaseConstants.DETAILS_ATTENDANCE_ROW;
			try {
				FileUtil.update(DatabaseConstants.FILE_NAME_MODEL_DETAILS, lineNumber, attendance.getString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized void updateMeetingStatus (String ID, String status) {
		int requestLineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_MODEL_DETAILS ,ID);
		try {
			FileUtil.update(DatabaseConstants.FILE_NAME_MODEL_DETAILS,
					requestLineNumber + DatabaseConstants.DETAILS_STATUS_ROW, status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
