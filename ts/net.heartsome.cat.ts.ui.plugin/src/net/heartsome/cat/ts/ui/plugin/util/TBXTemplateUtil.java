package net.heartsome.cat.ts.ui.plugin.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.util.CommonFunctions;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * 该类与 net.heartsome.util.TbxTemplate 类似，由于 TbxTemplate 中的 XCS 模板路径是写在代码中的，
 * 不能直接调用，因此写了一个与 TbxTemplate 类似的类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class TBXTemplateUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TBXTemplateUtil.class);

	private Vector<Element> items;

	private String templateFileName;

	private String templatePath;

	private Document document;

	public static String teLevel = "termEntry"; //$NON-NLS-1$
	public static String lsLevel = "langSet"; //$NON-NLS-1$
	public static String termLevel = "term"; //$NON-NLS-1$

	public static String termNoteSpec = "termNoteSpec"; //$NON-NLS-1$
	public static String descripSpec = "descripSpec"; //$NON-NLS-1$
	public static String adminSpec = "adminSpec";

	public TBXTemplateUtil(String templateFile, String templatePath, String catalogueFile) throws Exception {
		this.templateFileName = templateFile;
		this.templatePath = templatePath;
		this.items = new Vector<Element>();
		loadTemplate(catalogueFile);
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	private void loadTemplate(String catalogueFile) throws SAXException, ParserConfigurationException, IOException {
		SAXBuilder builder = new SAXBuilder();
		if (!catalogueFile.equals("")) { //$NON-NLS-1$
			Catalogue cat = new Catalogue(catalogueFile);
			builder.setEntityResolver(cat);
			builder.setValidating(true);
		}
		File templateFile = new File(templatePath, templateFileName); //$NON-NLS-1$
		document = builder.build(templateFile.getAbsolutePath());
		Element tbxXCS = document.getRootElement();
		Element datCatSet = tbxXCS.getChild("datCatSet"); //$NON-NLS-1$
		if (datCatSet != null) {
			List<Element> specs = datCatSet.getChildren();
			Iterator<Element> specsIt = specs.iterator();
			while (specsIt.hasNext()) {
				Element spec = specsIt.next();
				Element contents = spec.getChild("contents"); //$NON-NLS-1$
				if (contents != null) {
					items.add(spec);
				}
			}
		}
	}

	public int getItemCount() {
		return items.size();
	}

	public String getItemLevels(int item) {
		Element spec = items.get(item);
		Element levels = spec.getChild("levels"); //$NON-NLS-1$
		if (levels != null) {
			return levels.getText();
		}
		return ""; //$NON-NLS-1$
	}

	public String getSpecName(int item) {
		Element spec = items.get(item);
		return spec.getName();
	}

	public String getItemDescription(int item) {
		Element spec = items.get(item);
		String itemDesc = spec.getAttributeValue("display"); //$NON-NLS-1$
		if (itemDesc.equals("")) { //$NON-NLS-1$
			return getItemName(item);
		}
		return itemDesc;
	}
	
	 public String getItemName(int item){
	        Element spec = items.get(item);
	        return spec.getAttributeValue("name");         //$NON-NLS-1$
	    }

	public static Vector<String> getTemplateFiles(String catalogueFile, String templatePath, boolean needArray) {
		Vector<String> result = new Vector<String>();
		File templateDir = new File(templatePath);
		String[] contents = templateDir.list();
		if (contents != null) {
			for (int i = 0; i < contents.length; i++) {
				File currentFile = new File(templateDir.getAbsolutePath(), contents[i]);
				if (currentFile.isFile()) {
					try {
						SAXBuilder builder = new SAXBuilder();
						if (!catalogueFile.equals("")) { //$NON-NLS-1$
							Catalogue cat = new Catalogue(catalogueFile);
							builder.setEntityResolver(cat);
						}
						Document doc = builder.build(currentFile.getAbsolutePath());
						Element tbxXCS = doc.getRootElement();
						Element datCatSet = tbxXCS.getChild("datCatSet"); //$NON-NLS-1$
						if (datCatSet != null) {
							result.add(currentFile.getName());
						}
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			}
		}

		return result;
	}

	public static Vector<String> getTemplateFiles(String catalogueFile, boolean needArray) {
		return getTemplateFiles(catalogueFile, "templates", needArray);
	}

	public static String[] getTemplateFiles(String catalogueFile, String templatePath) {
		return CommonFunctions.Vector2StringArray(getTemplateFiles(catalogueFile, templatePath, false));
	}
}
