package phohawkenics.threads;

import java.net.*;	
import java.io.*;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.models.ClientModel;
import phohawkenics.models.MeetingInstanceModel;
import phohawkenics.utils.ClientUtil;
import phohawkenics.utils.LogUtil;
import phohawkenics.utils.ModelDetailsUtil;
import phohawkenics.utils.ModelUtil;

public class RBMS implements Runnable {
	private ClientListenerThread mClients[] = new ClientListenerThread[50];
	private MeetingInstanceThread mMeetings[] = new MeetingInstanceThread[50];
	private ServerSocket mServer = null;
	private Thread mThread = null;
	private int mClientCount = 0;
	private int mMeetingCount = 0;

	public RBMS(int port) {
		try {
			LogUtil.cout("Binding to port " + port + ", please wait  ...");
			mServer = new ServerSocket(port);
			LogUtil.cout("Server started: " + mServer);
			start();
		} catch (IOException ioe) {
			LogUtil.cout("Can not bind to port " + port + ": " + ioe.getMessage());
		}
	}

	private synchronized void addThread(Socket socket) {
		if (mClientCount < mClients.length) {
			LogUtil.cout("Client accepted: " + socket);
			mClients[mClientCount] = new ClientListenerThread(this, socket);
			try {
				mClients[mClientCount].open();
				mClients[mClientCount].start();
				mClientCount++;
			} catch (IOException ioe) {
				LogUtil.cout("Error opening thread: " + ioe);
			}
		} else
			LogUtil.cout("Client refused: maximum " + mClients.length
					+ " reached.");
	}

	private synchronized void addAction(int ID, String input) {
		if (mMeetingCount < mMeetings.length) {
			MeetingConstants.TYPES actionType = MeetingConstants.decodeInput(input);
			
			LogUtil.cout("Action Started (" + mClients[findClient(ID)].getClient().getName() + "): " + input) ;
			
			if (actionType == MeetingConstants.TYPES.REQUEST) {
				if (MeetingInstanceModel.validRequestFormat(input)) {
					mMeetings[mMeetingCount] = new MeetingInstanceThread(ID, input, this);		
					try {
						mMeetings[mMeetingCount].open();
						mMeetings[mMeetingCount].start();
						mMeetingCount++;
					} catch (IOException ioe) {
						LogUtil.cout("Error opening thread: " + ioe);
					}
				} else {
					LogUtil.cout(
							MeetingConstants.MSG_TYPE_REQUEST
							+ Constants.SEPERATOR_CLIENT_TRIGGER
							+"Error with request format"
						);
				}
			} else if (actionType == MeetingConstants.TYPES.ACCEPT
					|| actionType == MeetingConstants.TYPES.REJECT){
				processAcceptReject(ID, input, actionType);
			} else if (actionType == MeetingConstants.TYPES.WITHDRAW
					|| actionType == MeetingConstants.TYPES.ADD) {
				processWithdrawAdd(ID, input, actionType);
			} else if (actionType == MeetingConstants.TYPES.CANCEL) {
				 processCancel(ID, input);
			} else if (actionType == MeetingConstants.TYPES.MEETINGDETAILS) {
				String msgInfo[] = input.split(Constants.SEPERATOR_INPUT);
				if (msgInfo.length == 2) {
					mClients[findClient(ID)].send(MeetingConstants.MSG_TYPE_MEETING_DETAILS
							+ Constants.SEPERATOR_CLIENT_TRIGGER 
							+ ModelDetailsUtil.getModelDetailsStringByID(msgInfo[1]));
				}
			}
		} else {
			LogUtil.cout("Client refused: maximum " + mMeetings.length + " reached.");
		}
	}

	private synchronized void addAction(String ID) {
		mMeetings[mMeetingCount] = new MeetingInstanceThread(ID, this);		
		try {
			mMeetings[mMeetingCount].open();
			mMeetings[mMeetingCount].start();
			mMeetingCount++;
		} catch (IOException ioe) {
			LogUtil.cout("Error opening thread: " + ioe);
		}
	}

