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

import org.codehaus.xfire.XFireFactory;
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
import org.eclipse.swt.widgets.ProgressBar;

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
	
	public static int active(String licenseId, ProgressBar bar) throws Exception {
		boolean result = FileUtils.writeFile(new byte[] {'1','1'}, ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		if (!result) {
			return Constants.EXCEPTION_INT8;
		} else {
			FileUtils.removeFile(ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		}
		
		IService srvc = ServiceUtil.getService();
		bar.setSelection(1);
		
		String series = ProtectionFactory.getSeries();
		if (series == null || "".equals(series)) {
			return Constants.EXCEPTION_INT5;
		}
		
		byte[] serverPublicKey = srvc.getServerPublicKey();
		bar.setSelection(2);
		
		IKeyGenerator gen = new KeyGeneratorImpl();
		byte[] k = gen.generateKey(licenseId, series, serverPublicKey);
		if (k == null) {
			return Constants.EXCEPTION_INT9;
		}
		bar.setSelection(3);
		Encrypt en = ProtectionFactory.getEncrypt();
		bar.setSelection(4);
		String str = srvc.activeLicense(StringUtils.toHexString(k), en.getPublicKey(), ProtectionFactory.getPlatform());
		bar.setSelection(5);
		bar.setSelection(6);
		byte[] b = StringUtils.toBytes(str);
		bar.setSelection(7);
		b = en.decrypt(en.getPrivateKey(), b);
		if (b == null) {
			return Constants.EXCEPTION_INT7;
		}
		bar.setSelection(8);
		
		String info = new String(b);
		if (Constants.RETURN_INVALIDLICENSE.equals(info)) {
			return Constants.RETURN_INVALIDLICENSE_INT;
		} else if (Constants.RETURN_INVALIDBUNDLE.equals(info)) {
			return Constants.RETURN_INVALIDBUNDLE_INT;
		} else if (Constants.RETURN_DBEXCEPTION.equals(info)) {
			return Constants.EXCEPTION_INT13;
		} else if (Constants.RETURN_MUTILTEMPBUNDLE.equals(info)) {
			return Constants.RETURN_MUTILTEMPBUNDLE_INT;
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(info)) {
			return Constants.RETURN_EXPIREDLICENSE_INT;
		} else if (Constants.RETURN_STOPLICENSE.equals(info)) {
			return Constants.RETURN_STOPLICENSE_INT;
		} else {
			result = FileUtils.writeFile(b, ProtectionFactory.getFileName(1, Constants.PRODUCTID));
			if (!result) {
				return Constants.EXCEPTION_INT12;
			}
			bar.setSelection(9);
			b = InstallKeyEncrypt.encrypt(StringUtils.handle(gen.getInstallKey(), 1, 3, 2).getBytes());
			if (b == null) {
				return Constants.EXCEPTION_INT10;
			}
			result = FileUtils.writeFile(b, ProtectionFactory.getFileName(2, Constants.PRODUCTID));
			if (!result) {
				return Constants.EXCEPTION_INT11;
			}
			bar.setSelection(10);
			
			System.getProperties().setProperty("TSState", "true");
			return Constants.ACTIVE_OK_INT;
		}
	}
	
	public static int check(String licenseId, String macCode, byte[] b) throws Exception {	
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		IKeyGenerator gen = new KeyGeneratorImpl();
		byte[] k = gen.generateKey(licenseId, macCode, new String(b), serverPublicKey);
		if (k == null) {
			return Constants.EXCEPTION_INT9;
		}
		
		Encrypt en = ProtectionFactory.getEncrypt();
		
		String str = srvc.checkLicense(StringUtils.toHexString(k), en.getPublicKey());
		b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		if (b == null) {
			return Constants.EXCEPTION_INT7;
		}
		String info = new String(b);
		if (Constants.RETURN_CHECKSUCESS.equals(info)) {
			return Constants.STATE_VALID;
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(info)) {
			return Constants.STATE_EXPIRED;
		} else if (Constants.RETURN_MACCODEERR.equals(info)) { 
			return Constants.EXCEPTION_INT15;
		} else {
			return Constants.STATE_INVALID;
		}
	}
	
	public static int cancel() throws Exception {
		byte[] t = FileUtils.readFile(ProtectionFactory.getFileName(1, Constants.PRODUCTID));
		if (t == null) {
			return Constants.LOGOUT_FAIL;
		}
		
		t = new LicenseReader().getLicenseInfo(t);
		if (t == null) {
			return Constants.LOGOUT_FAIL;
		}
		
		t = StringUtils.reverse(new String(t), 1, 5, 2).getBytes();
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		Encrypt en = ProtectionFactory.getEncrypt();
		String str = srvc.logoutLicense(StringUtils.toHexString(en.encrypt(serverPublicKey, t)), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		String info = new String(b);
		if (Constants.RETURN_LOGOUTSUCESS.equals(info)) {
			return FileUtils.removeFile() ? Constants.LOGOUT_SUCCESS : Constants.LOGOUT_FAIL;
		} else if (Constants.RETURN_INVALIDLICENSE.equals(info)) {
			return FileUtils.removeFile() ? Constants.LOGOUT_SUCCESS : Constants.LOGOUT_FAIL;
		} else {
			return Constants.LOGOUT_FAIL;
		}
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
