/**
 * MailReceiver.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.mail;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

/**
 * The Class MailReceiver.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class MailReceiver {

	/** The port. */
	private int port = 25;

	/** The user name. */
	private String userName;

	/** The password. */
	private String password;

	/** The props. */
	private Properties props;

	/** The session. */
	private Session session;

	/** The folder list. */
	private List<Folder> folderList;

	/** The store. */
	private Store store;

	/** The SSL factory. */
	final String strSslFactory = "javax.net.ssl.SSLSocketFactory";

	/**
	 * 使用该方法创建对象时，默认使用 POP3 邮件协议，不使用 SSL 安全协议.
	 * @param host
	 *            邮件服务器，如:"mail.heartsome.net"
	 * @param userName
	 *            邮箱用户名
	 * @param password
	 *            邮箱密码
	 */
	public MailReceiver(String host, String userName, String password) {
		this(host, "pop3", 110, userName, password, false);
	}

	/**
	 * 构造方法.
	 * @param host
	 *            邮件服务器，如:"mail.heartsome.net"
	 * @param protocol
	 *            邮件协议
	 * @param port
	 *            端口号
	 * @param userName
	 *            邮箱用户名
	 * @param password
	 *            邮箱密码
	 * @param ssl
	 *            是否应用 SSL 安全协议
	 */
	public MailReceiver(String host, String protocol, int port, String userName, String password, boolean ssl) {
		folderList = new ArrayList<Folder>();
		props = new Properties();
		if (port != -1) {
			this.port = port;
		}
		this.userName = userName;
		this.password = password;
		props.setProperty("mail.host", host);
		props.setProperty("mail.store.protocol", protocol);
		props.setProperty("mail." + protocol + ".port", "" + this.port);
		if (ssl) {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			props.setProperty("mail." + protocol + ".socketFactory.class", strSslFactory);
			props.setProperty("mail." + protocol + ".socketFactory.fallback", "false");
			props.setProperty("mail." + protocol + ".socketFactory.port", "" + this.port);
		}
		createSession();
	}

	/**
	 * 创建 Session
	 */
	private void createSession() {
		session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
	}

	/**
	 * 接收邮件.
	 * @param expunge
	 *            	邮件是否删除
	 * @return List&lt;Message&gt;
	 * 				收到的邮件集合
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public List<Message> receive(boolean expunge) throws MessagingException {
		List<Message> result = new ArrayList<Message>();
		store = session.getStore();
		store.connect();
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		for (Message message : folder.getMessages()) {
			result.add(message);
			//设置邮件的 DELETED 标志
			message.setFlag(Flags.Flag.DELETED, expunge);
		}
		folderList.add(folder);
		return result;
	}

	/**
	 * 释放资源.
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public void releaseResource() throws MessagingException {
		for (Folder folder : folderList) {
			folder.close(true);
		}
		if (store != null) {
			store.close();
		}
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public static void main(String[] args) throws MessagingException {
		MailReceiver receiver = new MailReceiver("mail.heartsome.net", "pop3", 110, "dv_test9@heartsome.net",
				"BxKd7T00", false);
		receiver.receive(false);
	}

}
