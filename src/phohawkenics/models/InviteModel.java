package phohawkenics.models;

import phohawkenics.common.Constants;
import phohawkenics.utils.ModelDetailsUtil;

//From Server, Invitation for meeting
	public class InviteModel {
		private MeetingInstanceModel mMeetingInstance;
		private String mMeetingNumber = "";
		private String mDate;
		private String mTime;
		private String mTopic;
		private String mRequesterName;
		private boolean mSentRequest[];
		
		public InviteModel(MeetingInstanceModel meetingInstance, String inviteLine) {
			mMeetingInstance = meetingInstance;
			String inviteInfo[] = inviteLine.split(Constants.SEPERATOR_DATA_HANDLER);
			mMeetingNumber = inviteInfo[1];
			mDate = inviteInfo[2];
			mTime = inviteInfo[3];
			mTopic = inviteInfo[4];
			mRequesterName = inviteInfo[5];
			mSentRequest = new boolean[mMeetingInstance.getRequest().getParticipantCount()];
			String temp[] = inviteInfo[6].split(Constants.SEPERATOR_MULTIPLE);
			for (int i = 0;i < temp.length; i ++) {
				mSentRequest[i] = Boolean.valueOf(temp[i]);
			}
		}
		
		public InviteModel(MeetingInstanceModel meetingInstance) {
			// Get 6 digit Request number
			mMeetingInstance = meetingInstance;
			mMeetingNumber = MeetingInstanceModel.generateRandomNumber();
			mDate = mMeetingInstance.getRequest().getDate();
			mTime = mMeetingInstance.getRequest().getTime();
			mTopic = mMeetingInstance.getRequest().getTopic();
			mRequesterName = mMeetingInstance.getRequest().getRequesterName();
			mSentRequest = new boolean[mMeetingInstance.getRequest().getParticipantCount()];
			for (int i = 0; i < mSentRequest.length; i++) {
				mSentRequest[i] = false;
			}
		}
		
		public String getMeetingNumber() {
			return mMeetingNumber;
		}
		
		public String getMessage() {
			String message = "";
			message += "INVITATION MT# : " + mMeetingNumber 
					+ Constants.SEPERATOR_INPUT + mDate 
					+ Constants.SEPERATOR_INPUT + mTime 
					+ Constants.SEPERATOR_INPUT + "Requester:" + mRequesterName 
					+ Constants.SEPERATOR_INPUT + mTopic;
			return message;
		}
		
		public void setSentRequestStatus (int position) {
			mSentRequest[position] = true;
			ModelDetailsUtil.writeToFile(this);
		}
		
		public boolean getSentRequestStatus (int position) {
			return mSentRequest[position];
		}
		
		public String getString() {
			String invite = "INVITE" + Constants.SEPERATOR_DATA_HANDLER;
			
			invite += mMeetingNumber + Constants.SEPERATOR_DATA_HANDLER
					+ mDate + Constants.SEPERATOR_DATA_HANDLER
					+ mTime + Constants.SEPERATOR_DATA_HANDLER
					+ mTopic + Constants.SEPERATOR_DATA_HANDLER
					+ mRequesterName + Constants.SEPERATOR_DATA_HANDLER;
					for (int i = 0; i < mSentRequest.length; i++) {
						invite += String.valueOf(mSentRequest[i]);
						if (i != mSentRequest.length - 1) {
							invite += Constants.SEPERATOR_MULTIPLE;
						}
					}
			return invite;
		}
		
		public void resetInviteSendStatus(int position) {
			mSentRequest[position] = false;
		}
		
		public String getRequestNumber() {
			return mMeetingInstance.getRequest().getRequestNumber();
		}
	}