package phohawkenics.models;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.threads.RBMS;
import phohawkenics.utils.*;

public class RequestModel {
	private MeetingInstanceModel mMeetingInstance;
	private String mInput;
	private String mRequestNumber;
	private String mDate;
	private String mTime;
	private int mMinimum;
	private String mRequesterName;
	private String mParticipantNames[];
	private int mParticipantCount = 0;
	private String mTopic;
	private String mRoomNumber;
	
	public RequestModel(MeetingInstanceModel meetingInstance, String requestLine) {
		mMeetingInstance = meetingInstance;
		String requestInfo[] = requestLine.split(Constants.SEPERATOR_DATA_HANDLER);
		mInput = requestInfo[1];
		mRequestNumber = requestInfo[2];
		mDate = requestInfo[3];
		mTime = requestInfo[4];
		mMinimum = Integer.valueOf(requestInfo[5]);
		mRequesterName = requestInfo[6];
		String temp[] = requestInfo[7].split(Constants.SEPERATOR_MULTIPLE);
		mParticipantNames = temp;
		mParticipantCount = temp.length;
		mTopic = requestInfo[8];
		mRoomNumber = requestInfo[9];
	}
	
	public RequestModel(MeetingInstanceModel meetingInstance, int requestorID, String input, RBMS server) {
		mMeetingInstance = meetingInstance;
		mInput = input;
		// Decode request
		// FORMAT: REQUEST date time minimum participants1,participants2,... topic
		mRequesterName = server.getClientThreadByID(requestorID).getClient().getName();
		String inputArray[] = input.split(" ");		
		
		// Get 6 digit Request number
		String tempRequestNumber =  MeetingInstanceModel.generateRandomNumber();
		do {
			mRequestNumber =  MeetingInstanceModel.generateRandomNumber();
		} while (ModelDetailsUtil.requestExists(tempRequestNumber));
		
		mDate = inputArray[1];
		
		mTime = inputArray[2];
		
		mMinimum = Integer.parseInt(inputArray[3]);				
		
		mParticipantNames = inputArray[4].split(Constants.SEPERATOR_MULTIPLE);
		
		mParticipantCount = mParticipantNames.length;
		
		mTopic = inputArray[5];
		
		mRoomNumber = inputArray[6];
		
		if (RoomUtil.roomIsUnavailable(mRoomNumber, mDate, mTime)) {
			 mMeetingInstance.setStatus(MeetingConstants.STATUS_UNAVAILABLE);
		} else {
			mMeetingInstance.setStatus(MeetingConstants.STATUS_INVITE);
			ModelUtil.addNewEntryModels(mRequestNumber);
			ModelDetailsUtil.addNewEntryDetails(mRequestNumber, getString());
		}
	}
	
	public String getInput() {
		return mInput;
	}
	
	public String getRequestNumber() {
		return mRequestNumber;
	}
	
	public String getDate() {
		return mDate;
	}
	
	public String getTime() {
		return mTime;
	}
	
	public String getRequesterName () {
		return mRequesterName;
	}
	
	public String[] getParticipantNames () {
		return mParticipantNames;
	}
	public int getParticipantCount() {
		return mParticipantCount;
	}
	
	public int getMinimumParticipants() {
		return mMinimum;
	}
	
	public String getTopic() {
		return mTopic;
	}
	
	public String getRoomNumber() {
		return mRoomNumber;
	}
	
	public String getString() {
		String request = "REQUEST" + Constants.SEPERATOR_DATA_HANDLER;
		
		request += mInput + Constants.SEPERATOR_DATA_HANDLER 
				+ mRequestNumber + Constants.SEPERATOR_DATA_HANDLER 
				+ mDate + Constants.SEPERATOR_DATA_HANDLER 
				+ mTime + Constants.SEPERATOR_DATA_HANDLER
				+ mMinimum + Constants.SEPERATOR_DATA_HANDLER
				+ mRequesterName + Constants.SEPERATOR_DATA_HANDLER;
				
		for (int i = 0; i < mParticipantCount; i++) {
			request += mParticipantNames[i];
			
			if (i != mParticipantCount - 1) {
				request += Constants.SEPERATOR_MULTIPLE;
			}	
		}
		request += Constants.SEPERATOR_DATA_HANDLER + mTopic;
		request += Constants.SEPERATOR_DATA_HANDLER + mRoomNumber;
		
		return request;
	}
	
	public String getGlobalStatus() {
		return mMeetingInstance.getStatus();
	}
}