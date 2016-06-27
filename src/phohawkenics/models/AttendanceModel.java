package phohawkenics.models;

import java.util.Arrays;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.utils.LogUtil;
import phohawkenics.utils.ModelDetailsUtil;

//From Server, check if notify all if meeting still happening
	public class AttendanceModel {
		private MeetingInstanceModel mMeetingInstance;
		private String mRequestNumber = "";
		private String mMeetingNumber = "";
		private int mTotalPeople = 0;
		private int mTotalAnswers = 0;
		private int mTotalConfirm = 0;
		private int mTotalReject = 0;
		private int mMinimumAttendees = 0;
		private String[] mNames;
		
		private boolean participantHasNotAnswered(String name) {
			for (String currentName: mNames) {
				String nameArray[] = currentName.split("\\.");
				if(name.equals(nameArray[0])) {
					if(nameArray.length == 2)
						return false;
					break;
				}
			} return true;
		}
		
		private void printParticipants() {
			for (int i = 0; i < mNames.length;i++) {
				LogUtil.cout(mNames[i]);
			}
		}
		
		public AttendanceModel(MeetingInstanceModel meetingInstance, String attendanceLine) {
			mMeetingInstance = meetingInstance;
			String attendanceInfo[] = attendanceLine.split(Constants.SEPERATOR_DATA_HANDLER);
			mRequestNumber = attendanceInfo[1];
			mMeetingNumber = attendanceInfo[2];
			mTotalPeople = Integer.valueOf(attendanceInfo[3]);
			mTotalAnswers = Integer.valueOf(attendanceInfo[4]);
			mTotalConfirm = Integer.valueOf(attendanceInfo[5]);
			mTotalReject = Integer.valueOf(attendanceInfo[6]);
			mMinimumAttendees = Integer.valueOf(attendanceInfo[7]);
			mNames = attendanceInfo[8].split(Constants.SEPERATOR_MULTIPLE);
		}
		
		public AttendanceModel(MeetingInstanceModel meetingInstance) {
			mMeetingInstance = meetingInstance;
			mRequestNumber = mMeetingInstance.getRequest().getRequestNumber();
			mMeetingNumber = mMeetingInstance.getInvite().getMeetingNumber();
			mNames = Arrays.copyOfRange(mMeetingInstance.getRequest().getParticipantNames(), 0, mMeetingInstance.getRequest().getParticipantCount());
			mTotalPeople = mMeetingInstance.getRequest().getParticipantCount();
			
			mMinimumAttendees = mMeetingInstance.getRequest().getMinimumParticipants();
		}
		
		public int getMinimumAttendees () {
			return mMinimumAttendees;
		}
		public void added(String name) {
			if (!participantHasNotAnswered(name)) {
				for(int i = 0; i < mNames.length; i ++) {
					if(mNames[i].split(Constants.REGEX_DOT)[0].equalsIgnoreCase(name)) {
						mNames[i] = name + Constants.SEPERATOR_STATUS + MeetingConstants.ATTENDANCE_ADDED;
						break;
					}
				}
			}
			printParticipants();
		}
		
		public void withdrawed(String name) {
			if (!participantHasNotAnswered(name)) {
				for(int i = 0; i < mNames.length; i ++) {
					if(mNames[i].split(Constants.REGEX_DOT)[0].equalsIgnoreCase(name)) {
						mNames[i] = name + Constants.SEPERATOR_STATUS + MeetingConstants.ATTENDANCE_WITHDRAWED;
						break;
					}
				}
			}
			printParticipants();
		}
		
		public void confirmed(String name) {
			if (participantHasNotAnswered(name)) {
				for(int i = 0; i < mNames.length; i ++) {
					if(mNames[i].equalsIgnoreCase(name)) {
						mNames[i] += Constants.SEPERATOR_STATUS + MeetingConstants.ATTENDANCE_CONFIRM;
						break;
					}
				}
			}
			printParticipants();
		}
		public void reject(String name) {
			if (participantHasNotAnswered(name)) {
				for(int i = 0; i < mNames.length; i ++) {
					if(mNames[i].equalsIgnoreCase(name)) {
						mNames[i] += Constants.SEPERATOR_STATUS + MeetingConstants.ATTENDANCE_REJECTED;
						break;
					} 
				}
			}
			printParticipants();
		}
		public boolean stillWaitingOnReplies() {
			updateCount();
			boolean stillWaiting;
			if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_ATTENDANCE2)
					|| mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE2)
					|| mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_WAITING2)) {
				if ((mTotalPeople - mMinimumAttendees) >= mTotalReject) {
					if (mTotalAnswers == mTotalPeople) {
						mMeetingInstance.setStatus(MeetingConstants.STATUS_COMPLETED);
						stillWaiting = false;
					} else {
						if (!mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_WAITING))
							mMeetingInstance.setStatus(MeetingConstants.STATUS_WAITING2);
						stillWaiting = true;
					}
				}
				else {
					mMeetingInstance.setStatus(MeetingConstants.STATUS_CANCELLED2);
					stillWaiting = false;
				}
				
			} else {
				if ((mTotalPeople - mMinimumAttendees) >= mTotalReject) {
					if (mTotalAnswers == mTotalPeople) {
						mMeetingInstance.setStatus(MeetingConstants.STATUS_COMPLETED);
						stillWaiting = false;
					} else {
						if (!mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_WAITING))
							mMeetingInstance.setStatus(MeetingConstants.STATUS_WAITING);
						stillWaiting = true;
					}
				}
				else {
					mMeetingInstance.setStatus(MeetingConstants.STATUS_CANCELLED);
					stillWaiting = false;
				}
				
				
			}
			ModelDetailsUtil.writeToFile(mMeetingInstance.getAttendance());
			
			return stillWaiting;
		}
		
		public void updateCount() {
			mTotalAnswers = 0;
			mTotalConfirm = 0;
			mTotalReject = 0;
			for (String name: mNames) {
				String nameInfo[] = name.split(Constants.REGEX_DOT);
				if (nameInfo.length == 2) {
					++mTotalAnswers;
					String status = nameInfo[1];
					if (status.equals(MeetingConstants.ATTENDANCE_CONFIRM)
							|| status.equals(MeetingConstants.ATTENDANCE_ADDED)) {
						++mTotalConfirm;
					}
					if (status.equals(MeetingConstants.ATTENDANCE_REJECTED)
							|| status.equals(MeetingConstants.ATTENDANCE_WITHDRAWED)) {
						++mTotalReject;
					}
				}
			}
		}
		
		public String[] getConfirmedParticipants() {
			String confirmedParticipants[] = new String[mTotalConfirm];
			int j = 0;
			for (int i = 0; i < mNames.length; i++) {
				String nameArray[] = mNames[i].split(Constants.REGEX_DOT);
				if (nameArray.length == 2) {
					if (nameArray[1].equals(MeetingConstants.ATTENDANCE_CONFIRM)) {
						confirmedParticipants[j] = nameArray[0];
						++j;
					}
				}
			}
			return confirmedParticipants;
		}
		
		public int[] getNonConfirmedParticipantsPosition() {
			int nonConfirmedParticipants[] = new int[mTotalPeople - mTotalConfirm];
			int j = 0;
			for (int i = 0; i < mNames.length; i++) {
				String nameArray[] = mNames[i].split(Constants.REGEX_DOT);
				if (nameArray.length == 2) {
					if (!nameArray[1].equals(MeetingConstants.ATTENDANCE_CONFIRM)
							&& !nameArray[1].equals(MeetingConstants.ATTENDANCE_ADDED)) {
						nonConfirmedParticipants[j] = i;
						++j;
					}
				} else if (nameArray.length == 1) {
					nonConfirmedParticipants[j] = i;
					++j;
				}
			}
			return nonConfirmedParticipants;
		}
		
		public void resetNonConfirmedParticipantStatus(int position) {
			if (mNames[position].split(Constants.REGEX_DOT).length == 2) {
				mNames[position] = mNames[position].split(Constants.REGEX_DOT)[0]; 
			}
		}
		
		public String getString() {
			String attendance = "ATTENDANCE" + Constants.SEPERATOR_DATA_HANDLER;
			attendance += mRequestNumber + Constants.SEPERATOR_DATA_HANDLER
					+ mMeetingNumber + Constants.SEPERATOR_DATA_HANDLER
					+ String.valueOf(mTotalPeople) + Constants.SEPERATOR_DATA_HANDLER
					+ String.valueOf(mTotalAnswers) + Constants.SEPERATOR_DATA_HANDLER
					+ String.valueOf(mTotalConfirm) + Constants.SEPERATOR_DATA_HANDLER
					+ String.valueOf(mTotalReject) + Constants.SEPERATOR_DATA_HANDLER 
					+ String.valueOf(mMinimumAttendees) + Constants.SEPERATOR_DATA_HANDLER;
			
			for (int i = 0; i < mNames.length; i++) {
				attendance += mNames[i];
				
				if (i != mNames.length - 1) {
					attendance += Constants.SEPERATOR_MULTIPLE;
				}
			}
			
			return attendance;
		}
		
		public String getRequestNumber() {
			return mMeetingInstance.getRequest().getRequestNumber();
		}
	}
