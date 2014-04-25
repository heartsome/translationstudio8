/**
 * AutoConfiguration.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.xml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class AutoConfiguration.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class AutoConfiguration {

	/** The segment. */
	private Hashtable<String, String> segment;

	/**
	 * Run.
	 * @param input
	 *            the input
	 * @param out
	 *            the out
	 * @param catalogue
	 *            the catalogue
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public void run(String input, String out, String catalogue) throws SAXException, IOException,
			ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalogue(catalogue));
		Document d = builder.build(input);
		Element r = d.getRootElement();
		segment = new Hashtable<String, String>();
		recurse(r);

		Document doc = new Document(null, "ini-file", "-//HEARTSOME//Converters 2.0.0//EN", "configuration.dtd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Element root = doc.getRootElement();
		Enumeration<String> keys = segment.keys();
		while (keys.hasMoreElements()) {
			Element e = new Element("tag", doc); //$NON-NLS-1$
			String key = keys.nextElement();
			e.setText(key);
			e.setAttribute("hard-break", "segment"); //$NON-NLS-1$ //$NON-NLS-2$
			root.addContent(e);
			root.addContent("\n"); //$NON-NLS-1$
		}

		XMLOutputter outputter = new XMLOutputter();
		FileOutputStream output = new FileOutputStream(out);
		outputter.output(doc, output);
		output.close();
	}

	/**
	 * Recurse.
	 * @param r
	 *            the r
	 */
	private void recurse(Element r) {
		String text = ""; //$NON-NLS-1$
		List<Node> content = r.getContent();
		Iterator<Node> i = content.iterator();
		while (i.hasNext()) {
			Node n = i.next();
			if (n.getNodeType() == Node.TEXT_NODE) {
				text = text + n.getNodeValue().trim();
			}
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = new Element(n);
				recurse(e);
			}
		}
		if (!text.equals("") && !segment.contains(r.getName())) { //$NON-NLS-1$
			segment.put(r.getName(), r.getName());
		}
	}
}
