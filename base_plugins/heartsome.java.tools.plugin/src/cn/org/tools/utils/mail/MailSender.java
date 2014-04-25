/**
 * MailSender.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import cn.org.tools.utils.string.StringUtilsBasic;

/**
 * The Class MailSender.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class MailSender {

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

	/** The SS l_ factory. */
	final String strSslFactory = "javax.net.ssl.SSLSocketFactory";

	private HtmlEmail email;

	/**
	 * 构造方法.
	 * @param host
	 *            邮件服务器，如:"mail.heartsome.net"
	 * @param userName
	 *            邮箱用户名
	 * @param password
	 *            邮箱密码
	 */
	public MailSender(String host, String userName, String password) {
		this(host, -1, userName, password);
	}

	/**
	 * 构造方法.
	 * @param host
	 *            邮件服务器，如:"mail.heartsome.net"
	 * @param port
	 *            端口号
	 * @param userName
	 *            邮箱用户名
	 * @param password
	 *            邮箱密码
	 */
	public MailSender(String host, int port, String userName, String password) {
		this(host, "smtp", port, userName, password, false);
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
	public MailSender(String host, String protocol, int port, String userName, String password, boolean ssl) {
		props = new Properties();
		if (port != -1) {
			this.port = port;
		}
		this.userName = userName;
		this.password = password;
		props.setProperty("mail." + protocol + ".auth", "true");
		props.setProperty("mail.transport.protocol", protocol);
		props.setProperty("mail.host", host);
		props.setProperty("mail." + protocol + ".port", "" + this.port);
		createSession();
		email = new HtmlEmail();
		email.setCharset("utf-8");
		email.setMailSession(session);
		if (ssl) {
			email.setSSL(true);
		}
	}

	/**
	 * 创建 session.
	 */
	private void createSession() {
		session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		session.setDebug(true);
	}

	/**
	 * 设置发件人
	 * @param from
	 * 			发件人邮箱地址
	 * @throws MessagingException
	 * @throws EmailException
	 */
	public void setFrom(String from) throws MessagingException, EmailException {
		if (from == null) {
			return;
		}
		Address address = new InternetAddress(from);
		Map<String, String> map = getEmailInfo(address.toString());
		email.setFrom(map.get("email"), map.get("name"));
	}

	/**
	 * 设置收件人，如果有多个收件人，用逗号分隔
	 * @param to
	 * 			收件人邮箱地址
	 * @throws MessagingException
	 * @throws EmailException
	 */
	public void setTo(String to) throws MessagingException, EmailException {
		if (to == null) {
			return;
		}
		Address[] address = InternetAddress.parse(to);
		for (Address i : address) {
			Map<String, String> map = getEmailInfo(i.toString()); 
			email.addTo(map.get("email"), map.get("name"));
		}
	}

	/**
	 * 设置抄送人，如果有多个抄送人，用逗号分隔
	 * @param cc
	 * 			抄送人邮箱地址
	 * @throws MessagingException
	 * @throws EmailException ;
	 */
	public void setCC(String cc) throws MessagingException, EmailException {
		if (cc == null) {
			return;
		}
		Address[] address = InternetAddress.parse(cc);
		for (Address i : address) {
			Map<String, String> map = getEmailInfo(i.toString()); 
			email.addCc(map.get("email"), map.get("name"));
		}
	}

	/**
	 * 设置密送人，如果有多个密送人，用逗号分隔
	 * @param bcc
	 * 			密送人邮箱地址
	 * @throws MessagingException
	 * @throws EmailException ;
	 */
	public void setBCC(String bcc) throws MessagingException, EmailException {
		if (bcc == null) {
			return;
		}
		Address[] address = InternetAddress.parse(bcc);
		for (Address i : address) {
			Map<String, String> map = getEmailInfo(i.toString()); 
			email.addBcc(map.get("email"), map.get("name"));
		}
	}

	/**
	 * 设置回复人，如果有多个回复人，用逗号分隔
	 * @param reply
	 * @throws MessagingException
	 * @throws EmailException ;
	 */
	public void setReplyTo(String reply) throws MessagingException, EmailException {
		if (reply == null) {
			return;
		}
		Address[] address = InternetAddress.parse(reply);
		for (Address i : address) {
			Map<String, String> map = getEmailInfo(i.toString()); 
			email.addReplyTo(map.get("email"), map.get("name"));
		}
	}

	/**
	 * 设置主题
	 * @param subject
	 * 				主题内容
	 * @throws MessagingException ;
	 */
	public void setSubject(String subject) throws MessagingException {
		if (subject == null) {
			return;
		}
		email.setSubject(subject);
	}

	/**
	 * 设置发送日期
	 * @param date
	 * 			发送日期
	 * @throws MessagingException ;
	 */
	public void setSentDate(Date date) throws MessagingException {
		if (date == null) {
			return;
		}
		email.setSentDate(date);
	}

	/**
	 * 设置要发送的附件
	 * @param attachment
	 * 				附件数组
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws EmailException ;
	 */
	public void setMixMail(File[] attachment) throws MessagingException, UnsupportedEncodingException, EmailException {
		if (attachment != null) {
			for (File file : attachment) {
				EmailAttachment attach = new EmailAttachment();
				attach.setPath(file.getAbsolutePath());
				attach.setName(MimeUtility.encodeText(file.getName()));
				email.attach(attach);
			}
		}
	}

	/**
	 * 设置要发送的附件,并对附件重命名
	 * @param attachment
	 * 				附件的集合，key 为附件的名称，value为附件
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws EmailException ;
	 */
	public void setMixMailRename(Map<String, File> attachment) throws MessagingException, UnsupportedEncodingException,
			EmailException {
		if (attachment != null) {
			for (String fileName : attachment.keySet()) {
				EmailAttachment attach = new EmailAttachment();
				attach.setPath(attachment.get(fileName).getAbsolutePath());
				attach.setName(MimeUtility.encodeText(fileName));
				email.attach(attach);
			}
		}
	}

	/**
	 * 设置邮件内容
	 * @param text
	 * 				html格式的邮件内容
	 * @throws MessagingException
	 * @throws EmailException ;
	 */
	public void setHtmlText(String text) throws MessagingException, EmailException {
		if (StringUtilsBasic.checkNull(text)) {
			email.setHtmlMsg(text);
		}
	}

	/**
	 * 设置邮件消息
	 * @param text
	 * 				消息内容
	 * @throws MessagingException
	 * @throws EmailException ;
	 */
	public void setText(String text) throws MessagingException, EmailException {
		if (StringUtilsBasic.checkNull(text)) {
			email.setMsg(text);
		}
	}

	/**
	 * 发送邮件
	 * @throws EmailException ;
	 */
	public void send() throws EmailException {
		email.send();
	}

	/**
	 * 过滤邮件中的收件人，抄送人，密送人中重复的邮箱地址，而只保留一条
	 * @throws AddressException
	 * @throws EmailException ;
	 */
	@SuppressWarnings("unchecked")
	public void removeDumplicate() throws AddressException, EmailException {
		Address from = email.getFromAddress();
		List<Address> toList = MailUtils.removeDuplicate(from, email.getToAddresses());
		List<Address> ccList = MailUtils.removeDuplicate(from, email.getCcAddresses());
		List<Address> bccList = MailUtils.removeDuplicate(from, email.getBccAddresses());
		ccList = MailUtils.removeDuplicate(toList, ccList);
		bccList = MailUtils.removeDuplicate(toList, bccList);
		bccList = MailUtils.removeDuplicate(ccList, bccList);

		if (toList != null && toList.size() > 0) {
			email.setTo(toList);
		}
		if (ccList != null && ccList.size() > 0) {
			email.setCc(ccList);
		}
		if (bccList != null && bccList.size() > 0) {
			email.setBcc(bccList);
		}
	}
	
	/**
	 * 解析由前台传过来的“姓名 <地址>”字符串，得到姓名和地址键值对
	 * @param str
	 * 				“姓名 <地址>”字符串
	 * @return Map&lt;String, String&gt;
	 * 				name 和 email 键值对;
	 */
	private Map<String, String> getEmailInfo(String str) {
		int temp = str.indexOf("<");
		int temp1 = str.indexOf(">");
		Map<String, String> map = new HashMap<String, String>();
		if (temp != -1 && temp1 != -1) {
			String name = str.substring(0, temp).trim();
			String email = str.substring(temp + 1, temp1).trim();
			map.put("name", name);
			map.put("email", email);
		} else {
			int temp2 = str.indexOf("@");
			if (temp2 != -1) {
				String name = str.substring(0, temp2).trim();
				map.put("name", name);
				map.put("email", str);
			}
		}
		return map;
	}
}
