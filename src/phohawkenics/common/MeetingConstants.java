package phohawkenics.common;

public class MeetingConstants {
	public static String MSG_TYPE_REQUEST = "REQUEST";
	public static String STRING_ACCEPT = "ACCEPT";
	public static String STRING_REJECT = "REJECT";
	public static String STRING_WITHDRAW = "WITHDRAW";
	public static String STRING_CANCEL = "CANCEL";
	public static String MSG_TYPE_ADD = "ADD";
	public static enum TYPES {REQUEST, ACCEPT, REJECT, WITHDRAW, CANCEL, MEETINGDETAILS, ADD, OTHER };
	
	public static String STATUS_NOT_STARTED = "NOT STARTED";
	public static String STATUS_INVITE = "INVITE";
	public static String STATUS_ATTENDANCE = "ATTENDANCE";
	public static String STATUS_COMPLETED = "COMPLETED";
	public static String STATUS_CANCELLED = "CANCELLED";
	public static String STATUS_INVITE2 = "INVITE2";
	public static String STATUS_ATTENDANCE2 = "ATTENDANCE2";
	public static String STATUS_WAITING2 = "WAITING2";
	public static String STATUS_CANCELLED2 = "CANCELLED2";
	public static String STATUS_WAITING = "WAITING";
	public static String STATUS_EXPIRED = "EXPIRED";
	public static String STATUS_AVAILABLE = "AVAILABLE";
	public static String STATUS_UNAVAILABLE = "UNAVAILABLE";
	public static String STATUS_TEMP = "TEMP";
	
	public static String MSG_INVALID_REQUEST = "Invalid Request Format";
	public static String MSG_NOT_COMPLETED = "Not Complete";
	public static String MSG_CONFIRMED = "CONFIRMED";
	public static String MSG_CANCEL = "CANCEL";
	public static String MSG_SCHEDULED = "SCHEDULED";
	public static String MSG_NOT_SCHEDULED = "NOT SCHEDULED";
	public static String MSG_REQUEST_NUMBER = "Request#:";
	public static String MSG_MEETING_NUMBER = "Meeting#:";
	public static String MSG_DATE = "Date:";
	public static String MSG_TIME = "Time:";
	public static String MSG_MINIMUM = "Minimum:";
	public static String MSG_TOPIC = "Topic:";
	
	public static String MSG_TYPE_AGENDA = "AGENDA";
	public static String MSG_TYPE_MEETING_DETAILS = "MEETINGDETAILS";
	public static String MSG_TYPE_ROOMS = "ROOMS";
	public static String MSG_TYPE_NAMES = "NAMES";
	public static String MSG_TYPE_MESSAGE = "MESSAGE";
	public static String MSG_TYPE_MESSAGE_BANK = "MESSAGEBANK";
	
	public static String ATTENDANCE_NO_REPLY = "haveYetToReply";
	public static String ATTENDANCE_CONFIRM = "confirmed";
	public static String ATTENDANCE_REJECTED = "rejected";
	public static String ATTENDANCE_WITHDRAWED = "withdrawed";
	public static String ATTENDANCE_ADDED = "added";
	public static String ATTENDANCE_REQUESTER = "requester";
	
	public static TYPES decodeInput (String input) {
		String inputArray[] = input.split(Constants.SEPERATOR_INPUT);
		
		if (inputArray[0].equalsIgnoreCase(MeetingConstants.MSG_TYPE_REQUEST)) {
			return TYPES.REQUEST;
		} 
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.STRING_ACCEPT)) {
			return TYPES.ACCEPT;
		}
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.STRING_REJECT)) {
			return TYPES.REJECT;
		}
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.STRING_WITHDRAW)) {
			return TYPES.WITHDRAW;
		}
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.STRING_CANCEL)) {
			return TYPES.CANCEL;
		}
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.MSG_TYPE_MEETING_DETAILS)) {
			return TYPES.MEETINGDETAILS;
		} 
		else if (inputArray[0].equalsIgnoreCase(MeetingConstants.MSG_TYPE_ADD)) {
			return TYPES.ADD;
		}
		return TYPES.OTHER;
	}
}
