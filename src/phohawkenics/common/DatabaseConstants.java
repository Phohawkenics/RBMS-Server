package phohawkenics.common;

public class DatabaseConstants {
	public static String KEY_ID = "ID=";
	public static String KEY_NAME = "NAME=";
	public static String FILE_NAME_MODELS = "Models.txt";
	public static String FILE_NAME_MODEL_DETAILS = "Model_Details.txt";
	public static String FILE_NAME_ROOMS = "Rooms.txt";
	public static String FILE_NAME_CLIENTS = "Clients.txt";
	
	public static int MODELS_ROWS_PER_ENTRY = 1;
	public static int MODELS_OFFSET = 0;
	
	public static int DETAILS_STATUS_ROW = 1;
	public static int DETAILS_REQUEST_ROW = 2;
	public static int DETAILS_INVITATION_ROW = 3;
	public static int DETAILS_ATTENDANCE_ROW = 4;
	public static int DETAILS_ROWS_PER_ENTRY = 4;
	public static int DETAILS_OFFSET = 2;
	
	public static int ROOMS_RESERVATION_ROW = 1;
	public static int ROOMS_ROWS_PER_ENTRY = 1;
	public static int ROOMS_OFFSET = 1;
	
	public static int CLIENTS_PASSWORD_ROW = 1;
	public static int CLIENTS_AGENDA_ROW = 2;
	public static int CLIENTS_MESSAGE_BANK_ROW = 3;
	public static int CLIENTS_ROWS_PER_ENTRY = 3;
	public static int CLIENTS_OFFSET = 1;
}
