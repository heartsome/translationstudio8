package net.heartsome.license.constants;

public class Constants {
	
	// 信息的分隔符，和ws服务器的分隔符保持一致，两处需要同时修改。net.heartsome.r8.webservice.utils.Constants
	public final static String SEPARATOR = "#&";
	
	public final static String RETURN_INVALIDLICENSE = "It is a invalid License";
	public final static int RETURN_INVALIDLICENSE_INT = 1; 
	public final static String RETURN_INVALIDBUNDLE = "It is a invalid Bundle License";
	public final static int RETURN_INVALIDBUNDLE_INT = 2;
	public final static String RETURN_DBEXCEPTION = "Update database exception";
	public final static String RETURN_STOPLICENSE = "It is a stop License";
	public final static int RETURN_STOPLICENSE_INT = 3;
	public final static int ACTIVE_OK_INT = 4;
	public final static String RETURN_MUTILTEMPBUNDLE = "Mutil Active Trial License";
	public final static int RETURN_MUTILTEMPBUNDLE_INT = 5;
	
	public final static String RETURN_EXPIREDLICENSE = "Your license has expired";
	public final static int RETURN_EXPIREDLICENSE_INT = 6;
	public final static String RETURN_MACCODEERR = "MacCode is invalid";
	public final static String RETURN_CHECKSUCESS = "Check is Sucess";
	
	public final static int EXCEPTION_INT1 = 21;
	public final static String EXCEPTION_STRING1 = "LIC001";
	public final static int EXCEPTION_INT2 = 22;
	public final static String EXCEPTION_STRING2 = "LIC002";
	public final static int EXCEPTION_INT3 = 23;
	public final static String EXCEPTION_STRING3 = "LIC003";
	public final static int EXCEPTION_INT4 = 24;
	public final static String EXCEPTION_STRING4 = "LIC004";
	public final static int EXCEPTION_INT5 = 25;
	public final static String EXCEPTION_STRING5 = "LIC005";
	public final static int EXCEPTION_INT6 = 26;
	public final static String EXCEPTION_STRING6 = "LIC006";
	public final static int EXCEPTION_INT7 = 27;
	public final static String EXCEPTION_STRING7 = "LIC007";
	public final static int EXCEPTION_INT8 = 28;
	public final static String EXCEPTION_STRING8 = "LIC008";
	public final static int EXCEPTION_INT9 = 29;
	public final static String EXCEPTION_STRING9 = "LIC009";
	public final static int EXCEPTION_INT10 = 30;
	public final static String EXCEPTION_STRING10 = "LIC010";
	public final static int EXCEPTION_INT11 = 31;
	public final static String EXCEPTION_STRING11 = "LIC011";
	public final static int EXCEPTION_INT12 = 32;
	public final static String EXCEPTION_STRING12 = "LIC012";
	public final static int EXCEPTION_INT13 = 33;
	public final static String EXCEPTION_STRING13 = "LIC013";
	public final static int EXCEPTION_INT14 = 34;
	public final static String EXCEPTION_STRING14 = "LIC014";
	public final static int EXCEPTION_INT15 = 35;
	public final static String EXCEPTION_STRING15 = "LIC015";
	public final static int EXCEPTION_INT16 = 36;
	public final static String EXCEPTION_STRING16 = "LIC016";
	public final static int EXCEPTION_INT17 = 37;
	public final static String EXCEPTION_STRING17 = "LIC017";
	
	public final static int CANCEL = -1;
	public final static int STATE_FILE_NOT_EXSIT = 0;
	public final static int STATE_NOT_ACTIVATED = 1;
	public final static int STATE_VALID = 2;
	public final static int STATE_INVALID = 3;
	public final static int STATE_EXPIRED = 4;
	
	public final static String RETURN_LOGOUTSUCESS = "Logout is sucess";
	public final static int LOGOUT_SUCCESS = 1;
	public final static int LOGOUT_FAIL = 2;
	
	public final static String PRODUCTID = System.getProperty("TSVersion");
	
	public final static String TYPE_TMEP = "0"; 
	public final static String TYPE_BUSINESS = "1"; 
	
	public final static String CONNECT_URL = "http://192.168.0.191:8080/r8/licenses";
	
	public final static String RETURN_NULLTEMPENDDATE = "TempEndDate is null";
	

}
