/**
 * TaggedRtf2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.taggedrtf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.taggedrtf.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Attribute;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class TaggedRtf2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class TaggedRtf2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-trtf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("taggedrtf.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "Tagged RTF to XLIFF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public TaggedRtf2Xliff() {
		dependantConverter = Activator.getRtfConverter(Converter.DIRECTION_POSITIVE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public TaggedRtf2Xliff(Converter converter) {
		dependantConverter = converter;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		TaggedRtf2XliffImpl converter = new TaggedRtf2XliffImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getName()
	 * @return
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getType()
	 * @return
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 * @return
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class TaggedRtf2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class TaggedRtf2XliffImpl {

		/** The sdoc. */
		private Document sdoc;

		/** The tdoc. */
		private Document tdoc;

		/** The sroot. */
		private Element sroot;

		/** The troot. */
		private Element troot;

		/** The tbody. */
		private Element tbody;

		/** The match. */
		private String match;

		/** The source start. */
		private String sourceStart;

		/** The source end. */
		private String sourceEnd;

		/** The target start. */
		private String targetStart;

		/** The target end. */
		private String targetEnd;

		/** The lock100. */
		private boolean lock100;

		/** The is suite. */
		private boolean isSuite;

		/** The qt tool id. */
		private String qtToolID;

		/**
		 * Run.
		 * @param params
		 *            the params
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			params.put(Converter.ATTR_SEG_BY_ELEMENT, Converter.TRUE);
			params.put(Converter.ATTR_IS_TAGGEDRTF, Converter.TRUE);
			params.put(Converter.ATTR_FORMAT, TYPE_VALUE);

			isSuite = false;
			if (Converter.TRUE.equals(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}
			qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			lock100 = Boolean.parseBoolean(params.get(Converter.ATTR_LOCK_100));
			Map<String, String> result = null;
			try {
				// 把转换过程分为 10 部分：用 RTF 转换器进行转换的部分占 8，此转换器处理剩余的转换部分占 2
				monitor.beginTask("", 10);
				result = dependantConverter.convert(params, Progress.getSubMonitor(monitor, 8));
				if (result.get(Converter.ATTR_XLIFF_FILE) == null) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("taggedrtf.TaggedRtf2Xliff.msg1"));
				}
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("taggedrtf.cancel"));
				}

				File source = new File(params.get(Converter.ATTR_XLIFF_FILE));
				File original = new File(params.get(Converter.ATTR_SKELETON_FILE));
				File skeleton = new File(original.getAbsolutePath() + ".tg.skl"); //$NON-NLS-1$

				SAXBuilder builder = new SAXBuilder();
				sdoc = builder.build(source.getAbsolutePath());
				sroot = sdoc.getRootElement();
				tdoc = new Document(null, "xliff", sdoc.getPublicId(), sdoc.getSystemId()); //$NON-NLS-1$
				troot = tdoc.getRootElement();
				copyAttributes(sroot, troot);

				troot.setAttribute("xmlns:hs", Converter.HSNAMESPACE); //$NON-NLS-1$
				Attribute a = troot.getAttribute("xsi:schemaLocation"); //$NON-NLS-1$
				if (a == null) {
					troot.setAttribute("xsi:schemaLocation", Converter.HSSCHEMALOCATION); //$NON-NLS-1$
				} else {
					String attValue = a.getValue();
					if (attValue.indexOf(Converter.HSSCHEMALOCATION) == -1) {
						troot.setAttribute("xsi:schemaLocation", attValue + " " + Converter.HSSCHEMALOCATION); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				Element sfile = sroot.getChild("file"); //$NON-NLS-1$
				Element tfile = new Element("file", tdoc); //$NON-NLS-1$
				tfile.setAttribute("datatype", TYPE_VALUE); //$NON-NLS-1$ 
				tfile.setAttribute("original", params.get(Converter.ATTR_SOURCE_FILE)); //$NON-NLS-1$ 
				tfile.setAttribute("source-language", params.get(Converter.ATTR_SOURCE_LANGUAGE)); //$NON-NLS-1$ 
				String targetLang = params.get(Converter.ATTR_TARGET_LANGUAGE);
				if(!"".equals(targetLang)){
					tfile.setAttribute("target-language",targetLang); //$NON-NLS-1$ 
				}
				troot.addContent("\n"); //$NON-NLS-1$
				troot.addContent(tfile);

				Element header = new Element("header", tdoc); //$NON-NLS-1$
				Element skl = new Element("skl", tdoc); //$NON-NLS-1$
				Element extfile = new Element("external-file", tdoc); //$NON-NLS-1$
				extfile.setAttribute("href", TextUtil.cleanString(skeleton.getAbsolutePath())); //$NON-NLS-1$
				if (isSuite) {
					extfile
							.setAttribute(
									"crc", "" + CRC16.crc16(TextUtil.cleanString(skeleton.getAbsolutePath()).getBytes("UTF-8"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				skl.addContent("\n"); //$NON-NLS-1$
				skl.addContent(extfile);
				skl.addContent("\n"); //$NON-NLS-1$
				header.addContent("\n"); //$NON-NLS-1$
				header.addContent(skl);

				Element tool = new Element("tool", tdoc); //$NON-NLS-1$
				tool.setAttribute("tool-id", qtToolID); //$NON-NLS-1$ 
				tool.setAttribute("tool-name", "HSStudio"); //$NON-NLS-1$ //$NON-NLS-2$
				header.addContent("\n"); //$NON-NLS-1$
				header.addContent(tool);
				tfile.addContent("\n"); //$NON-NLS-1$
				tfile.addContent(header);

				List<Element> properties = sfile.getChild("header").getChildren("hs:prop-group"); //$NON-NLS-1$ //$NON-NLS-2$
				Iterator<Element> it = properties.iterator();
				while (it.hasNext()) {
					Element group = it.next();
					Element pgroup = new Element(group.getName(), tdoc);
					pgroup.clone(group, tdoc);
					header.addContent("\n"); //$NON-NLS-1$
					header.addContent(pgroup);
				}
				header.addContent("\n"); //$NON-NLS-1$

				tbody = new Element("body", tdoc); //$NON-NLS-1$
				tfile.addContent("\n"); //$NON-NLS-1$
				tfile.addContent(tbody);
				tfile.addContent("\n"); //$NON-NLS-1$
				troot.addContent("\n"); //$NON-NLS-1$

				// 对所占比例为 2 的转换任务再进一步细分为 10 个部分：处理翻译单元占 6，写骨架文件占 2，写源文件占 2
				IProgressMonitor subMonitor1 = Progress.getSubMonitor(monitor, 2);
				subMonitor1.beginTask("", 10);
				subMonitor1.subTask(Messages.getString("taggedrtf.TaggedRtf2Xliff.task2"));

				List<Element> units = sfile.getChild("body").getChildren("trans-unit"); //$NON-NLS-1$ //$NON-NLS-2$
				int size = units.size();
				IProgressMonitor subMonitor2 = Progress.getSubMonitor(subMonitor1, 6);
				subMonitor2.beginTask(Messages.getString("taggedrtf.TaggedRtf2Xliff.task3"), size);
				subMonitor2.subTask("");
				for (int i = 0; i < size; i++) {
					// 是否取消操作
					if (subMonitor2.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("taggedrtf.cancel"));
					}
					parseUnit(units.get(i));
					subMonitor2.worked(1);
				}
				subMonitor2.done();

				tbody.addContent("\n"); //$NON-NLS-1$

				subMonitor1.subTask(Messages.getString("taggedrtf.TaggedRtf2Xliff.task4"));
				XMLOutputter outputter = new XMLOutputter();
				FileOutputStream output = new FileOutputStream(skeleton);
				outputter.preserveSpace(true);
				outputter.output(sdoc, output);
				output.close();
				output = null;

				subMonitor1.worked(2);
				// 是否取消操作
				if (subMonitor1.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("taggedrtf.cancel"));
				}
				subMonitor1.subTask(Messages.getString("taggedrtf.TaggedRtf2Xliff.task5"));
				output = new FileOutputStream(source.getAbsolutePath());
				outputter.output(tdoc, output);
				output.close();
				output = null;
				subMonitor1.worked(2);
				subMonitor1.done();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("taggedrtf.TaggedRtf2Xliff.msg1"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Parses the unit.
		 * @param unit
		 *            the unit
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseUnit(Element unit) throws SAXException, IOException {
			Element src = unit.getChild("source"); //$NON-NLS-1$
			String text = src.toString();
			Vector<Element> tags = new Vector<Element>();
			match = ""; //$NON-NLS-1$
			if (find(text, "&lt;0", 0) != -1 && find(text, "0&gt;", 0) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				Element t = new Element("trans-unit", tdoc); //$NON-NLS-1$
				copyAttributes(unit, t);
				t.addContent("\n"); //$NON-NLS-1$
				Element source = new Element("source", tdoc); //$NON-NLS-1$
				copyAttributes(src, source);
				t.addContent(source);
				t.addContent("\n"); //$NON-NLS-1$
				Element target = new Element("target", tdoc); //$NON-NLS-1$
				t.addContent(target);
				t.addContent("\n"); //$NON-NLS-1$

				sourceStart = ""; //$NON-NLS-1$
				sourceEnd = ""; //$NON-NLS-1$
				targetStart = ""; //$NON-NLS-1$
				targetEnd = ""; //$NON-NLS-1$

				if (text.indexOf("0&gt;") == -1) { //$NON-NLS-1$
					src = fix(src, "0&gt;"); //$NON-NLS-1$
					text = src.toString();
				}
				if (text.indexOf("&lt;0") == -1) { //$NON-NLS-1$
					src = fix(src, "&lt;0}"); //$NON-NLS-1$
					text = src.toString();
				}

				List<Node> content = compact(src.getContent());
				Iterator<Node> i = content.iterator();

				// skip initial portion
				while (i.hasNext()) {
					Node n = i.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						int index = s.indexOf("0>"); //$NON-NLS-1$
						if (index != -1) {
							if (index != 0 && s.charAt(index - 1) != '{') {
								sourceStart = sourceStart + TextUtil.cleanString(s);
								continue;
							}
							int end = s.indexOf("<}"); //$NON-NLS-1$
							if (end == -1) {
								sourceStart = sourceStart + TextUtil.cleanString(s.substring(0, index + 2));
								source.addContent(s.substring(index + 2));
							} else {
								sourceStart = sourceStart + TextUtil.cleanString(s.substring(0, index + 2));
								source.addContent(s.substring(index + 2, end));
								s.substring(end + 2);
								index = s.indexOf("{>"); //$NON-NLS-1$
								end = s.indexOf("<0}"); //$NON-NLS-1$
								if (index != -1) {
									if (end == -1) {
										target.addContent(s.substring(index + 2));
									} else {
										target.addContent(s.substring(index + 2, end));
										targetEnd = targetEnd + TextUtil.cleanString(s.substring(end));
									}
								}
							}
							break;
						}
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						sourceStart = sourceStart + e.toString();
					}
				}

				// copy text to source
				while (i.hasNext()) {
					Node n = i.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						int end = s.indexOf("<}"); //$NON-NLS-1$
						if (end != -1) {
							source.addContent(s.substring(0, end));
							// skip the match quality value and add the
							// remainder to
							// the target
							s = s.substring(end);
							int index = s.indexOf("{>"); //$NON-NLS-1$
							if (s.startsWith("<}") && s.endsWith("{>")) { //$NON-NLS-1$ //$NON-NLS-2$
								match = s;
							} else if (index != -1) {
								match = s.substring(0, index + 2);
							}

							if ("<}100{>".equals(match) && lock100) { //$NON-NLS-1$
								t.setAttribute("translate", "no"); //$NON-NLS-1$ //$NON-NLS-2$
							}

							if (index != -1) {
								target.addContent(s.substring(index + 2));
							}
							break;
						}
						source.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element c = new Element(e.getName(), tdoc);
						c.clone(e, tdoc);
						source.addContent(c);
						tags.add(c);
					}
				}
				if (target.getContent().size() == 0) {
					// target empty, skip tags and match quality
					while (i.hasNext()) {
						Node n = i.next();
						if (n.getNodeType() == Node.TEXT_NODE) {
							String s = n.getNodeValue();
							int start = s.indexOf("<}"); //$NON-NLS-1$
							int end = s.indexOf("{>"); //$NON-NLS-1$
							if (start != -1 && end != -1 && end > start) {
								match = s.substring(start, end + 2);
							}
							if (end != -1) {
								target.addContent(s.substring(end + 2));
								break;
							}
						} else if (n.getNodeType() == Node.ELEMENT_NODE) {
							Element e = new Element(n);
							targetStart = targetStart + e.toString();
						}
					}
				}
				// copy text to target
				while (i.hasNext()) {
					Node n = i.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						int end = s.indexOf("<0}"); //$NON-NLS-1$
						if (end != -1) {
							target.addContent(s.substring(0, end));
							targetEnd = targetEnd + TextUtil.cleanString(s.substring(end));
						} else {
							target.addContent(s);
						}
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						for (int h = 0; h < tags.size(); h++) {
							Element k = tags.get(h);
							if (e.getText().equals(k.getText())) {
								e.clone(k, sdoc);
							}
						}
						Element c = new Element(e.getName(), tdoc);
						c.clone(e, tdoc);
						target.addContent(c);
					}
				}
				trimTags(source);
				trimTags(target);
				int srcTags = countTags(source);
				int tgtTags = countTags(target);
				if (srcTags == 0 && tgtTags != 0) {
					removeTags(target);
				}
				if (srcTags == 1 && tgtTags == 1) {
					equalizeTags(source, target);
				}

				if (!match.equals("")) { //$NON-NLS-1$
					Element note = new Element("note", tdoc); //$NON-NLS-1$
					note.setText(match);
					t.addContent("\n"); //$NON-NLS-1$
					t.addContent(note);
					t.addContent("\n"); //$NON-NLS-1$
					if (match.equals("<}100{>")) { //$NON-NLS-1$
						Element altTrans = new Element("alt-trans", tdoc); //$NON-NLS-1$
						altTrans.setAttribute("match-quality", "100"); //$NON-NLS-1$ //$NON-NLS-2$
						altTrans.setAttribute("tool", "Trados or Similar"); //$NON-NLS-1$ //$NON-NLS-2$
						altTrans.setAttribute("xml:space", "default"); //$NON-NLS-1$ //$NON-NLS-2$
						altTrans.addContent("\n"); //$NON-NLS-1$
						Element s = new Element("source", tdoc); //$NON-NLS-1$
						s.clone(source, tdoc);
						altTrans.addContent(s);
						altTrans.addContent("\n"); //$NON-NLS-1$
						Element tg = new Element("target", tdoc); //$NON-NLS-1$
						tg.clone(target, tdoc);
						altTrans.addContent(tg);
						t.addContent(altTrans);
						t.addContent("\n"); //$NON-NLS-1$
					}
				}

				if (!sourceStart.equals("") || !sourceEnd.equals("") || //$NON-NLS-1$ //$NON-NLS-2$
						!targetStart.equals("") || !targetEnd.equals("")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					Element group = new Element("hs:prop-group", tdoc); //$NON-NLS-1$
					group.setAttribute("name", "tags"); //$NON-NLS-1$ //$NON-NLS-2$
					t.addContent(group);
					Element prop1 = new Element("hs:prop", tdoc); //$NON-NLS-1$
					prop1.setAttribute("prop-type", "sourceStart"); //$NON-NLS-1$ //$NON-NLS-2$
					prop1.addContent(sourceStart);
					group.addContent("\n"); //$NON-NLS-1$
					group.addContent(prop1);
					Element prop2 = new Element("hs:prop", tdoc); //$NON-NLS-1$
					prop2.setAttribute("prop-type", "sourceEnd"); //$NON-NLS-1$ //$NON-NLS-2$
					prop2.addContent(sourceEnd);
					group.addContent("\n"); //$NON-NLS-1$
					group.addContent(prop2);
					Element prop3 = new Element("hs:prop", tdoc); //$NON-NLS-1$
					prop3.setAttribute("prop-type", "targetStart"); //$NON-NLS-1$ //$NON-NLS-2$
					prop3.addContent(targetStart);
					group.addContent("\n"); //$NON-NLS-1$
					group.addContent(prop3);
					Element prop4 = new Element("hs:prop", tdoc); //$NON-NLS-1$
					prop4.setAttribute("prop-type", "targetEnd"); //$NON-NLS-1$ //$NON-NLS-2$
					prop4.addContent(targetEnd);
					group.addContent("\n"); //$NON-NLS-1$
					group.addContent(prop4);
					group.addContent("\n"); //$NON-NLS-1$
				}

				tbody.addContent("\n"); //$NON-NLS-1$
				tbody.addContent(t);
				tbody.addContent("\n"); //$NON-NLS-1$
				enumerateTags(source);
				enumerateTags(target);
			} else {
				// this unit doesn't have a translation from Trados
				unit.setAttribute("datatype", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$
				Element c = new Element(unit.getName(), tdoc);
				c.clone(unit, tdoc);
				tbody.addContent("\n"); //$NON-NLS-1$
				tbody.addContent(c);
			}

		}

		/**
		 * Compact.
		 * @param list
		 *            the list
		 * @return the list< node>
		 */
		private List<Node> compact(List<Node> list) {
			List<Node> result = new ArrayList<Node>();
			for (int i = 0; i < list.size(); i++) {
				Node n = list.get(i);
				if (n.getNodeType() == Node.TEXT_NODE) {
					while (i + 1 < list.size() && (list.get(i + 1)).getNodeType() == Node.TEXT_NODE) {
						n.setNodeValue(n.getNodeValue() + (list.get(i + 1)).getNodeValue());
						i++;
					}
				}
				result.add(n);
			}
			return result;
		}

		/**
		 * Equalize tags.
		 * @param source
		 *            the source
		 * @param target
		 *            the target
		 */
		private void equalizeTags(Element source, Element target) {
			if (source.getChildren().get(0).equals(target.getChildren().get(0))) {
				return;
			}
			target.getChildren().get(0).clone(source.getChildren().get(0), tdoc);
		}

		/**
		 * Enumerate tags.
		 * @param e
		 *            the e
		 */
		private void enumerateTags(Element e) {
			List<Element> list = e.getChildren("ph"); //$NON-NLS-1$
			for (int i = 0; i < list.size(); i++) {
				Element ph = list.get(i);
				ph.setAttribute("id", "" + (i + 1)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		/**
		 * Removes the tags.
		 * @param target
		 *            the target
		 */
		private void removeTags(Element target) {
			List<Element> children = target.getChildren();
			Iterator<Element> it = children.iterator();
			while (it.hasNext()) {
				target.removeChild(it.next());
			}
		}

		/**
		 * Count tags.
		 * @param e
		 *            the e
		 * @return the int
		 */
		private int countTags(Element e) {
			return e.getChildren().size();
		}

		/**
		 * Fix.
		 * @param src
		 *            the src
		 * @param token
		 *            the token
		 * @return the element
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private Element fix(Element src, String token) throws SAXException, IOException {
			String text = src.toString();
			int index = find(text, token, 0);
			if (index == -1) {
				return src;
			}
			String first = text.substring(0, index);
			String last = text.substring(index);
			while (last.indexOf(token) == -1) {
				index = last.indexOf("<ph"); //$NON-NLS-1$
				int end = last.indexOf("</ph>"); //$NON-NLS-1$
				last = last.substring(0, index) + last.substring(end + 5);
			}
			ByteArrayInputStream stream = new ByteArrayInputStream((first + last).getBytes("UTF-8")); //$NON-NLS-1$
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(stream);
			return d.getRootElement();
		}

		/**
		 * Trim tags.
		 * @param e
		 *            the e
		 */
		private void trimTags(Element e) {
			List<Node> original = e.getContent();
			List<Node> trimmed = new ArrayList<Node>();
			// skip initial tags
			Iterator<Node> i = original.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				if (n.getNodeType() != Node.ELEMENT_NODE && !n.getNodeValue().equals("")) { //$NON-NLS-1$
					trimmed.add(n);
					break;
				}
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element h = new Element(n);
					if (e.getName().equals("source")) { //$NON-NLS-1$
						sourceStart = sourceStart + h.toString();
					} else {
						targetStart = targetStart + h.toString();
					}
				} else {
					if (e.getName().equals("source")) { //$NON-NLS-1$
						sourceStart = sourceStart + TextUtil.cleanString(n.getNodeValue());
					} else {
						targetStart = targetStart + TextUtil.cleanString(n.getNodeValue());
					}
				}
			}
			while (i.hasNext()) {
				Node n = i.next();
				if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().equals("")) { //$NON-NLS-1$
					continue;
				}
				trimmed.add(n);
			}
			while (trimmed.size() > 0) {
				Node n = trimmed.get(trimmed.size() - 1);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element h = new Element(n);
					if (e.getName().equals("source")) { //$NON-NLS-1$
						sourceEnd = h.toString() + sourceEnd;
					} else {
						targetEnd = h.toString() + targetEnd;
					}
					trimmed.remove(n);
				}
				if (n.getNodeType() == Node.TEXT_NODE) {
					int idx = n.getNodeValue().indexOf("<0}"); //$NON-NLS-1$
					if (idx == -1) {
						break;
					}
					if (e.getName().equals("source")) { //$NON-NLS-1$
						sourceEnd = n.getNodeValue().substring(idx) + sourceEnd;
					} else {
						targetEnd = n.getNodeValue().substring(idx) + targetEnd;
					}
					n.setNodeValue(n.getNodeValue().substring(0, idx));
				}
			}

			e.setContent(trimmed);
		}

		/**
		 * Copy attributes.
		 * @param src
		 *            the src
		 * @param tgt
		 *            the tgt
		 */
		private void copyAttributes(Element src, Element tgt) {
			List<Attribute> attributes = src.getAttributes();
			for (int i = 0; i < attributes.size(); i++) {
				Attribute a = attributes.get(i);
				tgt.setAttribute(a.getName(), a.getValue());
			}
			attributes = null;
		}
	}

	/**
	 * Find.
	 * @param text
	 *            the text
	 * @param token
	 *            the token
	 * @param from
	 *            the from
	 * @return the int
	 */
	public static int find(String text, String token, int from) {
		int length = text.length();
		for (int i = from; i < length; i++) {
			String remaining = text.substring(i);
			if (remaining.startsWith("<ph")) { //$NON-NLS-1$
				int ends = remaining.indexOf("</ph>"); //$NON-NLS-1$
				if (ends != -1) {
					remaining = remaining.substring(ends + 5);
					i = i + ends + 5;
				}
			}
			String trimmed = removePh(remaining);
			if (trimmed.startsWith(token)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Removes the ph.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String removePh(String string) {
		String result = ""; //$NON-NLS-1$
		int starts = string.indexOf("<ph"); //$NON-NLS-1$
		while (starts != -1) {
			result = result + string.substring(0, starts);
			string = string.substring(starts);
			int ends = string.indexOf("</ph>"); //$NON-NLS-1$
			if (ends != -1) {
				string = string.substring(ends + 5);
			}
			starts = string.indexOf("<ph"); //$NON-NLS-1$
		}
		return result + string;
	}
}