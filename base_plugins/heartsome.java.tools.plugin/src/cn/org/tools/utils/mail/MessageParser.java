/**
 * MessageParser.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.mail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The Class MessageParser.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class MessageParser {

	/** The message. */
	private Message message;

	/** The text buffer. */
	private StringBuffer textBuffer;

	/** The max arrow. */
	private int maxArrow = 0;

	/** The text charset. */
	private String textCharset = "UTF-8";

	/** The html resource. */
	private ResourceFileBean htmlResource;

	/**
	 * 构造方法.
	 * @param message
	 *            Message 对象
	 */
	public MessageParser(Message message) {
		this.message = message;
	}

	/**
	 * 获得消息的发件人.
	 * @return String
	 * 			  发件人地址 
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public String getFrom() throws MessagingException {
		return InternetAddress.toString(message.getFrom());
	}

	/**
	 * 获得收件人地址信息.
	 * @return String
	 * 				收件人地址
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public String getTo() throws MessagingException {
		return InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
	}

	/**
	 * 获得抄送人地址信息.
	 * @return String
	 * 				抄送人地址
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public String getCc() throws MessagingException {
		return InternetAddress.toString(message.getRecipients(Message.RecipientType.CC));
	}

	/**
	 * 获得回复人地址信息.
	 * @return String
	 * 				回复人地址
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public String gerReplayTo() throws MessagingException {
		return InternetAddress.toString(message.getReplyTo());
	}

	/**
	 * 获得消息的主题.
	 * @return String
	 * 				主题内容.
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public String getSubject() throws MessagingException {
		return message.getSubject();
	}

	/**
	 * 获取消息附件中的文件.
	 * @return List&lt;ResourceFileBean&gt;
	 * 				文件的集合(每个 ResourceFileBean 对象中存放文件名和 InputStream 对象)
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<ResourceFileBean> getFiles() throws MessagingException, IOException {
		List<ResourceFileBean> resourceList = new ArrayList<ResourceFileBean>();
		Object content = message.getContent();
		Multipart mp = null;
		if (content instanceof Multipart) {
			mp = (Multipart) content;
		} else {
			return resourceList;
		}
		for (int i = 0, n = mp.getCount(); i < n; i++) {
			Part part = mp.getBodyPart(i);
			//此方法返回 Part 对象的部署类型。
			String disposition = part.getDisposition();
			//Part.ATTACHMENT 指示 Part 对象表示附件。
			//Part.INLINE 指示 Part 对象以内联方式显示。
			if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))))) {
				//part.getFileName():返回 Part 对象的文件名。
				String fileName = MimeUtility.decodeText(part.getFileName());
				//此方法为 Part 对象返回一个 InputStream 对象
				InputStream is = part.getInputStream();
				resourceList.add(new ResourceFileBean(fileName, is));
			} else if (disposition == null) {
				//附件也可以没有部署类型的方式存在
				getRelatedPart(part, resourceList);
			}
		}
		return resourceList;
	}

	/**
	 * 获取消息附件中的文件.
	 * @param part
	 *            Part 对象
	 * @param resourceList
	 *            文件的集合(每个 ResourceFileBean 对象中存放文件名和 InputStream 对象)
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getRelatedPart(Part part, List<ResourceFileBean> resourceList) throws MessagingException, IOException {
		//验证 Part 对象的 MIME 类型是否与指定的类型匹配。
		if (part.isMimeType("multipart/related")) {
			Multipart mulContent = (Multipart) part.getContent();
			for (int j = 0, m = mulContent.getCount(); j < m; j++) {
				Part contentPart = mulContent.getBodyPart(j);
				if (!contentPart.isMimeType("text/*")) {
					String fileName = "Resource";
					//此方法返回 Part 对象的内容类型。
					String type = contentPart.getContentType();
					if (type != null) {
						type = type.substring(0, type.indexOf("/"));
					}
					fileName = fileName + "[" + type + "]";
					if (contentPart.getHeader("Content-Location") != null
							&& contentPart.getHeader("Content-Location").length > 0) {
						fileName = contentPart.getHeader("Content-Location")[0];
					}
					InputStream is = contentPart.getInputStream();
					resourceList.add(new ResourceFileBean(fileName, is));
				}
			}
		} else {
			Multipart mp = null;
			Object content = part.getContent();
			if (content instanceof Multipart) {
				mp = (Multipart) content;
			} else {
				return;
			}
			for (int i = 0, n = mp.getCount(); i < n; i++) {
				Part body = mp.getBodyPart(i);
				getRelatedPart(body, resourceList);
			}
		}
	}

	/**
	 * 取得纯文本的邮件内容，如果邮件是 HTML 格式的，则会过滤 HTML 标签后返回。.
	 * @return String
	 * 				纯文本的邮件内容
	 */
	public String getPlainText() {
		try {
			String content = getText(message);
			return htmlToText(content);
		} catch (MessagingException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 取得文本内容，如果邮件是 HTML 格式的，则会返回所有 HTML 标签.
	 * @param p
	 *            Part 对象
	 * @return String
	 * 			  文本内容
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getText(Part p) throws MessagingException, IOException {

		if (p.isMimeType("text/*")) {
			if (p.isMimeType("text/html")) {
				htmlResource = new ResourceFileBean("html-file.html", p.getInputStream());
			}
			String s = (String) p.getContent();
			s = MimeUtility.decodeText(s);
			String contenttype = p.getContentType();
			String[] types = contenttype.split(";");
			for (String i : types) {
				if (i.toLowerCase().indexOf("charset") > 0) {
					String[] values = i.split("=");
					if (values != null && values.length >= 2) {
						String charSet = values[1].trim();
						if (charSet.indexOf("\"") != -1 || charSet.indexOf("\'") != -1) {
							charSet = charSet.substring(1, charSet.length() - 1);
						}
						textCharset = charSet;
					}
				}
			}
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = getText(bp);
					}
					break;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null) {
						return s;
					}
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}

			}
		}
		return null;
	}

	/**
	 * 将 html 格式的文本过滤掉标签.
	 * @param html
	 *            html 格式的字符串
	 * @return String
	 * 			  过滤掉 html 标签后的文本。如果 html 为空，返回空串""
	 */
	private String htmlToText(String html) {
		if (html == null) {
			return "";
		}
		DOMFragmentParser parser = new DOMFragmentParser();
		CoreDocumentImpl codeDoc = new CoreDocumentImpl();
		InputSource inSource = new InputSource(new ByteArrayInputStream(html.getBytes()));
		inSource.setEncoding(textCharset);
		DocumentFragment doc = codeDoc.createDocumentFragment();

		try {
			parser.parse(inSource, doc);
		} catch (Exception e) {
			return "";
		}

		textBuffer = new StringBuffer();
		processNode(doc);
		return textBuffer.toString();
	}

	/**
	 * 遍历节点 node 以过滤所有的 html 标签.
	 * @param node
	 *            要遍历的节点
	 */
	private void processNode(Node node) {
		if (node == null) {
			return;
		}
		if ("BR".equals(node.getNodeName())) {
			textBuffer.append("\n" + newLine(node));
		} else if ("TR".equals(node.getNodeName())) {
			textBuffer.append("\n" + newLine(node));
		} else if ("DIV".equals(node.getNodeName())) {
			textBuffer.append("\n" + newLine(node));
		} else if ("BLOCKQUOTE".equals(node.getNodeName())) {
			if (node.getAttributes() != null) {
				Node item = node.getAttributes().getNamedItem("type");
				if (item != null) {
					if ("cite".equalsIgnoreCase(item.getNodeValue())) {
						textBuffer.append("\n" + newLine(node));
					}
				}
			}
		} else if ("LI".equals(node.getNodeName())) {
			textBuffer.append("\n" + newLine(node) + "    ");
		}
		if (node.getNodeType() == Node.TEXT_NODE) {
			String value = node.getNodeValue();
			if (value != null) {
				if (value.indexOf("\n") != -1 && "".equals(value.trim())) {
					return;
				} else if (value.indexOf("\n") != -1) {
					if (!"BODY".equals(node.getParentNode().getNodeName())) {
						value = value.replaceAll("\n", "");
					}
				}
			}
			textBuffer.append(value);
		} else if (node.hasChildNodes()) {
			NodeList childList = node.getChildNodes();
			int childLen = childList.getLength();

			for (int count = 0; count < childLen; count++) {
				processNode(childList.item(count));
			}
		} else {
			return;
		}
	}

	/**
	 * 生成一个新行.
	 * @param node
	 *            节点
	 * @return String
	 * 				由 > 组成的字符串
	 */
	private String newLine(Node node) {
		Node parentNode = node;
		int count = 0;
		while (parentNode != null) {
			String parentName = parentNode.getNodeName();
			if (parentNode.getAttributes() == null) {
				parentNode = parentNode.getParentNode();
				continue;
			}
			Node namedItem = parentNode.getAttributes().getNamedItem("type");
			if (namedItem == null) {
				parentNode = parentNode.getParentNode();
				continue;
			}
			if ("BLOCKQUOTE".equals(parentName)) {
				if ("cite".equalsIgnoreCase(namedItem.getNodeValue())) {
					count++;
				}
			}
			parentNode = parentNode.getParentNode();
		}
		if (count > maxArrow || count == 0) {
			maxArrow = count;
		}
		StringBuffer arrow = new StringBuffer();
		for (int i = 0; i < maxArrow; i++) {
			if (i == maxArrow - 1) {
				arrow.append("> ");
			} else {
				arrow.append(">");
			}
		}
		return arrow.toString();
	}

	/**
	 * 获取消息对象.
	 * @return Message
	 * 				消息对象.	
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * 获取 ResourceFileBean 对象.
	 * @return ResourceFileBean
	 * 				ResourceFileBean 对象
	 */
	public ResourceFileBean getHtmlResource() {
		return htmlResource;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		Properties prep = new Properties();
		Session session = Session.getInstance(prep);
		try {
			Message msg = new MimeMessage(session, new FileInputStream("/data/terry/Desktop/email.eml"));
			MessageParser parser = new MessageParser(msg);
			System.out.println(parser.getPlainText());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
