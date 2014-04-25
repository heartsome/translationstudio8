/**
 * Ttx2Xliff.java
 *
 * Version information :
 *
 * Date:Jun 16, 2012
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ttx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.ttx.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * trados 2007 的 ttx(tradostag Xliff) 文件转换器。
 * @author robert 2012-07-16
 */
public class Ttx2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-ttx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("ttx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "TTX to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Ttx2XliffImpl converter = new Ttx2XliffImpl();
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
	 * trados 2007 ttx 文件的转换实现类
	 * @author robert
	 * @version
	 * @since JDK1.6
	 */
	class Ttx2XliffImpl {

		/** 要转换的源文件 */
		private String inputFile;

		/** 转换后的 r8 hsxliff 文件 */
		private String xliffFile;

		/** 转换后的 r8 hsxliff 骨架文件 */
		private String skeletonFile;

		/** 用户所选择的源语言 */
		private String userSourceLang;

		/** 用户所选择的目标语言 */
		private String targetLang;

		/** 源文件 ttx 的源语言，解析文件时，要通过它来确定文件的源文。 */
		private String detectedSourceLang;

		/** 源文件 ttx 的目标语言，解析文件时，要通过它来确定文件的译文。 */
		private String detectedTargetLang;

		/** 选择的编码 */
		private String srcEncoding;

		/** 目标 hsxliff 文件的流输出类 */
		private FileOutputStream output;

		/** trans-unit 节点的id */
		private int segId;

		/** 标记的id */
		private int tagId;

		/** The lock xtrans. */
		private boolean lockXtrans;

		/** The lock100. */
		private boolean lock100;

		/** The is suite. */
		private boolean isSuite;

		/** The qt tool id. */
		private String qtToolID;
		/** 骨架文件的导航实例。 */
		private VTDNav sklVN;
		/** 骨架文件的 xml 修改实例。 */
		private XMLModifier sklXM;
		/** cf标记里是否要带走df的属性 */
		/** 因源文缺失 结束标记而手动添加的 <cf> 的个数 */
		private int addedEndTagNum = 0;

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
			// 把转换任务分为 5 个部分：复制文件，解析文件，处理内容列表，写骨架文件。
			monitor.beginTask(Messages.getString("ttx.Ttx2Xliff.task1"), 5);
			Map<String, String> result = new HashMap<String, String>();
			segId = 0;

			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			targetLang = params.get(Converter.ATTR_TARGET_LANGUAGE);
			userSourceLang = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			// 下面三个是整理分段等逻辑时需要的参数。
			// String elementSegmentation = params.get(Converter.ATTR_SEG_BY_ELEMENT);
			// String initSegmenter = params.get(Converter.ATTR_SRX);
			// String catalogue = params.get(Converter.ATTR_CATALOGUE);

			isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}

			qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			// 默认锁定 xu 句段
			lockXtrans = true;
			// if (Converter.TRUE.equals(params.get(Converter.ATTR_LOCK_XTRANS))) {
			// lockXtrans = true;
			// }

			lock100 = false;
			if (Converter.TRUE.equals(params.get(Converter.ATTR_LOCK_100))) {
				lock100 = true;
			}

			try {
				// 将源文件复制进骨架文件，之后就从骨架文件中提取翻译信息。
				if (!"utf-8".equalsIgnoreCase(srcEncoding)) {
					// vtd在处理 UTF-16LE 时会出现 token 定位异常，故转换成 UTF-8 的格式
					copyFile(inputFile, skeletonFile, srcEncoding, "UTF-8");
				} else {
					copyFile(inputFile, skeletonFile);
				}
				// copyFile(skeletonFile, "/home/robert/Desktop/testFile1.txt");
				parseSkeletonFile();

				boolean isAlert = false;
				// 检测是否含有未翻译的文本段，如果含有则拒绝转换，原因是 R8中无法转换未预翻译的文本段
				check(result);
				monitor.worked(1);
				getSrcAndTgtLang();

				// copyFile(skeletonFile, "/home/robert/Desktop/a.txt");

				output = new FileOutputStream(xliffFile);
				
				parseSkeletonFile();
				deleteDF();

				writeHeader();
				analyzeNodes();

				writeString("</body>\n");
				writeString("</file>\n");
				writeString("</xliff>");

				sklXM.output(skeletonFile);
				addPHIdToXliff();
				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
				if (isAlert) {
					throw new MissTranslationException();
				}
			} catch (OperationCanceledException e) {
				e.printStackTrace();
				throw e;
			} catch (MissTranslationException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, MessageFormat.format(
						Messages.getString("ttx.Ttx2Xliff.warn"), new Object[] { new File(inputFile).getName() }));
			} catch (Exception e) {
				e.printStackTrace();
				ConverterUtils
						.throwConverterException(Activator.PLUGIN_ID, Messages.getString("ttx.Ttx2Xliff.msg1"), e);
			} finally {
				monitor.done();
				try {
					if (output != null) {
						output.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

			return result;
		}
		
		/**
		 * 删除　df 标记，因为　df 是保存　ttx 可见即可得的标记信息，可以删除。此功能主要是针对　df 标记太多从而影响很多 bug.
		 */
		private void deleteDF() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			int dfSize = 0;
			ap.selectXPath("count(/TRADOStag/Body/Raw/descendant::df[not(descendant::df)])");
			dfSize = (int)ap.evalXPathToNumber();

			String dfContent = null;
			while(dfSize > 0){
				ap.selectXPath("/TRADOStag/Body/Raw/descendant::df[not(descendant::df)]");
				while(ap.evalXPath() != -1){
					dfContent = vu.getElementContent(); 
					sklXM.remove();
					if (dfContent != null) {
						sklXM.insertAfterElement(dfContent.getBytes("UTF-8"));
					}
				}
				sklXM.output(skeletonFile);
				parseSkeletonFile();
				ap.bind(sklVN);
				vu.bind(sklVN);
				ap.selectXPath("count(/TRADOStag/Body/Raw/descendant::df[not(descendant::df)])");
				dfSize = (int)ap.evalXPathToNumber();
			}
//			copyFile(skeletonFile, "C:\\Users\\Ilen\\Desktop\\testTTX.txt");
		}
		
		/**
		 * 检查当前　ttx 是否是预翻译　--李庆东
		 * @param result
		 * @throws Exception
		 */
		private void check(Map<String, String> result) throws Exception {
			sklVN.push();
			AutoPilot ap = new AutoPilot(sklVN);
			// 先查找是否有　tuv 节点的存在
			ap.selectXPath("/TRADOStag/Body/Raw//Tu/Tuv");
			if (ap.evalXPath() == -1) {
				throw new MissTranslationException();
			}
			// 目前已知的两处可能含有未翻译的文本
			ap.selectXPath("/TRADOStag/Body/Raw/text()|/TRADOStag/Body/Raw/df/text()");
			while (ap.evalXPath() != -1) {
				String text = sklVN.toString(sklVN.getCurrentIndex()).replaceAll("\\s", "").trim();
				if (text.length() == 0) {
					return;
				}
				//忽略字符请添加在此处
				if (!(text.matches("[0-9\\.:\\-,–%#$\\s/]*") || text.matches("[a-zA-Z]*://.*") || text.matches(".*@.*"))) {
					result.put("ttx2xlfAlert39238409230481092830", MessageFormat.format(
							Messages.getString("ttx.Ttx2Xliff.warn"), new Object[] { new File(inputFile).getName() }));
				}
			}
		}

		/**
		 * Load files.
		 * @throws Exception
		 *             the exception
		 */
		private void parseSkeletonFile() throws Exception {
			String errorInfo = "";
			VTDGen vg = new VTDGen();
			if (vg.parseFile(skeletonFile, true)) {
				sklVN = vg.getNav();
				sklXM = new XMLModifier(sklVN);
			} else {
				errorInfo = MessageFormat.format("无法解析源文件 {0} 。", new Object[] { new File(inputFile).getName() });
				throw new Exception(errorInfo);
			}
		}

		/**
		 * 获取源文件的源语言和目标语言
		 */
		private void getSrcAndTgtLang() throws Exception {
			String srcLang = "";
			String tgtLang = "";
			AutoPilot ap = new AutoPilot(sklVN);
			String xpath = "/TRADOStag/FrontMatter/UserSettings";
			ap.selectXPath(xpath);
			int attrIdx = -1;
			if (ap.evalXPath() != -1) {
				if ((attrIdx = sklVN.getAttrVal("SourceLanguage")) != -1) {
					srcLang = sklVN.toRawString(attrIdx);
				}
				if ((attrIdx = sklVN.getAttrVal("TargetLanguage")) != -1) {
					tgtLang = sklVN.toRawString(attrIdx);
				}
			}
			detectedSourceLang = srcLang;
			// 如果 ttx 的目标文件的目标语言为空，那设成转换的目标语言
			detectedTargetLang = "".equals(tgtLang) ? targetLang : tgtLang;
			// 如果当前转换的目标语言也为空，那设成标识 noLang,之后转换完了再在骨架文件中进行替换
			detectedTargetLang = "".equals(detectedTargetLang) ? "noLang" : detectedTargetLang;

		}

		/**
		 * 写下 R8 hsxliff 文件的头信息
		 */
		private void writeHeader() throws IOException {

			writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\""
					+ Converter.HSNAMESPACE + "\" "
					+ "xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd "
					+ Converter.HSSCHEMALOCATION + "\">\n");
			writeString("<file original=\"" + TextUtil.cleanString(inputFile) + "\" source-language=\""
					+ userSourceLang);
			if (!"".equals(detectedTargetLang)) {
				writeString("\" target-language=\"" + detectedTargetLang);
			}
			writeString("\" datatype=\"" + TYPE_VALUE + "\">\n");
			writeString("<header>\n");
			writeString("   <skl>\n");
			String crc = "";
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\"";
			}
			writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n");
			writeString("   </skl>\n");
			writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n");
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" + srcEncoding
					+ "</hs:prop></hs:prop-group>\n");
			writeString("</header>\n");
			writeString("<body>\n");
		}

		/**
		 * 分析骨架文件的每一个节点，主要是针对tu节点
		 * @throws Exception
		 */
		private void analyzeNodes() throws Exception {
			tagId = 0;
			int attrIdx = -1;
			// 保存的是要翻译的源文文本与标记
			Map<Integer, TextBean> srcTextMap = new TreeMap<Integer, TextBean>();
			// 保存的是要翻译的译文文本与标记
			Map<Integer, TextBean> tgtTextMap = new TreeMap<Integer, TextBean>();
			AutoPilot ap = new AutoPilot(sklVN);
			AutoPilot childAP = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			String xpath = "/TRADOStag/Body/Raw/descendant::Tu";
			String srcXpath = "./Tuv[@Lang=\"" + detectedSourceLang + "\"]";
			String tgtXapth = "./Tuv[@Lang=\"" + detectedTargetLang + "\" and (text()!='' or *)]";
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				// 第一步，处理源节点
				sklVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					srcTextMap.clear();
					if (vu.getChildElementsCount() > 0) {
						analysisChildOfSrcTuv(srcTextMap, sklVN, vu);
					} else if ((attrIdx = sklVN.getText()) != -1) {
						if (!"".equals(sklVN.toRawString(attrIdx))) {
							sklXM.updateToken(attrIdx, "%%%" + tagId + "%%%");
							srcTextMap.put(attrIdx, new TextBean(tagId, sklVN.toRawString(attrIdx), true));
							tagId++;
						}
					}
				}
				sklVN.pop();
				// <Tuv Lang="EN-US">See the enclosure <ut DisplayText="xr">&lt;:xr &quot;“Specifications” on
				// page&lt;:hs&gt;20&quot; 2&gt;</ut>.</Tuv>

				// 第二步，验证是否需要进行转换成trans-unit节点
				boolean neenConvert = false;
				if (srcTextMap.size() > 0) {
					if (srcTextMap.size() == 1) {
						for (Entry<Integer, TextBean> entry : srcTextMap.entrySet()) {
							if (entry.getValue().getTagId() != -1) {
								// tag若等于 -1 ，则是标记，标记没有必须生成trans-unit节点
								neenConvert = true;
							}
						}
					} else {
						for (Entry<Integer, TextBean> entry : srcTextMap.entrySet()) {
							if (entry.getValue().getTagId() != -1) {
								// tag为1，则全是标记，没有必要进行生成tu节点
								neenConvert = true;
							}
						}
					}
					// 如果不用转换，直接跳到下一循环处
					if (!neenConvert) {
						continue;
					}
				}

				// 第三步，处理目标节点
				sklVN.push();
				childAP.selectXPath(tgtXapth);
				tgtTextMap.clear();
				if (childAP.evalXPath() != -1) {
					if (vu.getChildElementsCount() > 0) {
						analysisChildOfTgtTuv(tgtTextMap, sklVN, vu);
					} else if ((attrIdx = sklVN.getText()) != -1) {
						if (!"".equals(sklVN.toRawString(attrIdx))) {
							tgtTextMap.put(attrIdx, new TextBean(-1, sklVN.toRawString(attrIdx), true));
						}
					}
					// 删除译文节点的内容
					String tuvHeader = vu.getElementHead();
					sklXM.remove();
					sklXM.insertAfterElement(tuvHeader + "</Tuv>");
				}
				sklVN.pop();

				// 第四步，处理目标文本集合的内容。
				addedEndTagNum = 0;
				Map<Integer, String> tagMap = new LinkedHashMap<Integer, String>(); // 保存标记与标记的占位符
				String srcText = analysisSrcTextMap(srcTextMap, tagMap);
				String tgtText = analysisTgtTextMap(tgtTextMap, tagMap);

				// 第五步，开始生成trans-unit节点
				sklXM.insertAttribute(" id=\"" + segId + "\"");
				// 转存一些关于tu节点的属性，
				int match = 0;
				if ((attrIdx = sklVN.getAttrVal("MatchPercent")) != -1) {
					String matchPercent = sklVN.toRawString(attrIdx);
					if (!matchPercent.equals("")) {
						match = Integer.parseInt(matchPercent);
					}
				}
				String origin = "";
				if ((attrIdx = sklVN.getAttrVal("Origin")) != -1) {
					origin = sklVN.toRawString(attrIdx);
				}

				boolean isLock = false;
				boolean isXtranslate = false;
				if ("xtranslate".equalsIgnoreCase(origin) && lockXtrans) {
					isLock = true;
					isXtranslate = true;
				}

				if (match >= 100 && lock100) {
					isLock = true;
				}
				writeSegment(srcText, tgtText, match, isLock, isXtranslate);
			}
		}

		/**
		 * 分析tuv节点下面的所有节点，获取出要翻译的文本段
		 */
		private void analysisChildOfSrcTuv(Map<Integer, TextBean> textMap, VTDNav vn, VTDUtils vu) throws Exception {
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./node()|text()";
			ap.selectXPath(xpath);
			int index = -1;
			while (ap.evalXPath() != -1) {
				int curIdx = vn.getCurrentIndex();
				int tokenType = vn.getTokenType(curIdx);
				// 等于0表示为节点
				if (tokenType == 0) {
					// 如果这个节点还有子节点。那么遍历其子节点
					if (vu.getChildElementsCount() > 0) {
						analysisChildOfSrcTuv(textMap, vn, vu);
					} else {
						String nodeName = vu.getCurrentElementName();
						if ("df".equals(nodeName)) {
							// df节点下的数据，直接加载。
							if ((index = vn.getText()) != -1) {
								sklXM.updateToken(index, "%%%" + tagId + "%%%");
								textMap.put(index, new TextBean(tagId, vn.toRawString(index), true));
								tagId++;
							}
						} else if ("ut".equals(nodeName)) {
							// ut节点下保存的是cf标记信息，要将它进行转义
							if ((index = vn.getText()) != -1) {
								// 这种情况，为cf标记
								if (vn.getAttrVal("DisplayText") != -1
										&& "cf".equals(sklVN.toString(vn.getAttrVal("DisplayText")))) {
									textMap.put(index, new TextBean(-1, vn.toString(index), false));
								} else {
									// 处理除cf之外的其他标记，如symbol，这时一般当文本处理
									sklXM.remove();
									sklXM.insertBeforeElement("%%%" + tagId + "%%%");
									textMap.put(index, new TextBean(tagId, vn.toString(index), false));
									tagId++;
								}
							}
						}
					}
				} else if (tokenType == 5) { // 等于5表示为文本子节点
					index = vn.getCurrentIndex();
					sklXM.updateToken(index, "%%%" + tagId + "%%%");
					textMap.put(index, new TextBean(tagId, vn.toRawString(index), true));
					tagId++;
				}
			}
			vn.pop();
		}

		/**
		 * 分析译文tuv节点下面的所有节点，获取出要翻译的文本段，但不占位，只获取文本
		 */
		private void analysisChildOfTgtTuv(Map<Integer, TextBean> tgtTextMap, VTDNav vn, VTDUtils vu) throws Exception {
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./*|text()";
			ap.selectXPath(xpath);
			int index = -1;
			while (ap.evalXPath() != -1) {
				int curIdx = vn.getCurrentIndex();
				int tokenType = vn.getTokenType(curIdx);
				// 等于0表示为节点
				if (tokenType == 0) {
					// 如果这个节点还有子节点。那么遍历其子节点
					if (vu.getChildElementsCount() > 0) {
						analysisChildOfTgtTuv(tgtTextMap, vn, vu);
					} else {
						String nodeName = vu.getCurrentElementName();
						if ("df".equals(nodeName)) {
							// df节点下的数据，直接加载。
							if ((index = vn.getText()) != -1) {
								tgtTextMap.put(index, new TextBean(-1, vn.toRawString(index), true));
							}
						} else if ("ut".equals(nodeName)) {
							// ut节点下保存的是cf标记信息，要将它进行转义
							if ((index = vn.getText()) != -1) {
								if (vn.getAttrVal("Type") != -1) {
									tgtTextMap.put(index, new TextBean(-1, vn.toString(index), false));
								} else {
									// 若 ut 节点没有 Type 属性，一般为除cf之外的其他标记，如symbol，这时一般当文本处理
									tgtTextMap.put(index, new TextBean(-1, vn.toString(index), false));
								}
							}
						}
					}
				} else if (tokenType == 5) { // 等于5表示为文本子节点
					index = vn.getCurrentIndex();
					tgtTextMap.put(index, new TextBean(-1, vn.toRawString(index), true));
				}
			}
			vn.pop();
		}

		/**
		 * 分析处理源或目标文本，主要是处理cf标记，若第一行的标记为一个</cf>，那么删除， 若缺少<cf>的结束标记，自动补全，还有种情况就是<cf>标记在tu节点之外的情况。
		 */
		private String analysisSrcTextMap(Map<Integer, TextBean> srcMap, Map<Integer, String> tagMap) {
			StringBuffer testSB = new StringBuffer();
			for (Entry<Integer, TextBean> entry : srcMap.entrySet()) {
				testSB.append(entry.getValue().getText());
			}
			// if (testSB.toString().indexOf("See the enclosure") >= 0) {
			// System.out.println("处理开始了。。。。");
			//
			// }

			TextBean[] beans = srcMap.values().toArray(new TextBean[] {});
			StringBuffer sb = new StringBuffer();
			int i = 0;
			int start = 0; // 是否遇到起始标记
			int end = 0;
			for (Entry<Integer, TextBean> entry : srcMap.entrySet()) {
				TextBean bean = entry.getValue();
				String text = resetText(entry.getValue().getText());
				if (bean.getTagId() == -1) {
					// 标记的起始符号
					if (text.indexOf("<cf ") != -1) {
						if (i + 1 < beans.length) {
							// 处于标记对的，添加到结果集中，否则以ph的形式出现。这是针对开始或结束时，有多余的cf标记的情况
							tagMap.put(beans[i + 1].getTagId(), text);
							start++;
							sb.append(text);
						} else {
							sb.append("<ph type='cf'>" + cleanString(text) + "</ph>");
						}
					}
					// 标记的结束符号
					if (bean.getText().indexOf("</") != -1) {
						// 处于标记对的，添加到结果集中，否则以ph的形式出现。这是针对开始或结束时，有多余的cf标记的情况
						if (start > 0 && start > end) {
							sb.append(entry.getValue().getText());
							end++;
						} else {
							sb.append("<ph type='cf'>" + cleanString(entry.getValue().getText()) + "</ph>");
						}
					}
				} else {
					// 添加纯文本，纯文本里面也有标记，比如<symbol font="Symbol" character="F0E2"/>
					if (!bean.isText()) { // 如果是<symbol font="Symbol" character="F0E2"/>类似的。就变成 ph 标记
						text = "<ph>" + cleanString(text) + "</ph>";
					}
					sb.append(text);
				}
				i++;
			}

			// 最后时，如果缺少一个标签，自动补全。
			for (int j = 0; j < start - end; j++) {
				addedEndTagNum++;
				sb.append("</cf>");
			}

			String srcText = sb.toString();
			return replaceR8Tag(srcText, tagMap);
		}

		/**
		 * 分析处理目标文本，主要是处理cf标记，若第一行的标记为一个</cf>，那么以ph的形式出现， 若缺少<cf>的结束标记，自动补全，还有种情况就是<cf>标记在tu节点之外的情况。
		 * 备注：目标文本与源文不一样的是，目标文本里所有的文本都是没有占位符编号的。这个占位符编号要从译文的srcTextMap中获取，当然此时这些编号都存放在了tagMap中了。
		 */
		private String analysisTgtTextMap(Map<Integer, TextBean> tgtMap, Map<Integer, String> tagMap) {

			StringBuffer testSB = new StringBuffer();
			for (Entry<Integer, TextBean> entry : tgtMap.entrySet()) {
				testSB.append(entry.getValue().getText());
			}

			StringBuffer sb = new StringBuffer();
			int i = 0;
			int start = 0; // 是否遇到起始标记
			int end = 0;
			for (Entry<Integer, TextBean> entry : tgtMap.entrySet()) {
				String text = resetText(entry.getValue().getText());
				if (!entry.getValue().isText()) {
					// 标记的起始符号
					if (text.indexOf("<cf ") != -1) {
						if (i + 1 < tgtMap.size()) { // 这是开始标记，如果是最后一个值，那么，它以ph的形式出现
							start++;
							sb.append(text);
						} else {
							sb.append("<ph type='cf'>" + cleanString(text) + "</ph>");
						}
					} else if (text.indexOf("</cf>") != -1) { // 标记的结束符号
						// 处于标记对的，添加到结果集中，否则以ph的形式出现。这是针对开始或结束时，有多余的cf标记的情况
						if (start > 0 && start > end) {
							sb.append(text);
							end++;
						} else {
							sb.append("<ph type='cf'>" + cleanString(text) + "</ph>");
						}
					} else {
						// 添加纯文本，纯文本里面也有标记，比如<symbol font="Symbol" character="F0E2"/>
						text = "<ph>" + cleanString(text) + "</ph>";
						sb.append(text);
					}
				} else {
					sb.append(text);
				}
				i++;

			}

			// 最后时，如果缺少一个标签，自动补全。
			for (int j = 0; j < start - end; j++) {
				sb.append("</cf>");
			}

			String srcText = sb.toString();
			return replaceR8Tag(srcText, tagMap);
		}

		/**
		 * 将ttx的cf标记替换成R8的 g 标记
		 */
		private String replaceR8Tag(String text, Map<Integer, String> tagMap) {
			String replacedText = text;
			String tagStr = ""; // 标签字符串
			String replaceStr = ""; // 要把标记头替换的内容
			int index = replacedText.indexOf("<cf ");
			int endIndex = -1;
			int tagSegId = -1; // 标签所对应的占位符
			while (index != -1) {
				endIndex = replacedText.indexOf(">", index);
				tagStr = replacedText.substring(index, endIndex + 1);
				tagSegId = getTagSegmentId(tagStr, tagMap);
				if (tagSegId == -1) {
					replaceStr = "<g";
				} else {
					replaceStr = "<g id='" + tagSegId + "'";
				}
				replacedText = replacedText.replaceFirst("<cf", replaceStr);
				index = replacedText.indexOf("<cf ");
			}
			replacedText = replacedText.replaceAll("</cf>", "</g>");
			return replacedText;
		}

		/**
		 * 根据标记的头节点获取标记所对应的占位符
		 * @param tagStr
		 * @param tagMap
		 * @return
		 */
		private int getTagSegmentId(String tagStr, Map<Integer, String> tagMap) {
			int tagSegId = -1;
			for (Entry<Integer, String> entry : tagMap.entrySet()) {
				if (tagStr.equals(entry.getValue())) {
					tagSegId = entry.getKey();
					break;
				}
			}
			return tagSegId;
		}

		/**
		 * 将ut节点下的标记信息进行转义
		 * @param value
		 * @return
		 */
		private String resetText(String value) {
			value = value.replaceAll("&quot;", "\"");
			return value;
		}

		/**
		 * 向 r8 hslixff 文件写入数据
		 */
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("UTF-8"));
		}

		/**
		 * 开始填充文本
		 * @param source
		 * @param target
		 * @param match
		 * @param isLock
		 * @param isXtranslate
		 * @throws IOException
		 */
		private void writeSegment(String source, String target, int match, boolean isLock, boolean isXtranslate)
				throws IOException {
			String approved = match > 100 ? "yes" : "no";
			String matchStr = match > 0 ? " hs:matchType=\"TM\" hs:quality=\"" + match + "\"" : "";
			if (!isLock) {
				writeString("  <trans-unit id=\"" + segId + "\" xml:space=\"preserve\" approved=\"" + approved
						+ "\" addedEndTagNum='" + addedEndTagNum + "'>\n" + "    <source xml:lang=\"" + userSourceLang
						+ "\">");
			} else {
				writeString("  <trans-unit id=\"" + segId + "\" xml:space=\"preserve\" translate=\"no\" approved=\""
						+ approved + "\" addedEndTagNum='" + addedEndTagNum + "'>\n" + "    <source xml:lang=\""
						+ userSourceLang + "\">");
			}
			writeString(source + "</source>\n ");
			if (!target.equals("")) {
				if (!targetLang.equals("")) {
					writeString("    <target xml:lang=\"" + targetLang + "\" state=\"new\"" + matchStr + ">" + target
							+ "</target>\n");
				} else {
					writeString("    <target state=\"new\"" + matchStr + ">" + target + "</target>\n");
				}
			}

			if (match >= 100 && !targetLang.equals("")) {
				if (!isXtranslate) {
					writeString("    <alt-trans xml:space=\"preserve\"  match-quality=\"100\" tool=\"Trados or Similar\">"
							+ "    <source xml:lang=\"" + userSourceLang + "\">");
				} else {
					writeString("    <alt-trans xml:space=\"preserve\"  match-quality=\"100\" tool=\"Trados or Similar\"  origin=\"Xtranslate\">"
							+ "    <source xml:lang=\"" + userSourceLang + "\">");
				}
				writeString(source + "</source>");
				writeString("    <target xml:lang=\"" + targetLang + "\"\n>");
				writeString(target + "</target>\n");
				writeString("    </alt-trans>\n");
			}
			writeString("  </trans-unit>\n");
			segId++;
		}

		/**
		 * 向　xliff 文件中的　ph 标记添加　id 属性。
		 */
		private void addPHIdToXliff() throws Exception {
			int phId = 1;
			VTDGen vg = new VTDGen();
			boolean parseResult = vg.parseFile(xliffFile, true);
			if (!parseResult) {
				throw new Exception();
			}

			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot childAP = new AutoPilot(vn);
			XMLModifier xm = new XMLModifier(vn);
			VTDUtils vu = new VTDUtils(vn);
			String xpath = "/xliff/file/body//trans-unit";
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				Map<Integer, String> phMap = new HashMap<Integer, String>();

				// 先循环　src 中的　ph　标记
				vn.push();
				childAP.selectXPath("./source/descendant::ph");
				while (childAP.evalXPath() != -1) {
					phMap.put(phId, vu.getElementFragment());
					xm.insertAttribute(" id='" + (phId++) + "'");
				}
				vn.pop();

				// 再循环　target 中的　ph 标记
				vn.push();
				childAP.selectXPath("./target//ph");
				while (childAP.evalXPath() != -1) {
					boolean findExsit = false;
					String tgtPhFrag = vu.getElementFragment();
					for (Entry<Integer, String> entry : phMap.entrySet()) {
						String srcPhFrag = entry.getValue();
						if (tgtPhFrag.equals(srcPhFrag)) {
							findExsit = true;
							int srcPhId = entry.getKey();
							phMap.remove(srcPhId);
							xm.insertAttribute(" id='" + srcPhId + "'");
							break;
						}
					}

					if (!findExsit) {
						xm.insertAttribute(" id='" + (phId++) + "'");
					}
				}
				vn.pop();
			}
			xm.output(xliffFile);
		}

	}

	/**
	 * Copy file.
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @throws Exception
	 *             the exception
	 */
	private static void copyFile(String in, String out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}

	private static void copyFile(String oldFile, String newFilePath, String strOldEncoding, String strNewEncoding)
			throws Exception {
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamRead = null;
		BufferedReader bufferRead = null;

		BufferedWriter newFileBW = null;
		OutputStreamWriter outputStreamWriter = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileInputStream = new FileInputStream(oldFile);
			inputStreamRead = new InputStreamReader(fileInputStream, strOldEncoding);
			bufferRead = new BufferedReader(inputStreamRead);

			fileOutputStream = new FileOutputStream(newFilePath, false);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream, strNewEncoding);
			newFileBW = new BufferedWriter(outputStreamWriter);

			String strTSVLine = "";

			while ((strTSVLine = bufferRead.readLine()) != null) {
				if (strTSVLine.equals("")) {
					continue;
				}
				newFileBW.write(strTSVLine.replaceAll("Shift_JIS", "UTF-8"));
				newFileBW.write("\n");
			}
		} finally {
			if (bufferRead != null)
				bufferRead.close();
			if (newFileBW != null) {
				newFileBW.flush();
				newFileBW.close();
			}
		}
	}

	/**
	 * 将ut节点下的标记信息进行转义
	 * @param value
	 * @return
	 */
	private static String cleanString(String value) {
		value = value.replaceAll("&", "&amp;");
		value = value.replaceAll("<", "&lt;");
		value = value.replaceAll("\"", "&quot;");
		value = value.replaceAll(">", "&gt;");
		return value;
	}

	public static void main(String[] args) {
//		String filePath = "ttx/testXml.xml";
//		VTDGen vg = new VTDGen();
//		vg.parseFile(filePath, true);
//		VTDNav vn = vg.getNav();
//		try {
//			AutoPilot ap = new AutoPilot(vn);
//			AutoPilot childAP = new AutoPilot(vn);
//			ap.selectXPath("/books/book[@id='4']");
//			XMLModifier xm = new XMLModifier(vn);
//			while (ap.evalXPath() != -1) {
//				vn.push();
//				childAP.selectXPath("preceding::book");
//				while (childAP.evalXPath() != -1) {
//					System.out.println(vn.toString(vn.getAttrVal("id")));
//				}
//				vn.pop();
//			}
//			xm.output(filePath);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			
			copyFile("/Users/Mac/Desktop/a.txt", "/Users/Mac/Desktop/b.txt", "UTF-8", "UTF-16LE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}