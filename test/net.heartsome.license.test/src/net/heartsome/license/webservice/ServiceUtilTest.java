package net.heartsome.license.webservice;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import net.heartsome.license.ProtectionFactory;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.Encrypt;
import net.heartsome.license.utils.StringUtils;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxy;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.codehaus.xfire.transport.http.EasySSLProtocolSocketFactory;
import org.codehaus.xfire.util.dom.DOMOutHandler;

public class ServiceUtilTest {
	private static final String SERVICE_URL = "https://lic.heartsome.net/hswebservices/licenses";
	private static final String SERVICE_NAMESPACE = "licenses.XFire";
	private static final String SERVICE_NAME = "licenses";
	private static final String HTTP_TYPE = "https";
	private static final int PORT = 443;

	public static IService getService() throws MalformedURLException {
		// Service srvcModel = new
		// ObjectServiceFactory().create(IService.class);
		// XFireProxyFactory factory = new XFireProxyFactory(XFireFactory
		// .newInstance().getXFire());
		//
		// IService srvc = (IService) factory.create(srvcModel,
		// Constants.CONNECT_URL);
		// return srvc;

		ProtocolSocketFactory easy = new EasySSLProtocolSocketFactory();
		Protocol protocol = new Protocol(HTTP_TYPE, easy, PORT);
		Protocol.registerProtocol(HTTP_TYPE, protocol);
		Service serviceModel = new ObjectServiceFactory().create(
				IService.class, SERVICE_NAME, SERVICE_NAMESPACE, null);

		IService service = (IService) new XFireProxyFactory().create(
				serviceModel, SERVICE_URL);
		Client client = ((XFireProxy) Proxy.getInvocationHandler(service))
				.getClient();
		client.addOutHandler(new DOMOutHandler());
		client.setProperty(CommonsHttpMessageSender.GZIP_ENABLED, Boolean.FALSE);
		client.setProperty(CommonsHttpMessageSender.DISABLE_EXPECT_CONTINUE,
				"1");
		client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, "0");

		return service;
	}

	public static String active(String licenseId, String maccode,
			String installcode) throws Exception {
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		String s = licenseId + Constants.SEPARATOR
				+ ProtectionFactory.getSeries() + Constants.SEPARATOR
				+ installcode;
		Encrypt en = ProtectionFactory.getEncrypt();
		byte[] k = en.encrypt(serverPublicKey, s.getBytes());
		String str = srvc.activeLicense(StringUtils.toHexString(k),
				en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		// System.out.println(new String(b));
		if (Constants.RETURN_INVALIDLICENSE.equals(new String(b))) {
			return Constants.RETURN_INVALIDLICENSE;
		} else if (Constants.RETURN_INVALIDBUNDLE.equals(new String(b))) {
			return Constants.RETURN_INVALIDBUNDLE;
		} else if (Constants.RETURN_DBEXCEPTION.equals(new String(b))) {
			return Constants.RETURN_DBEXCEPTION;
		} else if (Constants.RETURN_MUTILTEMPBUNDLE.equals(new String(b))) {
			return Constants.RETURN_MUTILTEMPBUNDLE;
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(new String(b))) {
			return Constants.RETURN_EXPIREDLICENSE;
		} else {
			return "Active Success";
		}
	}

	public static String cancel(String licenseId, String maccode,
			String installcode) throws Exception {
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		Encrypt en = ProtectionFactory.getEncrypt();
		String info = licenseId + Constants.SEPARATOR
				+ ProtectionFactory.getSeries() + Constants.SEPARATOR
				+ installcode;
		String str = srvc.logoutLicense(
				StringUtils.toHexString(en.encrypt(serverPublicKey,
						info.getBytes())), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		info = new String(b);
		// System.out.println(info);
		// if (Constants.RETURN_LOGOUTSUCESS.equals(info)) {
		// return Constants.LOGOUT_SUCCESS;
		// } else if (Constants.RETURN_INVALIDLICENSE.equals(info)) {
		// return Constants.LOGOUT_FAIL;
		// } else {
		// return Constants.LOGOUT_FAIL;
		// }
		return info;
	}

	public static String check(String licenseId, String maccode,
			String installcode) throws Exception {
		IService srvc = ServiceUtil.getService();
		byte[] serverPublicKey = srvc.getServerPublicKey();
		Encrypt en = ProtectionFactory.getEncrypt();
		String info = licenseId + Constants.SEPARATOR
				+ ProtectionFactory.getSeries() + Constants.SEPARATOR
				+ installcode;

		String str = srvc.checkLicense(
				StringUtils.toHexString(en.encrypt(serverPublicKey,
						info.getBytes())), en.getPublicKey());
		byte[] b = StringUtils.toBytes(str);
		b = en.decrypt(en.getPrivateKey(), b);
		info = new String(b);
		if (Constants.RETURN_CHECKSUCESS.equals(info)) {
			return "Check Success";
		} else if (Constants.RETURN_EXPIREDLICENSE.equals(info)) {
			return "Expired";
		} else {
			return "Invalid";
		}
	}

	// private static String[] getStrFromInfo(String info) {
	// return info.split(Constants.SEPARATOR);
	// }

	// public static void main(String[] argv) {
	// try {
	// int result = ServiceUtilTest.active("111111111111111111111111",
	// "000000000011", "11111111111111111111");
	// if (result == Constants.ACTIVE_OK_INT) {
	// System.out.println("激活成功");
	// } else {
	// System.out.println("激活失败");
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("激活失败");
	// }
	//
	// try {
	// int result = ServiceUtilTest.cancel("111111111111111111111111",
	// "000000000011", "11111111111111111111");
	// if (result == Constants.LOGOUT_SUCCESS) {
	// System.out.println("取消激活成功");
	// } else {
	// System.out.println("取消激活失败");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("取消激活失败");
	// }
	// }
}
