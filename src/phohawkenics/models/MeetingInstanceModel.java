package phohawkenics.models;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.threads.RBMS;
import phohawkenics.utils.LogUtil;
import phohawkenics.utils.ModelDetailsUtil;
import phohawkenics.utils.RoomUtil;

public class MeetingInstanceModel {
	public final static int MAX_PARTICIPANTS = 50;
	private final static int MAX_REQUEST_NUMBER = 10;
	private final static int REQUEST_NUMBER_LENGTH = 3;
	private String mStatus = "Not Completed";
	
	private RequestModel mRequest = null;
	private InviteModel mInvite = null;
	private AttendanceModel mAttendance = null;
	
	public MeetingInstanceModel (int requestorID, String input, RBMS server) {
		mRequest = new RequestModel(this, requestorID, input, server);
		mInvite = new InviteModel(this);
		mAttendance = new AttendanceModel(this);
	}
	
	public MeetingInstanceModel (String primaryIDLineNumber, RBMS server) {
		String modelInfo[] = ModelDetailsUtil.getModelDetailsStringByID(primaryIDLineNumber).split(Constants.SEPERATOR_LINE);
		if (modelInfo.length > 0)
			mStatus = modelInfo[0];
		if (modelInfo.length > 1)
			mRequest = decodeRequestString(modelInfo[1]);
		if (modelInfo.length > 2)
			mInvite = decodeInviteString(modelInfo[2]);
		else
			mInvite = new InviteModel(this);
		if (modelInfo.length > 3)
			mAttendance = decodeAttendanceString(modelInfo[3]);
		else
			mAttendance = new AttendanceModel(this);
	}
	
	private RequestModel decodeRequestString(String requestLine) {
		return new RequestModel(this, requestLine);
	}
	
	private InviteModel decodeInviteString(String inviteLine) {
		if (inviteLine.isEmpty())
			return new InviteModel(this);
		else
			return new InviteModel(this, inviteLine);
	}
	
	private AttendanceModel decodeAttendanceString(String attendanceLine) {
		if(attendanceLine.isEmpty())
			return new AttendanceModel(this);
		else
			return new AttendanceModel(this, attendanceLine);
	}
	
	public void setStatus(String status) {
		mStatus = status;
		if (getRequest() != null) {
			ModelDetailsUtil.updateMeetingStatus(getRequest().getRequestNumber(), mStatus);
			LogUtil.cout(MeetingConstants.MSG_REQUEST_NUMBER
					+ getRequest().getRequestNumber() 
					+ Constants.SEPERATOR_INPUT
					+ mStatus);
		}
	}
	
	public String getStatus () {
		return mStatus;
	}
	
	public RequestModel getRequest() {
		return mRequest;
	}
	
	public InviteModel getInvite() {
		return mInvite;
	}
	
	public AttendanceModel getAttendance() {
		return mAttendance;
	}
	
	public static boolean validRequestFormat(String input) {
		String inputArray[] = input.split(" ");			
		if (inputArray.length == 7) {	
			 return true;
		}
		return false;
	}
	
	public static String generateRandomNumber() {
		String random = "";
		for (int i = 0; i < REQUEST_NUMBER_LENGTH; i ++)
			random += String.valueOf((int) (Math.random() * MAX_REQUEST_NUMBER));
		return random;
	}
	
	public String getConfirmMessage() {
		String message = MeetingConstants.MSG_CONFIRMED + Constants.SEPERATOR_INPUT
				+ MeetingConstants.MSG_MEETING_NUMBER + getInvite().getMeetingNumber();
		
		return message;
	}
	
	public String getPotentialCancelMessage() {
		String message = MeetingConstants.MSG_CANCEL + Constants.SEPERATOR_INPUT
				+ MeetingConstants.MSG_MEETING_NUMBER + getInvite().getMeetingNumber();
		
		return message;
	}
	
	public String getScheduledMessage() {
		String message = MeetingConstants.MSG_SCHEDULED + Constants.SEPERATOR_INPUT;
		message += MeetingConstants.MSG_REQUEST_NUMBER + mRequest.getRequestNumber() + Constants.SEPERATOR_INPUT
				+ MeetingConstants.MSG_MEETING_NUMBER + mInvite.getMeetingNumber() + Constants.SEPERATOR_INPUT;
		
		String confirmedParticipants[] = mAttendance.getConfirmedParticipants();
		for (int i = 0; i < confirmedParticipants.length; i ++) {
			message += confirmedParticipants[i];
			if (i != confirmedParticipants.length - 1) {
				message += Constants.SEPERATOR_MULTIPLE;
			}
		}
		
		return message;
	}
	
	public void meetingCompleted() {
		ModelDetailsUtil.updateMeetingStatus(mRequest.getRequestNumber(), MeetingConstants.STATUS_COMPLETED);
		RoomUtil.addReservationTimeSlot( mRequest.getRoomNumber(), mRequest.getDate(), mRequest.getTime(), mRequest.getRequestNumber());
	}
	
	public String getNotScheduledMessage() {
		String message = MeetingConstants.MSG_NOT_SCHEDULED + Constants.SEPERATOR_INPUT;
		message += MeetingConstants.MSG_DATE + mRequest.getDate() + Constants.SEPERATOR_INPUT
				+ MeetingConstants.MSG_TIME + mRequest.getTime() + Constants.SEPERATOR_INPUT
				+ MeetingConstants.MSG_MINIMUM + mAttendance.getMinimumAttendees() + Constants.SEPERATOR_INPUT;
		
		String confirmedParticipants[] = mAttendance.getConfirmedParticipants();
		for (int i = 0; i < confirmedParticipants.length; i ++) {
			message += confirmedParticipants[i];
			if (i != confirmedParticipants.length - 1) {
				message += Constants.SEPERATOR_MULTIPLE;
			}
		}
		
		message += Constants.SEPERATOR_INPUT + MeetingConstants.MSG_TOPIC + mRequest.getTopic();
		return message;
	}
}
