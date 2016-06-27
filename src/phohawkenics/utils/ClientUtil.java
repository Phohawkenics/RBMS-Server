package phohawkenics.utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import phohawkenics.common.Constants;
import phohawkenics.common.DatabaseConstants;

public class ClientUtil extends FileUtil{
	
	public static void addNewEntryClients (String name, String password) {
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DatabaseConstants.FILE_NAME_CLIENTS, true)))) {
			out.println(DatabaseConstants.KEY_NAME + name);
			out.println(password);
			for(int i = DatabaseConstants.CLIENTS_OFFSET; i < DatabaseConstants.CLIENTS_ROWS_PER_ENTRY; i++) {
				out.println("");
			}
			out.close();
		}catch (IOException e) {
		    LogUtil.cout("Error writing new client to file: " + e.getMessage());
		}
	}
	
	public static boolean clientIsUnvailable(String name, String date, String timeRange) {
		String clientAgenda = getClientAgenda(name);
		String clientAgendaArray[] = clientAgenda.split(Constants.SEPERATOR_DATA_HANDLER);
		
		if(clientAgendaArray[0].isEmpty()) {
			return false;
		}
		
		int timeRangeArray[] = formatTimeRangeToIntArray(timeRange);
		int tStart = timeRangeArray[0];
		int tEnd = timeRangeArray[1];
		
		String agendaDetails[];
		for(int i = 0; i < clientAgendaArray.length; i++) {
			// [0] = DATE, [1] = START TIME, [2] = END TIME, [3] = Request #
			agendaDetails =  clientAgendaArray[i].split(Constants.SEPERATOR_INPUT);
			
			if (date.equalsIgnoreCase(agendaDetails[0])) {
				int startTime = Integer.valueOf(agendaDetails[1]);
				int endTime = Integer.valueOf(agendaDetails[2]);
				
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
	
	public static String addAgendaEntry (String name, String date, String timeRange, String status, String meetingNumber, String requestNumber) {
		int clientIDLineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String clientAgenda = getClientAgenda(name);
		int timeRangeArray[] = formatTimeRangeToIntArray(timeRange);
		int tStart = timeRangeArray[0];
		int tEnd = timeRangeArray[1];
		if (!clientAgenda.isEmpty())
			clientAgenda += Constants.SEPERATOR_DATA_HANDLER;
		
		clientAgenda += date + Constants.SEPERATOR_INPUT
				+ tStart + Constants.SEPERATOR_INPUT
				+ tEnd + Constants.SEPERATOR_INPUT
				+ status + Constants.SEPERATOR_INPUT
				+ meetingNumber + Constants.SEPERATOR_INPUT
				+ requestNumber;
		try {
			FileUtil.update(DatabaseConstants.FILE_NAME_CLIENTS, clientIDLineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW, clientAgenda);
		} catch (FileNotFoundException e) {
			LogUtil.cout("Error writing to file: " + e.getMessage());
		}
		
		return clientAgenda;
	}
	
	private static int[] formatTimeRangeToIntArray(String timeRangeString) {
		// 12h30-13h30
		String tempTimeRangeArray[] = timeRangeString.split(Constants.SEPERATOR_TIME_RANGE);
		int timeRangeArray[] = new int[2];
		for (int i = 0; i < 2; i++) {
			timeRangeArray[i] = Integer.valueOf(
					tempTimeRangeArray[i].replace(Constants.SEPERATOR_TIME_H, Constants.KEY_EMPTY));
		}
		return timeRangeArray;
	}
	
	public static String getClientAgenda(String name) {
		int clientIDlineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String clientFile[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		String clientAgendaString = "";
		if (clientFile.length > clientIDlineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW) {
			clientAgendaString = clientFile[clientIDlineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW];
		}
		return clientAgendaString;
	}
	
	public static String setClientAgenda(String name, String agenda) {
		int clientIDlineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String clientFile[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		String clientAgendaString = "";
		if (clientFile.length > clientIDlineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW) {
			clientAgendaString = clientFile[clientIDlineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW];
		}
		return clientAgendaString;
	}
	
	private static String getClientString (int nameLineNumber) {
		String modelInfo [] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		String modelString = Constants.KEY_EMPTY;
		modelString += modelInfo[nameLineNumber].replace(DatabaseConstants.KEY_NAME, Constants.KEY_EMPTY) + Constants.SEPERATOR_LINE
				+ modelInfo[nameLineNumber + DatabaseConstants.CLIENTS_PASSWORD_ROW] + Constants.SEPERATOR_LINE
				+ modelInfo[nameLineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW];
		
		return modelString;
	}
	
	public static String getClientsStringByName (String name) {
		int nameLineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		if (nameLineNumber == -1) {
			return Constants.KEY_EMPTY;
		}
		return getClientString(nameLineNumber);
	}
	
	public static boolean isUniqueName(String name) {
		int nameLineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_MODEL_DETAILS, name);
		if (nameLineNumber == -1) return true;
		else return false;
	}

	public static void updateAgendaEntryStatus(String name, String status, String meetingNumber) {
		String clientAgenda[] = getClientAgenda(name).split(Constants.SEPERATOR_DATA_HANDLER);
		
		for(int i = 0; i < clientAgenda.length; i++) {
			String agendaEntry[] = clientAgenda[i].split(Constants.SEPERATOR_INPUT);
			if (agendaEntry[4].equals(meetingNumber)) {
				agendaEntry[3] = status;
				String updatedEntry = FileUtil.join(Constants.SEPERATOR_INPUT, agendaEntry);
				clientAgenda[i] = updatedEntry;
				break;
			}
		}
		
		int nameLineNumber = findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		
		try {
			update(
					DatabaseConstants.FILE_NAME_CLIENTS,
					nameLineNumber + DatabaseConstants.CLIENTS_AGENDA_ROW,
					join(Constants.SEPERATOR_DATA_HANDLER, clientAgenda)
				);
		} catch (FileNotFoundException e) {
			LogUtil.cout("Error writing to file: " + e.getMessage());
		}
	}
	
	public static boolean doesAgendaEntryNotExist(String name, String meetingNumber) {
		String clientAgenda[] = getClientAgenda(name).split(Constants.SEPERATOR_DATA_HANDLER);
		for(int i = 0; i < clientAgenda.length; i++) {
			String agendaEntry[] = clientAgenda[i].split(Constants.SEPERATOR_INPUT);
			if (agendaEntry.length > 1) {
				if (agendaEntry[4].equals(meetingNumber)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static String getClientNames() {
		String clientNames[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		String temp = Constants.KEY_EMPTY;
		for(int i = 0; i < clientNames.length; i ++) {
			if (clientNames[i].contains(DatabaseConstants.KEY_NAME)) {
				temp += clientNames[i].replace(DatabaseConstants.KEY_NAME, Constants.KEY_EMPTY) + Constants.SEPERATOR_MULTIPLE;
			}
		}
		if (temp.length() == 0) {
			return Constants.KEY_EMPTY;
		} else {
			return temp.substring(0, temp.length() - 1);
		}
	}
	
	public static String getMessageBankAndDelete(String name) {
		int clientIDlineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String clientFile[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		
		String messages = Constants.KEY_EMPTY;
		
		if (clientFile.length > clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW) {
			messages = clientFile[clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW];
			try {
				update(DatabaseConstants.FILE_NAME_CLIENTS, clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW, Constants.KEY_EMPTY);
			} catch (FileNotFoundException e) {
				LogUtil.cout("Error writing to file: " + e.getMessage());
			}
		}
		
		return messages;
	}
	
	public static String getMessageBank(String name) {
		int clientIDlineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String clientFile[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_CLIENTS);
		
		String messages = Constants.KEY_EMPTY;
		
		if (clientFile.length > clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW) {
			messages = clientFile[clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW];
		}
		
		return messages;
	}
	
	public static void addToMessageBank (String name, String msg) {
		int clientIDlineNumber = FileUtil.findNameLine(DatabaseConstants.FILE_NAME_CLIENTS, name);
		String messageBank = getMessageBank(name);
		
		if (!messageBank.isEmpty())
			messageBank += Constants.SEPERATOR_DATA_HANDLER;
		messageBank += LogUtil.geCurrentTime() + Constants.SEPERATOR_INPUT + msg;
		try {
			FileUtil.update(DatabaseConstants.FILE_NAME_CLIENTS, 
					clientIDlineNumber + DatabaseConstants.CLIENTS_MESSAGE_BANK_ROW,
					messageBank);
		} catch (FileNotFoundException e) {
			LogUtil.cout("Error writing to file: " + e.getMessage());
		}
	}
}
