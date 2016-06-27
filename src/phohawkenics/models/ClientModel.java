package phohawkenics.models;

import phohawkenics.common.Constants;
import phohawkenics.threads.ClientListenerThread;
import phohawkenics.utils.ClientUtil;

public class ClientModel {
	//private ClientListenerThread mClientThread;
	private String mName;
	private String mPassword;
	private boolean mFlag = true;

	
	public ClientModel(ClientListenerThread clientThread, String name, String password) {
		//mClientThread = clientThread;
		mName = name;
		mPassword = password;
		if (ClientUtil.isUniqueName(name))
			ClientUtil.addNewEntryClients(mName, mPassword);
		else
			mFlag = false;
	}
	
	public ClientModel(ClientListenerThread clientThread, String name) {
		//mClientThread = clientThread;
		String temp = ClientUtil.getClientsStringByName(name);
		if (temp.equals(Constants.KEY_EMPTY)) {
			mFlag = false;
		} else {
			String clientInfo[] = temp.split(Constants.SEPERATOR_LINE);
			if (clientInfo.length > 0)
				mName = clientInfo[0];
			if (clientInfo.length > 1)
				mPassword = clientInfo[1];
			if (clientInfo.length > 2) {
				// possible array 
			}
		}
	}
	
	public String getName() {
		return mName;
	}
	public String getPassword() {
		return mPassword;
	}
	public boolean getFlag() {
		return mFlag;
	}
	
	public boolean clientIsUnavailable(String name, String dateTime) {
		String temp[] = dateTime.split(Constants.SEPERATOR_INPUT);
		return ClientUtil.clientIsUnvailable(name, temp[0], temp[1]);
	}
}
