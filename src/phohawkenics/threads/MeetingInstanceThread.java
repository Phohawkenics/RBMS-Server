package phohawkenics.threads;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.models.AttendanceModel;
import phohawkenics.models.InviteModel;
import phohawkenics.models.MeetingInstanceModel;
import phohawkenics.models.RequestModel;
import phohawkenics.utils.ClientUtil;
import phohawkenics.utils.LogUtil;
import phohawkenics.utils.ModelDetailsUtil;
import phohawkenics.utils.RoomUtil;

public class MeetingInstanceThread extends Thread{
	private RBMS mServer = null;
	private MeetingInstanceModel mMeetingInstance = null;
	private DataOutputStream  streamOutRequester = null;
	private String streamOutParticipantNames [];
	private DataOutputStream streamOutParticipants[];
	private ArrayList<String> unknownParticipantNames = new ArrayList<String>();
	private final static int WAIT_MILLISECONDS = 2000;
	
	public MeetingInstanceThread (int requestorID, String input, RBMS server){
		mServer = server;
		mMeetingInstance = new MeetingInstanceModel(requestorID, input, server);
	}
	
	public MeetingInstanceThread (String primaryIDLineNumber, RBMS server) {
		mServer = server;
		mMeetingInstance = new MeetingInstanceModel(String.valueOf(primaryIDLineNumber), server);
	}
	
	private synchronized static void send (DataOutputStream outputStream, String msg) {
		try {
			outputStream.writeUTF(msg);
			outputStream.flush();
		} catch (IOException ioe) {
			LogUtil.cout(" ERROR sending: " + ioe.getMessage());
		}
	}
	
	private void setUnknownRequester() {
		if (mServer.getClientOutputStreamByName(mMeetingInstance.getRequest().getRequesterName()) != null) {
			streamOutRequester = new DataOutputStream(
					new BufferedOutputStream(mServer.getClientOutputStreamByName(mMeetingInstance.getRequest().getRequesterName())
					));
		}
	}
	
	private void setUnknownParticipants() {
		int tempPosition;
		String unknownInfo[];
		for (int i = 0; i < unknownParticipantNames.size(); i++) {
			unknownInfo = unknownParticipantNames.get(i).split(Constants.REGEX_DOT);
			tempPosition = Integer.valueOf(unknownInfo[1]);
				if (mServer.getClientOutputStreamByName(unknownInfo[0]) != null) {
					streamOutParticipants[tempPosition] = 
							new DataOutputStream(
									new BufferedOutputStream(mServer.getClientOutputStreamByName(unknownInfo[0]))
									);
					unknownParticipantNames.remove(i);
				}
			
		}
	}
	
	private void setUnknownOutputStreams() {
		setUnknownRequester();
		setUnknownParticipants();
	}