	private void processAcceptReject(int ID, String input, MeetingConstants.TYPES actionType) {
		String meetingNumber = input.split(Constants.SEPERATOR_INPUT)[1];
		int clientNumber = findClient(ID);
		int actionNumber = findActionByMeetingNumber(meetingNumber);
		if (actionNumber == -1) {
			LogUtil.cout("Invalid meeting number");
			mClients[clientNumber].send(
					MeetingConstants.MSG_TYPE_REQUEST
					+ Constants.SEPERATOR_CLIENT_TRIGGER
					+ "You have entered an invalid meeting number"
				);
		} else {
			MeetingInstanceThread action = mMeetings[findActionByMeetingNumber(meetingNumber)];
			String name = mClients[clientNumber].getClient().getName();
			if (actionType == MeetingConstants.TYPES.ACCEPT)
				action.confirmParticipant(name);
			else if (actionType == MeetingConstants.TYPES.REJECT)
				action.rejectParticipant(name);
		}
	}

	private void processWithdrawAdd(int ID, String input, MeetingConstants.TYPES actionType) {
		String name = input.split(" ")[1];
		String requestNumber = input.split(" ")[2];
		int clientNumber = findClient(ID);
		int actionNumber = findActionByMeetingNumber(requestNumber);
		if (actionNumber == -1) {
			if (actionType == MeetingConstants.TYPES.ADD) {
				ModelDetailsUtil.updateMeetingStatus(requestNumber,
						MeetingConstants.STATUS_TEMP
						+ Constants.SEPERATOR_CLIENT_TRIGGER 
						+ name + Constants.SEPERATOR_INPUT
						+ MeetingConstants.ATTENDANCE_ADDED
					);
			} else if (actionType == MeetingConstants.TYPES.WITHDRAW) {
				ModelDetailsUtil.updateMeetingStatus(requestNumber,
						MeetingConstants.STATUS_TEMP
						+ Constants.SEPERATOR_CLIENT_TRIGGER 
						+ name + Constants.SEPERATOR_INPUT
						+ MeetingConstants.ATTENDANCE_WITHDRAWED
					);
			}
			addAction(requestNumber);
		} else {
			MeetingInstanceThread action = mMeetings[findActionByMeetingNumber(requestNumber)];
			name = mClients[clientNumber].getClient().getName();
			if (actionType == MeetingConstants.TYPES.ADD)
				action.confirmParticipant(name);
			else if (actionType == MeetingConstants.TYPES.WITHDRAW)
				action.rejectParticipant(name);
		}
	}
	
	private void processCancel(int ID, String input) {
		String requestNumber = input.split(" ")[1];
		int actionNumber = findActionByMeetingNumber(requestNumber);
		if (actionNumber == -1) {
			ModelDetailsUtil.updateMeetingStatus(requestNumber,
					MeetingConstants.STATUS_CANCELLED
				);
			addAction(requestNumber);
		} else {
			LogUtil.cout("Cannot cancel, meeting has not been confirmed yet!");
		}
	}

	public void initialize() {
		String[] modelIDs = ModelUtil.getPrimaryIDModels();
		for (String ID: modelIDs) {
			String status = ModelDetailsUtil.getStatus(ID);
			if (!status.equals(MeetingConstants.STATUS_CANCELLED)
					&& !status.equals(MeetingConstants.STATUS_COMPLETED))
				addAction(ID);
		}
	}

	public void run() {
		LogUtil.cout("Initializing..");
		initialize();
		while (mThread != null) {
			try {
				if (mClientCount == 0) {
					LogUtil.cout("Waiting for a client ...");
				} else {
					LogUtil.cout("Active connections");
					for(int i = 0; i < mClientCount; i++)
						LogUtil.cout(String.valueOf(mClients[i].getID()));
				}
				addThread(mServer.accept());
			} catch (IOException ioe) {
				LogUtil.cout("Server accept error: " + ioe);
				stop();
			}
		}
	}

	public void start() {
		if (mThread == null) {
			mThread = new Thread(this);
			mThread.start();
		}
	}

	public void stop() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	
	public synchronized void handle(String clientName, int ID, String input) {
		MeetingConstants.TYPES actionType;
		if (input.equals(Constants.KEY_EXIT)) {
			mClients[findClient(ID)].send(Constants.KEY_EXIT);
			removeClient(ID);
		} else {
			actionType = MeetingConstants.decodeInput(input);
			if (actionType == MeetingConstants.TYPES.OTHER)
				for (int i = 0; i < mClientCount; i++)
					mClients[i].send(clientName + ": " + input);
			else
				addAction(ID ,input);
		}
	}

