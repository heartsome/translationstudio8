package net.heartsome.cat.ts.ui.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 将 MARTIF 文件转换为 TBX 文件（参考了 R7 中 net.heartsome.plugin.martif2tbx.model.Martif2Tbx 
 * 类的代码，将其改成了 VTD 实现）
 * @author peason
 * @version
 * @since JDK1.6
 */
public class Martif2Tbx {

	/** tbx 标准中的语言属性名称 */
	private static String tbxLangDescriptor = "xml:lang";

	/** martif 标准中的语言属性名称 */
	private static String martifLangDescriptor = "lang";

	/** id 属性名称 */
	private static String idDescriptor = "id";

	/** TBX 文件输出流 */
	private FileOutputStream fos;

	/**
	 * 构造方法
	 */
	public Martif2Tbx() {

	}

	/**
	 * 创建 TBX 文件，初始化 fos 对象
	 * @param mainLanguage
	 *            语言代码
	 * @param tbxFilePath
	 *            tbx 文件路径
	 * @throws Exception
	 *             ;
	 */
	private void createDocument(String mainLanguage, String tbxFilePath) throws Exception {
		fos = new FileOutputStream(tbxFilePath);
		writeString("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		writeString("<!DOCTYPE martif PUBLIC \"TBXcoreStructureDTD-v-1-0.DTT\" >\n");
		String str = "<martif type=\"TBX\" ";
		if (mainLanguage == null) {
			mainLanguage = "";
		}
		str += tbxLangDescriptor + "=\"" + mainLanguage + "\"";
		str += ">\n";
		writeString(str);
		writeString("<martifHeader>\n");
	}

	private void writeString(String input) throws UnsupportedEncodingException, IOException {
		fos.write(input.getBytes("UTF-8"));
	}

	/**
	 * 删除 martif 文件中的 Doctype
	 * @param input
	 *            martif 文件路径
	 * @param output
	 *            临时文件路径
	 * @throws Exception
	 *             ;
	 */
	private void removeDocTypeDeclaration(String input, String output) throws Exception {
		FileInputStream is = new FileInputStream(input);
		FileOutputStream os = new FileOutputStream(output);
		int size = is.available();
		byte[] array = new byte[size];
		is.read(array);
		String file = new String(array, "UTF-8");
		// remove xml declaration and doctype
		int begin = file.indexOf("<" + "martif");
		if (begin != -1) {
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>".getBytes("UTF-8"));
			os.write(file.substring(begin).getBytes("UTF-8"));
		} else {
			throw new Exception(Messages.getString("util.Martif2Tbx.msg1"));
		}
		is.close();
		os.close();
	}

	/**
	 * 将 martif 文件转换为 TBX 文件
	 * @param filename
	 *            martif 路径及文件名
	 * @param output
	 *            tbx 文件路径及文件名
	 * @throws Exception
	 *             ;
	 */
	public void convertFile(String filename, String output) throws Exception {
		File temp = File.createTempFile("tmp", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		removeDocTypeDeclaration(filename, temp.getAbsolutePath());
		VTDGen vg = new VTDGen();
		if (vg.parseFile(temp.getAbsolutePath(), true)) {
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/martif");
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("util.Martif2Tbx.msg1"));
			}
			String lang = vu.getCurrentElementAttribut(martifLangDescriptor, null);
			createDocument(lang, output);
			ap.selectXPath("/martif/martifHeader");
			if (ap.evalXPath() != -1) {
				vn.push();
				String martifRevisionDesc = vu.getChildContent("revisionDesc");
				if (martifRevisionDesc != null) {
					writeString("<tbxRevisionDesc>\n");
					writeString(martifRevisionDesc);
					writeString("</tbxRevisionDesc>\n");
				}
				vn.pop();
				vn.push();
				String martifDatabaseDesc = vu.getChildContent("databaseDesc");
				if (martifDatabaseDesc != null) {
					writeString("<tbxdatabaseDesc>\n");
					writeString(martifDatabaseDesc);
					writeString("</tbxdatabaseDesc>\n");
				}
				writeString("</martifHeader>\n");
				vn.pop();
			}
			ap.selectXPath("/martif/text/body/termEntry");
			writeString("<text>\n");
			writeString("<body>\n");
			AutoPilot ap3 = new AutoPilot(vn);
			while (ap.evalXPath() != -1) {
				String id = vu.getCurrentElementAttribut(idDescriptor, null);
				String termEntry = "<termEntry ";
				if (id != null) {
					termEntry += idDescriptor + "=\"" + id + "\"";
				}
				termEntry += ">\n";
				writeString(termEntry);
				vn.push();
				ap3.selectXPath("./*");
				while (ap3.evalXPath() != -1) {
					String name = vu.getCurrentElementName();
					if (name.equals("note")) {
						String note = vu.getElementFragment();
						if (note != null) {
							writeString(note);
							writeString("\n");
						}
					} else if (name.equals("langSet")) {
						String language = vu.getCurrentElementAttribut(martifLangDescriptor, "");
						writeString("<langSet " + tbxLangDescriptor + "=\"" + language + "\">\n");
						AutoPilot ap4 = new AutoPilot(vn);
						ap4.selectXPath("./ntig/termGrp/*");
						writeString("<ntig>\n");
						writeString("<termGrp>\n");
						while (ap4.evalXPath() != -1) {
							String nodeName = vu.getCurrentElementName();
							if (nodeName.equals("term")) {
								writeString(vu.getElementFragment());
								writeString("\n");
							} else {
								String type = vu.getCurrentElementAttribut("type", "");
								if (!type.equals("")) {
									writeString("<termNote type=\"" + type + "\">\n");
									writeString(vu.getElementContent());
									writeString("</termNote>\n");
								}
							}
						}
						writeString("</termGrp>\n");
						writeString("</ntig>\n");
						writeString("</langSet>\n");
					} else {
						// String xpath2 = "./*[not(name()='note' or name()='langSet')]";
						int index = -1;
						if ((index = vn.getAttrVal("type")) != -1) {
							String type = vn.toString(index);
							String content = getChildElementPureText(vn);
							writeString("<descrip type=\"" + type + "\">\n");
							writeString(content);
							writeString("</descrip>\n");
						}
					}
				}
				writeString("</termEntry>\n");
				vn.pop();
			}
			writeString("</body>\n");
			writeString("</text>\n");
			writeString("</maritif>");
		}
	}
	
	/**
	 * 得到当前子节点的纯文本。实体会被转义。若无文本内容返回 null。
	 * @exception XPathParseException
	 *                ,XPathEvalException,NavException
	 */
	public String getChildElementPureText(VTDNav vn) throws XPathParseException, XPathEvalException, NavException {
		String txtNode = ".//text()";
		AutoPilot ap = new AutoPilot(vn);
		StringBuilder result = new StringBuilder();
		ap.selectXPath(txtNode);
		int txtIndex = -1;
		boolean isNull = true;
		while ((txtIndex = ap.evalXPath()) != -1) {
			result.append(vn.toString(txtIndex));
			if (isNull) {
				isNull = false;
			}
		}

		return isNull ? null : result.toString();
	}
}