	private void sendInvitations() {
		if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE2)
				|| mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE)) {
			mMeetingInstance.setStatus(MeetingConstants.STATUS_INVITE);
			String inviteMsg = mMeetingInstance.getInvite().getMessage();
			for (int i = 0; i < streamOutParticipants.length; i++) {
				if (streamOutParticipants[i] != null) {
					if (!mMeetingInstance.getInvite().getSentRequestStatus(i)) {
						send(streamOutParticipants[i],
								MeetingConstants.MSG_TYPE_MESSAGE
								+ Constants.SEPERATOR_CLIENT_TRIGGER
								+inviteMsg);
						mMeetingInstance.getInvite().setSentRequestStatus(i);
					}
				} else {
					if (!mMeetingInstance.getInvite().getSentRequestStatus(i)) {
						addToMessageBank(streamOutParticipantNames[i], inviteMsg);
						mMeetingInstance.getInvite().setSentRequestStatus(i);
					}
				}
			}
		}
		ModelDetailsUtil.writeToFile(mMeetingInstance.getInvite());
	}
	
	private void sendScheduledMessage() {		
		String[] confirmedParticipants = mMeetingInstance.getAttendance().getConfirmedParticipants();
		String scheduledMsgParticipants = mMeetingInstance.getConfirmMessage(); 
		
		for (int i = 0; i < streamOutParticipantNames.length; i++) {
			for (String participant: confirmedParticipants) {
				if (streamOutParticipantNames[i].equals(participant)) {
					String name = streamOutParticipantNames[i];
					if(streamOutParticipants[i] != null)
						send(streamOutParticipants[i], MeetingConstants.MSG_TYPE_MESSAGE
								+ Constants.SEPERATOR_CLIENT_TRIGGER
								+ scheduledMsgParticipants);
					else
						addToMessageBank(name, scheduledMsgParticipants);
				}
			}
		}
		
		String scheduledMsgRequester = mMeetingInstance.getScheduledMessage();
		if (streamOutRequester != null) {
			send(streamOutRequester, MeetingConstants.MSG_TYPE_MESSAGE 
					+ Constants.SEPERATOR_CLIENT_TRIGGER + scheduledMsgRequester);
		} else {
			addToMessageBank(mMeetingInstance.getRequest().getRequesterName(), scheduledMsgRequester);
		}
	}

	private void sendCancelMessage() {
		String willTryAgainMesssage = "Will attempt to resend";
		
		String[] confirmedParticipants = mMeetingInstance.getAttendance().getConfirmedParticipants();
		String potentialCancelMsg = mMeetingInstance.getPotentialCancelMessage(); 
		
		for (int i = 0; i < streamOutParticipantNames.length; i++) {
			for (String participant: confirmedParticipants) {
				if (streamOutParticipantNames[i].equals(participant)) {
					String name = streamOutParticipantNames[i];
					if(streamOutParticipants[i] != null)
						send(streamOutParticipants[i], MeetingConstants.MSG_TYPE_MESSAGE
								+ Constants.SEPERATOR_CLIENT_TRIGGER
								+ potentialCancelMsg);
					else
						addToMessageBank(name, potentialCancelMsg);
				}
			}
		}
		
		if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE2)) {
			for (int i = 0; i < streamOutParticipantNames.length; i++) {
				for (String participant: confirmedParticipants) {
					if (streamOutParticipantNames[i].equals(participant)) {
						String name = streamOutParticipantNames[i];
						if(streamOutParticipants[i] != null)
							send(streamOutParticipants[i], MeetingConstants.MSG_TYPE_MESSAGE
									+ Constants.SEPERATOR_CLIENT_TRIGGER
									+ willTryAgainMesssage);
						else
							addToMessageBank(name, willTryAgainMesssage);
					}
				}
			}
		}
		
		String notScheduledMsg = mMeetingInstance.getNotScheduledMessage();
		if (streamOutRequester != null)
			send(streamOutRequester, 
					MeetingConstants.MSG_TYPE_MESSAGE
					+ Constants.SEPERATOR_CLIENT_TRIGGER
					+ notScheduledMsg);
		else {
			addToMessageBank(mMeetingInstance.getRequest().getRequesterName(), notScheduledMsg);
		}
		
		if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE2)) {
			if (streamOutRequester != null)
				send(streamOutRequester, 
						MeetingConstants.MSG_TYPE_MESSAGE
						+ Constants.SEPERATOR_CLIENT_TRIGGER
						+ willTryAgainMesssage);
			else {
				addToMessageBank(mMeetingInstance.getRequest().getRequesterName(), notScheduledMsg);
			}
		}
	}

	private void sendMessageToParticipant(String name, String msg) {
		for (int i = 0; i < streamOutParticipantNames.length; i ++) {
			if(name.equals(streamOutParticipantNames[i])) {
				if (streamOutParticipants[i] != null) {
					send(streamOutParticipants[i],
							MeetingConstants.MSG_TYPE_MESSAGE
							+ Constants.SEPERATOR_CLIENT_TRIGGER
							+ msg);
				
				} else {
					addToMessageBank(streamOutParticipantNames[i], msg);
				}
			}
		}
	}

	private void waitForAnswers() {
		if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_ATTENDANCE2)
				|| mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_ATTENDANCE)) {
			ModelDetailsUtil.writeToFile(mMeetingInstance.getAttendance());
			if (unknownParticipantNames.size() > 0)
				setUnknownOutputStreams();
			addAgendaEntryInFile();
			do {
				try {
					sleep(MeetingInstanceThread.WAIT_MILLISECONDS);
				} catch (InterruptedException e) {
					LogUtil.cout("Failed on sleep?????");
				}
				if (unknownParticipantNames.size() > 0)
					setUnknownOutputStreams();
			} while (roomCheck() && mMeetingInstance.getAttendance().stillWaitingOnReplies());
		}
	}
	
	private void addAgendaEntryInFile() {
		String name;
		String requesterName = mMeetingInstance.getRequest().getRequesterName();
		boolean requesterFlag = true;
		
		String meetingNumber = mMeetingInstance.getInvite().getMeetingNumber();
		for(int i = 0; i < streamOutParticipants.length; i++) {
			name = streamOutParticipantNames[i];
			if (requesterName.equals(name)) {
				requesterFlag = false;
			}
			if(ClientUtil.doesAgendaEntryNotExist(name, meetingNumber)) {
				ClientUtil.addAgendaEntry(name,
						mMeetingInstance.getRequest().getDate(),
						mMeetingInstance.getRequest().getTime(),
						MeetingConstants.ATTENDANCE_NO_REPLY,
						meetingNumber,
						mMeetingInstance.getRequest().getRequestNumber()
					);
				
				if (streamOutParticipants[i] != null) {
					send(streamOutParticipants[i],
							MeetingConstants.MSG_TYPE_AGENDA
							+ Constants.SEPERATOR_CLIENT_TRIGGER 
							+ ClientUtil.getClientAgenda(name));
				}
			}
		}
		
		if (requesterFlag) {
			ClientUtil.addAgendaEntry(requesterName,
					mMeetingInstance.getRequest().getDate(),
					mMeetingInstance.getRequest().getTime(),
					MeetingConstants.ATTENDANCE_REQUESTER,
					meetingNumber,
					mMeetingInstance.getRequest().getRequestNumber()
				);
			if (streamOutRequester != null) {
				send(streamOutRequester,
						MeetingConstants.MSG_TYPE_AGENDA
						+ Constants.SEPERATOR_CLIENT_TRIGGER 
						+ ClientUtil.getClientAgenda(requesterName));
			}
		}
		
	}
	
	private void updateAgendaEntryInFile(String name, String status) {
		ClientUtil.updateAgendaEntryStatus(name, status,
				mMeetingInstance.getInvite().getMeetingNumber());
		DataOutputStream client = new DataOutputStream(
				new BufferedOutputStream(mServer.getClientOutputStreamByName(name))
				);
				send(client,
						MeetingConstants.MSG_TYPE_AGENDA
						+ Constants.SEPERATOR_CLIENT_TRIGGER 
						+ ClientUtil.getClientAgenda(name));
	}
	
	private boolean roomCheck() {
		RequestModel request = mMeetingInstance.getRequest();
		if (RoomUtil.roomIsUnavailable(request.getRoomNumber(), request.getDate(), request.getTime())) {
			 mMeetingInstance.setStatus(MeetingConstants.STATUS_UNAVAILABLE);
			 return false;
		} else {
			return true;
		}
	}
	
	private void roomIsUnavailable() {
		if (streamOutRequester != null)
			send(streamOutRequester,
					MeetingConstants.MSG_TYPE_REQUEST 
					+ Constants.SEPERATOR_CLIENT_TRIGGER 
					+  "Request#: " + mMeetingInstance.getRequest().getRequestNumber() + " Room Unavailable");
	}
	
	private void modifyCompletedMeeting(String info) {
		String nameStatus[] = info.split(Constants.SEPERATOR_INPUT);
		setUnknownOutputStreams();
		updateAgendaEntryInFile(nameStatus[0], nameStatus[1]);
		if (nameStatus[1].equals(MeetingConstants.ATTENDANCE_ADDED)) {
			addParticipant(nameStatus[0]);
			String confirmMsg = mMeetingInstance.getConfirmMessage();
			sendMessageToParticipant(nameStatus[0], confirmMsg);
			
		} else if (nameStatus[1].equals(MeetingConstants.ATTENDANCE_WITHDRAWED)) {
			withdrawParticipant(nameStatus[0]);
		}
		mMeetingInstance.getAttendance().stillWaitingOnReplies();
	}
	
	private void addToMessageBank(String name, String msg) {
		ClientUtil.addToMessageBank(name, msg);
	}
	
	public void run() {
		String statusInfo[] = mMeetingInstance.getStatus().split(Constants.SEPERATOR_CLIENT_TRIGGER);
		String startingStatus = mMeetingInstance.getStatus();
		if (!startingStatus.equalsIgnoreCase(MeetingConstants.STATUS_CANCELLED2)) { 
			if (startingStatus.equalsIgnoreCase(MeetingConstants.STATUS_CANCELLED)) {
				sendCancelMessage();
				RoomUtil.removeRoomReservation(mMeetingInstance.getRequest().getRoomNumber(),
						mMeetingInstance.getRequest().getRequestNumber()
					);
			} else if (startingStatus.equalsIgnoreCase(MeetingConstants.STATUS_UNAVAILABLE)) {
				roomIsUnavailable();
			} else if (startingStatus.equalsIgnoreCase(MeetingConstants.STATUS_COMPLETED)) {
				LogUtil.cout("Forbidden state reached");
			} else if (statusInfo.length > 1) {
				/*
				 * This state can only happen if a meeting is completed
				 */
				modifyCompletedMeeting(statusInfo[1]);
				if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_CANCELLED)) {
					RoomUtil.removeRoomReservation(mMeetingInstance.getRequest().getRoomNumber(),
							mMeetingInstance.getRequest().getRequestNumber()
						);
					mMeetingInstance.setStatus(MeetingConstants.STATUS_INVITE2);
					sendCancelMessage();
					int nonConfirmedParticipantsPositions[] = mMeetingInstance.getAttendance().getNonConfirmedParticipantsPosition();
					for (int position: nonConfirmedParticipantsPositions) {
						InviteModel tempI = mMeetingInstance.getInvite();
						tempI.resetInviteSendStatus(position);
						
						AttendanceModel tempA = mMeetingInstance.getAttendance();
						tempA.resetNonConfirmedParticipantStatus(position);
						ModelDetailsUtil.writeToFile(tempI);
						ModelDetailsUtil.writeToFile(tempA);
						updateAgendaEntryInFile(streamOutParticipantNames[position], MeetingConstants.ATTENDANCE_NO_REPLY);
					}
				}
			} else {
				/*
				 * The regular flow
				 */
				LogUtil.cout("Sending Invitations for Request#:" + mMeetingInstance.getRequest().getRequestNumber());
				mMeetingInstance.setStatus(MeetingConstants.STATUS_INVITE);
				sendInvitations();
				LogUtil.cout("Waiting for replies for Request#:" + mMeetingInstance.getRequest().getRequestNumber());
				mMeetingInstance.setStatus(MeetingConstants.STATUS_ATTENDANCE);
				waitForAnswers();
				if(mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_COMPLETED)) {
					sendScheduledMessage();
					mMeetingInstance.meetingCompleted();
				} else if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_CANCELLED)) {
					sendCancelMessage();
				} else if (mMeetingInstance.getStatus().equalsIgnoreCase(MeetingConstants.STATUS_UNAVAILABLE)){
					roomIsUnavailable();
					sendCancelMessage();
				} else {
					LogUtil.cout("Not sure what happend");
				}
			}
			
			/*
			 * RESEND ON CANCELL BY WITHDRAWABLE
			 * */
			
			if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_INVITE2) 
					|| mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_ATTENDANCE2)) {
				LogUtil.cout("Sending Invitations for Request#:" + mMeetingInstance.getRequest().getRequestNumber());
				mMeetingInstance.setStatus(MeetingConstants.STATUS_INVITE2);
				sendInvitations();
				LogUtil.cout("Waiting for replies for Request#:" + mMeetingInstance.getRequest().getRequestNumber());
				mMeetingInstance.setStatus(MeetingConstants.STATUS_ATTENDANCE2);
				waitForAnswers();
				if(mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_COMPLETED)) {
					sendScheduledMessage();
					mMeetingInstance.meetingCompleted();
				} else if (mMeetingInstance.getStatus().equals(MeetingConstants.STATUS_CANCELLED2)) {
					sendCancelMessage();
				} else if (mMeetingInstance.getStatus().equalsIgnoreCase(MeetingConstants.STATUS_UNAVAILABLE)){
					roomIsUnavailable();
					sendCancelMessage();
				} else {
					LogUtil.cout("Corruption in Meeting Status");
				}
			}
		}

		mServer.removeAction(mMeetingInstance.getRequest().getRequestNumber());
		interrupt();
	}
	
	public void participantExiting(String name) {
		if (name.equalsIgnoreCase(mMeetingInstance.getRequest().getRequesterName())) {
			streamOutRequester = null;
		}
		for (int i = 0; i < streamOutParticipantNames.length; i++) {
			if (streamOutParticipantNames[i].equalsIgnoreCase(name)){
				streamOutParticipants[i] = null;
			}
		}
	}
	
	public void addParticipant(String name) {
		setUnknownOutputStreams();
		mMeetingInstance.getAttendance().added(name);
		updateAgendaEntryInFile(name, MeetingConstants.ATTENDANCE_ADDED);
	}
	
	public void withdrawParticipant(String name) {
		setUnknownOutputStreams();
		mMeetingInstance.getAttendance().withdrawed(name);
		updateAgendaEntryInFile(name, MeetingConstants.ATTENDANCE_WITHDRAWED);
	}
	
	public void confirmParticipant(String name) {
		setUnknownOutputStreams();
		mMeetingInstance.getAttendance().confirmed(name);
		updateAgendaEntryInFile(name, MeetingConstants.ATTENDANCE_CONFIRM);
	}
	
	public void rejectParticipant(String name) {
		setUnknownOutputStreams();
		mMeetingInstance.getAttendance().reject(name);
		updateAgendaEntryInFile(name, MeetingConstants.ATTENDANCE_REJECTED);
	}
	
	public MeetingInstanceModel getModels() {
		return mMeetingInstance;
	}
	
	public void open() throws IOException {
		if (mServer.getClientOutputStreamByName(mMeetingInstance.getRequest().getRequesterName()) != null) {
			streamOutRequester = new DataOutputStream(
					new BufferedOutputStream(mServer.getClientOutputStreamByName(mMeetingInstance.getRequest().getRequesterName())
					));
		}
		
		streamOutParticipantNames = mMeetingInstance.getRequest().getParticipantNames();
		streamOutParticipants = new DataOutputStream[streamOutParticipantNames.length];
		for (int i = 0; i < streamOutParticipantNames.length; i++) {
			if(mServer.getClientOutputStreamByName(streamOutParticipantNames[i]) != null) {
				streamOutParticipants[i] =
					new DataOutputStream(
							new BufferedOutputStream(
									mServer.getClientOutputStreamByName(streamOutParticipantNames[i])
									)
							);
			} else {
				unknownParticipantNames.add(streamOutParticipantNames[i] + Constants.SEPERATOR_STATUS + i);
				streamOutParticipants[i] = null;
			}
		}
	}

	public void close() throws IOException {
		/*if (streamOutRequester != null)
			streamOutRequester.close();
		for (int i = 0; i < streamOutParticipants.length; i++) {
			if (streamOutParticipants[i] != null) {
				streamOutParticipants[i].close();
			}
		}*/
	}
}