	public synchronized void handleLogin(int ID, String input) {
		int clientPosition = findClient(ID);
		ClientListenerThread clientThread = mClients[clientPosition];
		if (input.equals(Constants.KEY_EXIT)) {
			clientThread.send(Constants.KEY_EXIT);
			removeClient(ID);
		} else {
			String loginActionInfo[] = input.split(Constants.SEPERATOR_INPUT); 
			if (loginActionInfo[0].equals("LOGIN")) {
				if (findClient(loginActionInfo[1]) == -1) {
					ClientModel clientModel = new ClientModel(clientThread, loginActionInfo[1]);
					if (clientModel.getFlag()) {
						clientThread.setClient(clientModel);
						clientThread.send("Login Successful");
					} else
						clientThread.send("User does not exist");
				} else {
					clientThread.send("User already logged in elsewhere");
				}
			} else {
				ClientModel clientModel = new ClientModel(clientThread, loginActionInfo[1], loginActionInfo[2]);
				if (clientModel.getFlag()) {
					clientThread.setClient(clientModel);
					clientThread.send("Registration Successful");
					for (int i = 0; i < mClientCount - 1; i++)
						if (mClients[i].getName() != null)
							mClients[i].send(MeetingConstants.MSG_TYPE_NAMES + Constants.SEPERATOR_CLIENT_TRIGGER + ClientUtil.getClientNames());
				} else
					clientThread.send("User already registered");
			}
		}
	}

	public synchronized void removeClient(int ID) {
		int pos = findClient(ID);
		if (pos >= 0) {
			ClientListenerThread toTerminate = mClients[pos];
			LogUtil.cout("Removing client thread " + ID + " at " + pos);
			for (int i = 0; i < mMeetingCount; i++) {
				mMeetings[i].participantExiting(mClients[pos].getClient().getName());
			}
			if (pos < mClientCount - 1)
				for (int i = pos + 1; i < mClientCount; i++)
					mClients[i - 1] = mClients[i];
			mClientCount--;
			try {
				toTerminate.close();
			} catch (IOException ioe) {
				LogUtil.cout("Error closing thread: " + ioe);
			}
			
			toTerminate.interrupt();
		}
	}

	public synchronized void removeAction(String requestNumber) {
		int pos = findActionByRequestNumber(requestNumber);
		if (pos >= 0) {
			MeetingInstanceThread toTerminate = mMeetings[pos];
			LogUtil.cout("Removing Request #: " + requestNumber + " at " + pos);
			if (pos < mMeetingCount - 1)
				for (int i = pos + 1; i < mMeetingCount; i++)
					mMeetings[i - 1] = mMeetings[i];
			mMeetingCount--;
			try {
				toTerminate.close();
			} catch (IOException ioe) {
				LogUtil.cout("Error closing thread: " + ioe);
			}
			toTerminate.interrupt();
		}
	}

	public ClientListenerThread getClientThreadByID(int ID) {
		if (findClient(ID) != -1)
			return mClients[findClient(ID)];
		else
			return null;
	}
	
	public ClientListenerThread getClientThreadByName(String name) {
		if (findClient(name) != -1)
			return mClients[findClient(name)];
		else
			return null;
	}
	
	public OutputStream getClientOutputStreamByName(String clientName) {
		if (findClient(clientName) != -1) {
			try {
				return mClients[findClient(clientName)].getSocket().getOutputStream();
			} catch (IOException e) {
				LogUtil.cout("Error retrieving client sockt: " + e.getMessage());
			}
			return null;
		} else 
			return null;
	}
	
	public int findActionByRequestNumber(String requestNumber) {
		for (int i = 0; i < mMeetingCount; i++)
			if (mMeetings[i].getModels().getRequest().getRequestNumber().equals(requestNumber))
				return i;
		return -1;
	}
	
	public int findActionByMeetingNumber(String meetingNumber) {
		for (int i = 0; i < mMeetingCount; i++)
			if (mMeetings[i].getModels().getInvite().getMeetingNumber().equals(meetingNumber))
				return i;
		return -1;
	}
	
	public int findClient(String clientName) {
		for (int i = 0; i < mClientCount; i++)
			if (mClients[i].getClient() != null) {
				if (mClients[i].getClient().getName().equalsIgnoreCase(clientName))
					return i;
			}
		return -1;
	}
	
	public int findClient(int ID) {
		for (int i = 0; i < mClientCount; i++)
			if (mClients[i].getID() == ID)
				return i;
		return -1;
	}
	
	public static void main(String args[]) {
		@SuppressWarnings("unused")
		RBMS server = null;
		/*if (args.length != 1)
			LogUtil.cout("Usage: java RBMS port");
		else
			server = new RBMS(Integer.parseInt(args[0]));*/
		server = new RBMS(9999);
	}
}