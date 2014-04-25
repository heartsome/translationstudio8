/**
 * MailUtils.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.mail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * The Class MailUtils.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class MailUtils {
	
	/**
	 * 构造方法.
	 */
	protected MailUtils() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/**
	 * 用来取得 Email 地址的友好称呼。
	 * @param emailAddress
	 *            	Email 地址
	 * @return String
	 * 				例如： emailAddress="John &lt;john@test.com&gt;" 就返回 John。
	 */

	public static String getAddressName(String emailAddress) {
		String result = "";
		if (emailAddress == null) {
			return result;
		}
		try {
			InternetAddress address = new InternetAddress(emailAddress);
			String text = address.getPersonal();
			if (text == null) {
				result = emailAddress;
			} else {
				result = text;
			}
		} catch (AddressException e) {
			e.printStackTrace();
			return emailAddress;
		}
		return result;
	}

	/**
	 * 用来取得 Email 地址。
	 * @param emailAddress
	 *            the email address
	 * @return String
	 * 				例如： emailAddress="John &lt;john@test.com&gt;" 就返回 john@test.com
	 */
	public static String getAddress(String emailAddress) {
		String result = "";
		if (emailAddress == null) {
			return result;
		}
		try {
			InternetAddress address = new InternetAddress(emailAddress);
			String text = address.getAddress();
			if (text == null) {
				result = emailAddress;
			} else {
				result = text;
			}
		} catch (AddressException e) {
			e.printStackTrace();
			return emailAddress;
		}
		return result;
	}

	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param message
	 *            邮件对象
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public static void removeDumplicate(Message message) throws MessagingException {
		Address[] from = message.getFrom();
		Address[] to = message.getRecipients(Message.RecipientType.TO);
		Address[] cc = message.getRecipients(Message.RecipientType.CC);
		Address[] bcc = message.getRecipients(Message.RecipientType.BCC);
		Address[] tonew = removeDuplicate(from, to);
		Address[] ccnew = removeDuplicate(from, cc);
		Address[] bccnew = removeDuplicate(from, bcc);
		if (tonew != null) {
			message.setRecipients(Message.RecipientType.TO, tonew);
		}
		if (ccnew != null) {
			message.setRecipients(Message.RecipientType.CC, ccnew);
		}
		if (bccnew != null) {
			message.setRecipients(Message.RecipientType.BCC, bccnew);
		}
	}

	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param from
	 *            发件人,多个发件人以逗号分隔
	 * @param to
	 *            收件人,多个收件人以逗号分隔
	 * @return Address[]
	 * 			  收件人数组中过滤掉重复的发件人信息后剩余的集合
	 * @throws AddressException
	 *            解析发件人或收件人失败时抛出该异常
	 */
	public static Address[] removeDuplicate(String from, String to) throws AddressException {
		return removeDuplicate(InternetAddress.parse(from), InternetAddress.parse(to));
	}

	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param from
	 *            发件人数组
	 * @param to
	 *            收件人
	 * @return Address[]
	 * 			  收件人数组中过滤掉重复的发件人信息后剩余的集合
	 * @throws AddressException
	 *            解析收件人失败时抛出该异常
	 */
	public static Address[] removeDuplicate(Address[] from, String to) throws AddressException {
		return removeDuplicate(from, InternetAddress.parse(to));
	}

	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param from
	 * 			发件人地址
	 * @param to
	 * 			收件人地址集合
	 * @return List&lt;Address&gt;
	 * 				收件人数组中过滤掉重复的发件人信息后剩余的集合。如果 from 或 to 为 null，返回 to
	 * @throws AddressException
	 * 			    解析失败时抛出该异常
	 */
	public static List<Address> removeDuplicate(Address from, List<Address> to) throws AddressException {
		if (from == null) {
			return to;
		}
		Address[] result = new Address[to.size()];
		Address[] removed = removeDuplicate(new Address[]{from}, to.toArray(result));
		if (removed == null) {
			return to;
		}
		return Arrays.asList(removed);
	}
	
	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param from
	 * 			发件人集合
	 * @param to
	 * 			收件人集合
	 * @return List&lt;Address>&gt;
	 * 			收件人数组中过滤掉重复的发件人信息后剩余的集合。如果 from 为 null，返回 to；如果 to 为 null，返回 null
	 */
	public static List<Address> removeDuplicate(List<Address> from, List<Address> to) {
		if (from == null) {
			return to;
		}
		if (to == null) {
			return null;
		}
		Address[] fromArray = new Address[from.size()];
		Address[] toArray = new Address[to.size()];
		from.toArray(fromArray);
		to.toArray(toArray);
		Address[] result = removeDuplicate(fromArray, toArray);
		return Arrays.asList(result);
	}
	
	/**
	 * 过滤邮件中的 From 和 To，使邮件不允许发件人和收件人一样.
	 * @param from
	 *            发件人数组
	 * @param to
	 *            收件人数组
	 * @return Address[]
	 * 			  收件人数组中过滤掉重复的发件人信息后剩余的集合。如果 from 为 null，返回 to；如果 to 为 null，返回 null
	 */
	public static Address[] removeDuplicate(Address[] from, Address[] to) {
		if (from == null) {
			return to;
		}
		if (to == null) {
			return null;
		}
		Set<Address> fromSet = new HashSet<Address>();
		Set<Address> toSet = new HashSet<Address>();
		for (Address address : from) {
			fromSet.add(address);
		}
		for (Address address : to) {
			toSet.add(address);
		}
		toSet.removeAll(fromSet);
		InternetAddress[] address = new InternetAddress[toSet.size()];
		toSet.toArray(address);
		return address;
	}

	/**
	 * 检查 email 地址是否有效(此方法无意义，请勿使用该方法，应该删除或者重新写逻辑).
	 * @param address
	 *            要检查的 email 地址
	 * @return boolean
	 * 				true:地址有效
	 */
	public static boolean isValidAddress(String address) {
		return true;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws AddressException
	 *             the address exception
	 */
	public static void main(String[] args) throws AddressException {
		System.out.println(new InternetAddress("a <c> asdf").getAddress());
	}

}
