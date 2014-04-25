package net.heartsome.license.constants;

public class Constants {
	
	// 信息的分隔符，和ws服务器的分隔符保持一致，两处需要同时修改。net.heartsome.r8.webservice.utils.Constants
	public final static String SEPARATOR = "#&";
	
	public final static String RETURN_INVALIDLICENSE = "It is a invalid License";
	public final static int RETURN_INVALIDLICENSE_INT = 1; 
	public final static String RETURN_INVALIDBUNDLE = "It is a invalid Bundle License";
	public final static int RETURN_INVALIDBUNDLE_INT = 2;
	public final static String RETURN_DBEXCEPTION = "Update database exception";
	public final static int RETURN_DBEXCEPTION_INT = 3;
	public final static int ACTIVE_OK_INT = 4;
	public final static String RETURN_MUTILTEMPBUNDLE = "Mutil Active Trial License";
	public final static int RETURN_MUTILTEMPBUNDLE_INT = 5;
	
	public final static String RETURN_EXPIREDLICENSE = "Your license has expired";
	public final static int RETURN_EXPIREDLICENSE_INT = 6;
	public final static String RETURN_MACCODEERR = "MacCode is invalid";
	public final static String RETURN_CHECKSUCESS = "Check is Sucess";
	
	public final static int CANCEL = -1;
	public final static int STATE_FILE_NOT_EXSIT = 0;
	public final static int STATE_NOT_ACTIVATED = 1;
	public final static int STATE_VALID = 2;
	public final static int STATE_INVALID = 3;
	public final static int STATE_EXPIRED = 4;
	public final static int STATE_UNCONNECT = 5;
	
	public final static String RETURN_LOGOUTSUCESS = "Logout is sucess";
	public final static int LOGOUT_SUCCESS = 1;
	public final static int LOGOUT_FAIL = 2;
	
	public final static int PRODUCTID = 89;
	
	public final static String TYPE_TMEP = "0"; 
	public final static String TYPE_BUSINESS = "1"; 
	
	public final static String CONNECT_URL = "http://192.168.0.191:8080/r8/services/licenseManager";
	
	public final static String RETURN_NULLTEMPENDDATE = "TempEndDate is null";
	

}
