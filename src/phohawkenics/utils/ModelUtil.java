package phohawkenics.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import phohawkenics.common.Constants;
import phohawkenics.common.DatabaseConstants;

public class ModelUtil extends FileUtil{
	
	public static void addNewEntryModels (String ID) {
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DatabaseConstants.FILE_NAME_MODELS, true)))) {
		    out.println(DatabaseConstants.KEY_ID + ID);
			for(int i = DatabaseConstants.MODELS_OFFSET; i < DatabaseConstants.MODELS_ROWS_PER_ENTRY; i++) {
				out.println("");
			}
			out.close();
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
	
	public static String[] getPrimaryIDModels() {
		String modelsInfo[] = FileUtil.fileToArray(DatabaseConstants.FILE_NAME_MODELS);
		String temp = Constants.KEY_EMPTY;
		for(int i = 0; i < modelsInfo.length; i ++) {
			if (modelsInfo[i].contains(DatabaseConstants.KEY_ID)) {
				temp += modelsInfo[i].replace(DatabaseConstants.KEY_ID, Constants.KEY_EMPTY) + Constants.SEPERATOR_MULTIPLE;
			}
		}
		
		if (temp.length() == 0) {
			return new String[0];
		} else {
			return temp.substring(0, temp.length() - 1).split(Constants.SEPERATOR_MULTIPLE);
		}
	}
}
