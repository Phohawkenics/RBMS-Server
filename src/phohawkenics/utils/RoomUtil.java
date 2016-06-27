package phohawkenics.utils;

import java.io.FileNotFoundException;

import phohawkenics.common.Constants;
import phohawkenics.common.DatabaseConstants;

public class RoomUtil extends FileUtil{
	public static boolean roomIsUnavailable(String ID, String date, String timeRange) {
		String roomReservations = getRoomReservations(ID);
		String roomReservationsArray[] = roomReservations.split(Constants.SEPERATOR_DATA_HANDLER);
		
		if(roomReservationsArray[0].isEmpty()) {
			return false;
		}
		
		int timeRangeArray[] = formatTimeRangeToIntArray(timeRange);
		
		int tStart = timeRangeArray[0];
		int tEnd = timeRangeArray[1];
		
		String reservationDetails[];
		for(int i = 0; i < roomReservationsArray.length; i++) {
			// [0] = DATE, [1] = START TIME, [2] = END TIME, [3] = Request #
			reservationDetails =  roomReservationsArray[i].split(Constants.SEPERATOR_INPUT);
			
			if (date.equalsIgnoreCase(reservationDetails[0])) {
				int startTime = Integer.valueOf(reservationDetails[1]);
				int endTime = Integer.valueOf(reservationDetails[2]);
				
				if (tStart >= startTime && tStart < endTime)
					return true;
				else if (tEnd > startTime && tEnd <= endTime)
					return true;
				else if (tStart <= startTime && tEnd >= endTime)
					return true;
				else
					return false;
			} 
		}
		
		return false;
	}
	
	public static void addReservationTimeSlot (String ID, String date, String timeRange, String requestNumber) {
		int roomIDLineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_ROOMS, ID);
		String roomReservations = getRoomReservations(ID);
		int timeRangeArray[] = formatTimeRangeToIntArray(timeRange);
		int tStart = timeRangeArray[0];
		int tEnd = timeRangeArray[1];
		if (!roomReservations.isEmpty())
			roomReservations += Constants.SEPERATOR_DATA_HANDLER;
		
		roomReservations += date + Constants.SEPERATOR_INPUT
				+ tStart + Constants.SEPERATOR_INPUT
				+ tEnd + Constants.SEPERATOR_INPUT
				+ requestNumber;
		try {
			FileUtil.update(DatabaseConstants.FILE_NAME_ROOMS, roomIDLineNumber + DatabaseConstants.ROOMS_RESERVATION_ROW, roomReservations);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int[] formatTimeRangeToIntArray(String timeRangeString) {
		// 12h30-13h30
		String tempTimeRangeArray[] = timeRangeString.split(Constants.SEPERATOR_TIME_RANGE);
		int timeRangeArray[] = new int[2];
		for (int i = 0; i < 2; i++) {
			timeRangeArray[i] = Integer.valueOf(
					tempTimeRangeArray[i].replace(Constants.SEPERATOR_TIME_H, Constants.KEY_EMPTY));
		}
		return timeRangeArray;
	}
	
	public static String getRoomReservations(String ID) {
		int roomIDlineNumber = FileUtil.findIDLine(DatabaseConstants.FILE_NAME_ROOMS, ID);
		String roomFile[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_ROOMS);
		String roomReservationsString = "";
		if (roomFile.length > roomIDlineNumber + DatabaseConstants.ROOMS_RESERVATION_ROW) {
			roomReservationsString = roomFile[roomIDlineNumber + DatabaseConstants.ROOMS_RESERVATION_ROW];
		}
		return roomReservationsString;
	}
	
	public static String getRoomIDs() {
		String roomsID[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_ROOMS);
		String temp = Constants.KEY_EMPTY;
		for(int i = 0; i < roomsID.length; i ++) {
			if (roomsID[i].contains(DatabaseConstants.KEY_ID)) {
				temp += roomsID[i].replace(DatabaseConstants.KEY_ID, Constants.KEY_EMPTY) + Constants.SEPERATOR_MULTIPLE;
			}
		}
		if (temp.length() == 0) {
			return Constants.KEY_EMPTY;
		} else {
			return temp.substring(0, temp.length() - 1);
		}
	}
	public static void removeRoomReservation(String ID, String requestNumber) {
		String roomReservations = getRoomReservations(ID);
		String roomReservationsArray[] = roomReservations.split(Constants.SEPERATOR_DATA_HANDLER);
		
		String newRoomReservations = Constants.KEY_EMPTY;
		for (int i = 0; i < roomReservationsArray.length; i ++) {
			if (!requestNumber.equals(roomReservationsArray[i].split(Constants.SEPERATOR_INPUT)[3])) {
				newRoomReservations += roomReservationsArray[i] + Constants.SEPERATOR_DATA_HANDLER;
			}
		}
		if (!newRoomReservations.isEmpty()) {
			newRoomReservations = newRoomReservations.substring(0, newRoomReservations.length() - 1);
		}
		try {
			update(DatabaseConstants.FILE_NAME_ROOMS,
					findIDLine(DatabaseConstants.FILE_NAME_ROOMS, ID) + DatabaseConstants.ROOMS_RESERVATION_ROW, 
					newRoomReservations);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
