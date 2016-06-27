package phohawkenics.threads;

import java.io.*;
import java.net.Socket;

import phohawkenics.common.Constants;
import phohawkenics.common.MeetingConstants;
import phohawkenics.models.ClientModel;
import phohawkenics.utils.ClientUtil;
import phohawkenics.utils.LogUtil;
import phohawkenics.utils.RoomUtil;

public class ClientListenerThread extends Thread {
	private ClientModel mClient = null;
	private RBMS server = null;
	private Socket socket = null;
	private int ID = -1;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;

	public ClientListenerThread(RBMS _server, Socket _socket) {
		super();
		server = _server;
		socket = _socket;
		ID = socket.getPort();
	}

	public void send(String msg) {
		try {
			streamOut.writeUTF(msg);
			streamOut.flush();
		} catch (IOException ioe) {
			LogUtil.cout(ID + " ERROR sending: " + ioe.getMessage());
			server.removeClient(ID);
			interrupt();
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public int getID() {
		return ID;
	}
	
	public ClientModel getClient () {
		return mClient;
	}
	
	public void setClient(ClientModel client) {
		mClient = client;
	}

	public void run() {
		LogUtil.cout("Server Thread " + ID + " running.");
		
		while (mClient == null && !isInterrupted()) {
			try {
				server.handleLogin(ID, streamIn.readUTF());
			} catch (IOException ioe) {
				LogUtil.cout(" ERROR reading name input: " + ioe.getMessage());
				server.removeClient(ID);
				interrupt();
			}
		}
		
		if (mClient != null) {
			instantiateClientGUI();
		}
		
		while (!isInterrupted()) {
			try {
				server.handle(mClient.getName() ,ID, streamIn.readUTF());
			} catch (IOException ioe) {
				LogUtil.cout(ID + " ERROR reading: " + ioe.getMessage());
				server.removeClient(ID);
				interrupt();
			}
		}
	}
	
	public void instantiateClientGUI() {
		send("Hello " + mClient.getName());
		send(MeetingConstants.MSG_TYPE_AGENDA 
				+ Constants.SEPERATOR_CLIENT_TRIGGER + ClientUtil.getClientAgenda(mClient.getName()));
		send(MeetingConstants.MSG_TYPE_ROOMS 
				+ Constants.SEPERATOR_CLIENT_TRIGGER 
				+ RoomUtil.getRoomIDs());
		send(MeetingConstants.MSG_TYPE_NAMES 
				+ Constants.SEPERATOR_CLIENT_TRIGGER 
				+ ClientUtil.getClientNames());
		send(MeetingConstants.MSG_TYPE_MESSAGE_BANK 
				+ Constants.SEPERATOR_CLIENT_TRIGGER 
				+ ClientUtil.getMessageBankAndDelete(mClient.getName()));
	}

	public void open() throws IOException {
		streamIn = new DataInputStream(new BufferedInputStream(
				socket.getInputStream()));
		streamOut = new DataOutputStream(new BufferedOutputStream(
				socket.getOutputStream()));
	}

	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}
}