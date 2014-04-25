package net.heartsome.license.webservice;

import java.net.MalformedURLException;

import net.heartsome.license.LicenseReader;
import net.heartsome.license.ProtectionFactory;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.Encrypt;
import net.heartsome.license.encrypt.InstallKeyEncrypt;
import net.heartsome.license.generator.IKeyGenerator;
import net.heartsome.license.generator.KeyGeneratorImpl;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.utils.StringUtils;

import java.lang.reflect.Proxy;

import org.codehaus.xfire.client.XFireProxy;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.codehaus.xfire.transport.http.EasySSLProtocolSocketFactory;
import org.codehaus.xfire.util.dom.DOMOutHandler;

public class ServiceUtil {
	private static final String SERVICE_URL ="https://lic.heartsome.net/hswebservices/licenses";
	private static final String SERVICE_NAMESPACE = "licenses.XFire";   
	private static final String SERVICE_NAME = "licenses";   
	private static final String HTTP_TYPE = "https";   
	private static final int PORT = 443; 

	public static IService getService() throws MalformedURLException {
//		Service srvcModel = new ObjectServiceFactory().create(IService.class);
//		XFireProxyFactory factory = new XFireProxyFactory(XFireFactory
//				.newInstance().getXFire());
//
//		IService srvc = (IService) factory.create(srvcModel, Constants.CONNECT_URL);
//		return srvc;
		
		ProtocolSocketFactory easy = new EasySSLProtocolSocketFactory();  
        Protocol protocol = new Protocol(HTTP_TYPE, easy, PORT);  
        Protocol.registerProtocol(HTTP_TYPE, protocol);  
        Service serviceModel = new ObjectServiceFactory().create(IService.class,  
        		SERVICE_NAME, SERVICE_NAMESPACE, null);  
        
    	IService service = (IService) new XFireProxyFactory().create(serviceModel, SERVICE_URL);  
    	Client client = ((XFireProxy)Proxy.getInvocationHandler(service)).getClient();  
        client.addOutHandler(new DOMOutHandler());  
        client.setProperty(CommonsHttpMessageSender.GZIP_ENABLED, Boolean.FALSE);  
        client.setProperty(CommonsHttpMessageSender.DISABLE_EXPECT_CONTINUE, "1");  
        client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, "0"); 
        
        return service;
	}
	
	public static int active(String licenseId) throws Exception{
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		
		IKeyGenerator gen = new KeyGeneratorImpl();
		byte[] k = gen.generateKey(licenseId, serverPublicKey);
		Encrypt en = ProtectionFactory.getEncrypt();
		String str = srvc.activeLicense(StringUtils.toHexString(k), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
//		System.out.println(new String(b));
		if (Constants.RETURN_INVALIDLICENSE.equals(new String(b))) {
			return Constants.RETURN_INVALIDLICENSE_INT;
		} else if (Constants.RETURN_INVALIDBUNDLE.equals(new String(b))) {
			return Constants.RETURN_INVALIDBUNDLE_INT;
		} else if (Constants.RETURN_DBEXCEPTION.equals(new String(b))) {
			return Constants.RETURN_DBEXCEPTION_INT;
		} else if (Constants.RETURN_MUTILTEMPBUNDLE.equals(new String(b))) {
			return Constants.RETURN_MUTILTEMPBUNDLE_INT;
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(new String(b))) {
			return Constants.RETURN_EXPIREDLICENSE_INT;
		} else {
			FileUtils.writeFile(b, ProtectionFactory.getFileName(1, Constants.PRODUCTID));
			FileUtils.writeFile(InstallKeyEncrypt.encrypt(gen.getInstallKey().getBytes()), ProtectionFactory.getFileName(2, 89));
			if (FileUtils.isExsit()) {
				return Constants.ACTIVE_OK_INT;
			} else {
				return Constants.RETURN_DBEXCEPTION_INT;
			}
		}
	}
	
	public static int check(String info, byte[] b) throws Exception {
		if (info == null) {
			LicenseReader reader = new LicenseReader(Constants.PRODUCTID);
			info = reader.getLicenseInfo();
		}
		
		if (info == null) {
			return Constants.STATE_INVALID;
		}
		
		String[] arrInfo = getStrFromInfo(info);
		String strKeyCode = arrInfo[0];
//		String strMacCode = arrInfo[1];
		String strInstallCode = arrInfo[2];
		
		if (b == null) {
			b = FileUtils.readFile(ProtectionFactory.getFileName(2, Constants.PRODUCTID));
			try {
				b = InstallKeyEncrypt.decrypt(b);
				if (!strInstallCode.equals(new String(b))) {
					return Constants.STATE_INVALID;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Constants.STATE_INVALID;
			}
		}
		
		if (b == null) {
			return Constants.STATE_INVALID;
		}
		
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		IKeyGenerator gen = new KeyGeneratorImpl();
		byte[] k = gen.generateKey(strKeyCode, strInstallCode, serverPublicKey);
		
		Encrypt en = ProtectionFactory.getEncrypt();
		
		String str = srvc.checkLicense(StringUtils.toHexString(k), en.getPublicKey());
		b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		info = new String(b);
		if (Constants.RETURN_CHECKSUCESS.equals(info)) {
			return Constants.STATE_VALID;
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(info)) {
			return Constants.STATE_EXPIRED;
		} else {
			return Constants.STATE_INVALID;
		}
	}
	
	public static int cancel() throws Exception {
		LicenseReader reader = new LicenseReader(Constants.PRODUCTID);
		String info = reader.getLicenseInfo();
		if (info == null) {
			return Constants.LOGOUT_FAIL;
		}
		
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		Encrypt en = ProtectionFactory.getEncrypt();
		String str = srvc.logoutLicense(StringUtils.toHexString(en.encrypt(serverPublicKey, info.getBytes())), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		info = new String(b);
		if (Constants.RETURN_LOGOUTSUCESS.equals(info)) {
			return FileUtils.removeFile() ? Constants.LOGOUT_SUCCESS : Constants.LOGOUT_FAIL;
		} else if (Constants.RETURN_INVALIDLICENSE.equals(info)) {
			return FileUtils.removeFile() ? Constants.LOGOUT_SUCCESS : Constants.LOGOUT_FAIL;
		} else {
			return Constants.LOGOUT_FAIL;
		}
	}
	
	private static String[] getStrFromInfo(String info) {
		return info.split(Constants.SEPARATOR);
	}
	
	public static String getTempEndDate(String key) throws Exception {
		if (key == null) {
			return null;
		}
		
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		Encrypt en = ProtectionFactory.getEncrypt();
		String str = srvc.getTempEndDate(StringUtils.toHexString(en.encrypt(serverPublicKey, key.getBytes())), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		key = new String(b);
		if (Constants.RETURN_NULLTEMPENDDATE.equals(key)) {
			return null;
		} else {
			return key;
		}
	}
}
