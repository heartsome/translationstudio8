package net.heartsome.cat.ts.core.file;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQSequence;

import net.sf.saxon.xqj.SaxonXQDataSource;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Saxon 查询器，封装需要使用 XQuery 的查询
 * <div style='color:red;'>注意：此类请勿使用“Ctrl+Shift+F”格式化，此类中的 XQuery 语句在格式化后会加大阅读的难度</div>
 * @author Weachy
 * @version
 * @since JDK1.5
 */
public class SaxonSearcher {

	private static final String SOURCE_COLUMN = "source";
	private static final String TARGET_COLUMN = "target";

	public static ArrayList<String> sort(String xmlns, String fileName, String elementName, boolean isAsc)
			throws XQException {
		URI uri = new File(fileName).toURI();
		String uriPath = uri.getPath();

		ArrayList<String> rowIds = new ArrayList<String>();
		XQDataSource dataSource = new SaxonXQDataSource();

		XQConnection conn = dataSource.getConnection();
		String queryString = "for $file in doc(\'" + uriPath + "')/xliff/file,"
				+ " $tu in $file/body//trans-unit order by $tu/" + elementName + " " + (isAsc ? "" : "descending")
				+ " return <file original='{$file/@original}' tuid='{$tu/@id}'></file>";
		if (xmlns != null) {
			queryString = "declare default element namespace '" + xmlns + "';" + queryString;
		}
		XQExpression expression = conn.createExpression();
		XQSequence results = expression.executeQuery(queryString);
		while (results.next()) {
			Node node = results.getNode();
			String original = node.getAttributes().getNamedItem("original").getNodeValue();
			String tuid = node.getAttributes().getNamedItem("tuid").getNodeValue();
			String rowId = RowIdUtil.getRowId(fileName, original, tuid);
			rowIds.add(rowId);
		}
		// 释放资源
		results.close();
		expression.close();
		conn.close();

		return rowIds;
	}

	/**
	 * 排序
	 * @param xliffXmlnsMap
	 *            XLIFF 文件名和其命名空间的映射
	 * @param langFilterCondition
	 *            语言过滤条件
	 * @param elementName
	 *            排序的节点名
	 * @param isAsc
	 *            是否为升序
	 * @return RowId集合
	 * @throws XQException
	 *             ;
	 */
	public static ArrayList<String> sort(Map<String, String> xliffXmlnsMap, String langFilterCondition,
			String elementName, boolean isAsc) throws XQException {
		ArrayList<String> rowIds = new ArrayList<String>();
		if (xliffXmlnsMap == null || xliffXmlnsMap.isEmpty() || elementName == null || elementName.length() == 0) {
			return rowIds;
		}
		
		String[] xquery = getXQueryString(xliffXmlnsMap, langFilterCondition, Arrays.asList(elementName));
		if (xquery.length < 2) {
			return rowIds;
		}
		StringBuffer queryString = new StringBuffer(xquery[0]);
		queryString.append("for $tu in (");
		queryString.append(xquery[1]);
		queryString.append(") order by $tu/@" + elementName + (isAsc ? "" : " descending")
				+ " return <tu fileName='{$tu/@fileName}' original='{$tu/@original}' tuid='{$tu/@tuid}' />");

//		System.out.println(queryString);
		
		return qurey(queryString.toString());
	}

	/**
	 * 得到重复文本段
	 * @param xliffXmlnsMap
	 *			  XLIFF 文件名和其命名空间的映射
	 * @param langFilterCondition
	 * 			  语言过滤条件
	 * @return 
	 * 			  重复文本段的唯一标识
	 * @throws XQException ;
	 */
	public static ArrayList<String> getRepeatedSegment(Map<String, String> xliffXmlnsMap, String langFilterCondition)
			throws XQException {
		ArrayList<String> rowIds = new ArrayList<String>();
		if (xliffXmlnsMap == null || xliffXmlnsMap.isEmpty()) {
			return rowIds;
		}

		String[] xquery = getXQueryString(xliffXmlnsMap, langFilterCondition, Arrays.asList(SOURCE_COLUMN));
		if (xquery.length < 2) {
			return rowIds;
		}
		StringBuffer queryString = new StringBuffer(xquery[0]);
		queryString.append("for $t in (let $allTU_1 := ( ");
		queryString.append(xquery[1]);
		queryString.append(")," +
				"$allTU := for $allTU1 in $allTU_1  return <tu fileName='{$allTU1/@fileName}'  original='{$allTU1/@original}'  tuid='{$allTU1/@tuid}'  source='{normalize-space($allTU1/@source)}' /> , "
				+ "$id := (for $src in distinct-values($allTU/@" + SOURCE_COLUMN + ") "
					+ "return <root>{if (count($allTU[@" + SOURCE_COLUMN + "=$src])>1) "
					+ "then <src>{$src}</src> else ''}</root>)/src/text(), "
				+ "$tu := $allTU[@" + SOURCE_COLUMN + "=$id] " 
				+ "return $tu) order by $t/@" + SOURCE_COLUMN + " "
			+ "return <tu fileName='{$t/@fileName}' original='{$t/@original}' tuid='{$t/@tuid}' /> ");  // 最后再排序，将源文本相同的放在一起

//		String fileName1 = new File("/data/weachy/Desktop/hsts7.xlf").toURI().getPath().replace("'", "''");
//		String fileName2 = new File("/data/weachy/Desktop/a2.xlf").toURI().getPath().replace("'", "''");
		
//		String queryString = "let " 
//				+ "$allTU := ( " 
//					+ "for $file1 in doc('" + fileName1 + "')/xliff/file, $tu1 in $file1/body//trans-unit " 
//					+ "return <tu fileName='" + fileName1 + "' original='{$file1/@original}' tuid='{$tu1/@id}' src='{$tu1/source}' />, " 
//					+ "for $file2 in doc('" + fileName2 + "')/xliff/file, $tu2 in $file2/body//trans-unit " 
//					+ "return <tu fileName='" + fileName2 + "' original='{$file2/@original}' tuid='{$tu2/@id}' src='{$tu2/source}' /> " 
//				+ "), "
//				+ "$id := (for $src in distinct-values($allTU/@src) "
//					+ "return <root>{if (count($allTU[@src=$src])>1) "
//					+ "then <src>{$src}</src> else ''}</root>)/src/text(), "
//				+ "$tu := $allTU[@src=$id] "
//				+ "return $tu";
//
//		System.out.println(queryString);
		System.out.println(queryString.toString());

		return qurey(queryString.toString());
	}
	
	/**
	 * 得到译文不一致文本段	  UNDO 这里与BUG 2279有什么关系？robert
	 * @param xliffXmlnsMap
	 *			  XLIFF 文件名和其命名空间的映射
	 * @param langFilterCondition
	 * 			  语言过滤条件
	 * @return
	 * 		  译文不一致文本段
	 * @throws XQException ;
	 */
	public static ArrayList<String> getInconsistentTranslationsSegment(Map<String, String> xliffXmlnsMap, String langFilterCondition)
			throws XQException {
		ArrayList<String> rowIds = new ArrayList<String>();
		if (xliffXmlnsMap == null || xliffXmlnsMap.isEmpty()) {
			return rowIds;
		}
		
		String[] xquery = getXQueryString(xliffXmlnsMap, langFilterCondition, Arrays.asList(SOURCE_COLUMN, TARGET_COLUMN));
		if (xquery.length < 2) {
			return rowIds;
		}
		StringBuffer queryString = new StringBuffer(xquery[0]);
		queryString.append("for $t in (let $allTU := ( ");
		queryString.append(xquery[1]);
		queryString.append("), "
				+ "$srcId := distinct-values($allTU/@" + SOURCE_COLUMN + "), "
				+ "$id := (for $sId in $srcId "
					+ "return <root>{if (count(distinct-values($allTU[@" + SOURCE_COLUMN + "=$sId]/@" + TARGET_COLUMN + "))>1) "
					+ "then <src>{$sId}</src> else ''}</root>)/src/text(), "
				+ "$tu := $allTU[@" + SOURCE_COLUMN + "=$id] "
				+ "return $tu) order by $t/@" + SOURCE_COLUMN + " "
			+ "return <tu fileName='{$t/@fileName}' original='{$t/@original}' tuid='{$t/@tuid}' />");
		
//		queryString.append("), "
//					+ "$srcId := (for $src in distinct-values($allTU/@" + source + ") "
//					+ "return <root>{if (count($allTU[@" + source + "=$src])>1) "
//					+ "then <src>{$src}</src> else ''}</root>)/src/text() "
//				+ "return (for $sId in $srcId return (for " 
//						+ "$tgtId in (" 
//							+ "for $tgt in ($allTU[@" + source + "=$sId]/@" + target + ") "
//							+ "return <root>{if (count($tgt)=1) then <tgt tgt='{$tgt}' /> else ''}</root>" 
//						+ ")/tgt/@tgt, " 
//						+ "$tu1 in $allTU[@" + source + "=$sId and not(@" + target + "=$tgtId)] return $tu1)))" 
//						+ " order by $t/@" + source + " "
//			+ "return <tu fileName='{$t/@fileName}' original='{$t/@original}' tuid='{$t/@tuid}' /> ");

		return qurey(queryString.toString());
	}
	
	/**
	 * 查询
	 * @param queryString 
	 * 			  XQuery查询语句
	 * @return	  RowId集合
	 * @throws XQException ;
	 */
	private static ArrayList<String> qurey(String queryString) throws XQException {
		XQDataSource dataSource = new SaxonXQDataSource();
		XQConnection conn = null;
		XQExpression expression = null;
		XQSequence results = null;
		try {
			conn = dataSource.getConnection();
			expression = conn.createExpression();
			
			results = expression.executeQuery(queryString);
			LinkedHashSet<String> set = new LinkedHashSet<String>();
			while (results.next()) {
				Node node = results.getNode();
				String fileName = node.getAttributes().getNamedItem("fileName").getNodeValue();
				String original = node.getAttributes().getNamedItem("original").getNodeValue();
				String tuid = node.getAttributes().getNamedItem("tuid").getNodeValue();

				// 解决 Windows 平台下，无法查询“重复文本段”的问题“：
				// 这里返回的是 URI，因此需要转成操作系统的标准文件路径。
				// 注：在 Winodws 平台中文件路径分隔符使用“\”，而在 URI 标准中文件路径分隔符使用“/”，并且会以“/”为根，
				//    因此，Windows 的路径“c:\test.txt”，使用 URI 表示为“/c:/test.txt”。
				fileName = new File(fileName).getAbsolutePath();

				String rowId = RowIdUtil.getRowId(fileName, original, tuid);
				set.add(rowId);
			}
			return new ArrayList<String>(set);
		} finally {			
			// 释放资源
			if (results != null && !results.isClosed()) {				
				results.close();
			}
			if (expression != null && !expression.isClosed()) {				
				expression.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
	}
	
	/**
	 * 得到XQuery语句
	 * @param xliffXmlnsMap 
	 *			  XLIFF 文件名和其命名空间的映射
	 * @param langFilterCondition 
	 * 			  语言过滤条件
	 * @param elementName 
	 * 			  元素名称
	 * @return XQuery语句。<br/>第一个字符串为 XQuery 语句声明部分。<br/>第二个字符串为得到一个或一个以上 XLIFF 文件所有翻译单元主要内容的 XQuery 语句;
	 */
	private static String[] getXQueryString(Map<String, String> xliffXmlnsMap, String langFilterCondition, List<String> elementNames) {
		String declare = "";  // 声明部分
		HashMap<String, String> nsAliasMap = new HashMap<String, String>();  // 所有命名空间和别名的映射
		if (xliffXmlnsMap != null && !xliffXmlnsMap.isEmpty()) {
			HashSet<String> set = new HashSet<String>(xliffXmlnsMap.values());
			int i = 0;
			for (String ns : set) {
				if (ns == null || "".equals(ns)) {
					continue;
				}
				declare += "declare namespace ns" + i + "='" + ns + "'; ";
				nsAliasMap.put(ns, "ns" + i);
				i++;
			}
		}

		HashMap<String, String> fileURIs = new HashMap<String, String>();
		for (String fileName : xliffXmlnsMap.keySet()) {
			fileURIs.put(fileName, new File(fileName).toURI().getPath().replace("'", "''")/* 处理文件名有“'”符号的情况 */);
		}
		
		if (langFilterCondition == null || "".equals(langFilterCondition)) {
			langFilterCondition = "";
		} else {
			langFilterCondition = "[" + langFilterCondition + "]";
		}
		String queryString = "";
		List<String> args;
		MessageFormat messageFormat;
		int i = 0;
		for (String fileName : fileURIs.keySet()) {
			String varFile = "$file" + i;
			String varTU = "$tu" + i;
			String namespace = xliffXmlnsMap.get(fileName);
			String nsAlias = nsAliasMap.get(namespace);  // 命名空间别名
			if (nsAlias == null) {
				nsAlias = "";
			}
			if (!"".equals(nsAlias)) {
				nsAlias += ":";
			}
			String message = "for {1} in doc(\''{0}'')/{4}xliff/{4}file{3}, {2} in {1}/{4}body//{4}trans-unit "
				+ "return <tu fileName=''{0}'' original='''{'{1}/@original'}''' tuid='''{'{2}/@id'}''' ";
			for (int j = 5; j < 5 + elementNames.size(); j++) {
				message += "{" + j + "}='''{'{2}/{4}{" + j + "}/text()'}''' ";
			}
			message += "/>, ";
			messageFormat = new MessageFormat(message);
			
			args = new ArrayList<String>(Arrays.asList(fileURIs.get(fileName), varFile, varTU,
					langFilterCondition, nsAlias));
			args.addAll(elementNames);	
			queryString += messageFormat.format(args.toArray(new String[]{}));
			i++;
		}
		queryString = queryString.substring(0, queryString.length() - ", ".length()); // 去掉末尾的“, ”
		return new String[] { declare, queryString };
	}

	private static void testSort() {
		long sTime = System.currentTimeMillis();
		HashMap<String, String> map = new HashMap<String, String>();{
			map.put("/data/weachy/Desktop/a1.xlf", "urn:oasis:names:tc:xliff:document:1.1");
			map.put("/data/weachy/Desktop/a2.xlf", "urn:oasis:names:tc:xliff:document:1.2");
			map.put("/data/weachy/Desktop/hsts7.xlf", "urn:oasis:names:tc:xliff:document:1.1");
		}
		try {
			List<String> rowIds = sort(map,	"upper-case(@source-language)='EN' and upper-case(@target-language)='ZH-CN'", "source", true);
			for (String string : rowIds) {
				System.out.println(string);
			}
		} catch (XQException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - sTime);
	}
	
	private static void testGetRepeatedSegment() {
		long sTime = System.currentTimeMillis();

		HashMap<String, String> map = new HashMap<String, String>();{
			map.put("/home/robert/Desktop/The Silmarillion.txt", "urn:oasis:names:tc:xliff:document:1.1");
			map.put("/home/robert/Desktop/heartsome.docx.xlf", "urn:oasis:names:tc:xliff:document:1.2");
		}
		XLFHandler handler = new XLFHandler();
		handler.openFiles(new ArrayList<String>(Arrays.asList(
				"/home/robert/Desktop/translate test.txt.xlf", "/home/robert/Desktop/heartsome.docx.xlf")
			), null);
		List<String> rowIds = handler.getRepeatedSegment("upper-case(@source-language)='EN' and upper-case(@target-language)='ZH-CN'");
		for (String rowId : rowIds) {
			System.out.println(rowId);
			String src = handler.getSrcContent(rowId);
			System.out.println(src);
		}
		System.out.println(System.currentTimeMillis() - sTime);
	}
	
	/**
	 * 得到所有需要繁殖翻译的文本段的rowId，用于繁殖翻译 robert 2012-04-03	//UNDO 这里还没有完善，因为text()后还有多个空格的情况，见Bug #2279。
	 * @param xliffXmlnsMap
	 * @param langFilterCondition
	 * @return ;
	 * @throws XQException 
	 */
	public static Map<String, List<String>> getPropagateTranslationsRowIds(Map<String, String> xliffXmlnsMap, String langFilterCondition) throws XQException{
		if (xliffXmlnsMap == null || xliffXmlnsMap.isEmpty()) {
			return null;
		}
		StringBuffer querySB = new StringBuffer();
		String declare = "";  // 声明部分
		HashMap<String, String> nsAliasMap = new HashMap<String, String>();  // 所有命名空间和别名的映射
		if (xliffXmlnsMap != null && !xliffXmlnsMap.isEmpty()) {
			HashSet<String> set = new HashSet<String>(xliffXmlnsMap.values());
			int i = 0;
			for (String ns : set) {
				if (ns == null || "".equals(ns)) {
					continue;
				}
				declare += "declare namespace ns" + i + "='" + ns + "'; ";
				nsAliasMap.put(ns, "ns" + i);
				i++;
			}
		}

		HashMap<String, String> fileURIs = new HashMap<String, String>();
		for (String fileName : xliffXmlnsMap.keySet()) {
			fileURIs.put(fileName, new File(fileName).toURI().getPath().replace("'", "''")/* 处理文件名有“'”符号的情况 */);
		}
		
		if (langFilterCondition == null || "".equals(langFilterCondition)) {
			langFilterCondition = "";
		} else {
			langFilterCondition = "[" + langFilterCondition + "]";
		}
		String queryString1 = "";
		String queryString2 = "";
		List<String> args;
		MessageFormat messageFormat1;
		MessageFormat messageFormat2;
		int i = 0;
		for (String fileName : fileURIs.keySet()) {
			String varFile = "$file" + i;	//{1}
			String varTU = "$tu" + i;	//{2}
			String namespace = xliffXmlnsMap.get(fileName);
			String nsAlias = nsAliasMap.get(namespace);  // 命名空间别名	{4}
			if (nsAlias == null) {
				nsAlias = "";
			}
			if (!"".equals(nsAlias)) {
				nsAlias += ":";
			}
			//查询出源文与译文不为空，并且未加锁状态的所有节点的rowid
			String message1 = "for {1} in doc(\''{0}'')/{4}xliff/{4}file{3}, {2} in {1}/{4}body//{4}trans-unit[({4}source/text()!='''' or {4}source/*) and ({4}target/text()!='''') and not(@translate=''no'')] \n"
				+ "return <tu fileName=''{0}'' original='''{'{1}/@original'}''' tuid='''{'{2}/@id'}''' source='''{'{2}/{4}source/text()'}''' /> " + (i == fileURIs.size() -1 ? "\n" : ", \n");
			
			String message2 = "for {1} in doc(\''{0}'')/{4}xliff/{4}file{3}, {2} in {1}/{4}body//{4}trans-unit[({4}source/text()!='''' or {4}source/*) and not({4}target/text()!='''') and not(@translate=''no'')] \n"
				+ "return <tu fileName=''{0}'' original='''{'{1}/@original'}''' tuid='''{'{2}/@id'}''' source='''{'{2}/{4}source/text()'}''' /> " + (i == fileURIs.size() -1 ? "\n" : ", \n");
			
			messageFormat1 = new MessageFormat(message1);
			messageFormat2 = new MessageFormat(message2);
			
			args = new ArrayList<String>(Arrays.asList(fileURIs.get(fileName), varFile, varTU,
					langFilterCondition, nsAlias));
			queryString1 += messageFormat1.format(args.toArray(new String[]{}));
			queryString2 += messageFormat2.format(args.toArray(new String[]{}));
			i++;
		}
		
		querySB.append(declare + "\n");
		querySB.append("let $matchTU := (");
		querySB.append(queryString1 + ")\n");
		
		querySB.append("let $matchedTU := (");
		querySB.append(queryString2 + ")\n");
		
		//下面是测试部份了
//		querySB.append("for $rootTU in $matchedTU return <root fileName='{$rootTU/@fileName}' original='{$rootTU/@original}' tuid='{$rootTU/@tuid}' />");
		
		querySB.append("for $rootTU in $matchTU return <root fileName='{$rootTU/@fileName}' original='{$rootTU/@original}' tuid='{$rootTU/@tuid}'>{ \n");
		querySB.append("for $tu in $matchedTU \n");
		querySB.append("return if($rootTU/@source=$tu/@source) then <tu fileName='{$tu/@fileName}' original='{$tu/@original}' tuid='{$tu/@tuid}' />  else '' \n");
		querySB.append("}</root>");
		System.out.println("querySB.toString() = "  + querySB.toString());
		return PropagateQurey(querySB.toString());
	}
	
	/**
	 * 繁殖翻译文本段的查询 robert 2012-04-03
	 * @param queryString
	 *            XQuery查询语句
	 * @return RowId集合
	 * @throws XQException
	 *             ;
	 */
	public static Map<String, List<String>> PropagateQurey(String queryString) throws XQException {
		XQDataSource dataSource = new SaxonXQDataSource();
		XQConnection conn = null;
		XQExpression expression = null;
		XQSequence results = null;
		try {
			conn = dataSource.getConnection();
			expression = conn.createExpression();
			
			results = expression.executeQuery(queryString);
			Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
			while (results.next()) {
				Node node = results.getNode();
//				System.out.println("node.getChildNodes().getLength() = " + node.getChildNodes().getLength());
				if (node.getChildNodes().getLength() >= 1) {
					String rootFileName = node.getAttributes().getNamedItem("fileName").getNodeValue();
					rootFileName = new File(rootFileName).getAbsolutePath();
					String rootOriginal = node.getAttributes().getNamedItem("original").getNodeValue();
					String rootTuid = node.getAttributes().getNamedItem("tuid").getNodeValue();
					String rootRowId = RowIdUtil.getRowId(rootFileName, rootOriginal, rootTuid);
					if (!resultMap.keySet().contains(rootRowId)) {
						resultMap.put(rootRowId, new ArrayList<String>());
					}
					NodeList nodeList = node.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						if (nodeList.item(i).getAttributes() == null) {
							continue;
						}
						String fileName = nodeList.item(i).getAttributes().getNamedItem("fileName").getNodeValue();
						fileName = new File(fileName).getAbsolutePath();
						String original = nodeList.item(i).getAttributes().getNamedItem("original").getNodeValue();
						String tuid = nodeList.item(i).getAttributes().getNamedItem("tuid").getNodeValue();
						
						String rowId = RowIdUtil.getRowId(fileName, original, tuid);
						resultMap.get(rootRowId).add(rowId);
					}
				}
				
			}
			return resultMap;
		} finally {			
			// 释放资源
			if (results != null && !results.isClosed()) {				
				results.close();
			}
			if (expression != null && !expression.isClosed()) {				
				expression.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
	}
	
	
	/**
	 * 测试过滤重复文本段	robert	2012-06-11
	 * @throws XQException 
	 */
	private static void testRpeateed() throws XQException{
		String xqueryStr_1 = "declare namespace ns0='urn:oasis:names:tc:xliff:document:1.2'; " +
				"for $t in (let $allTU_1 := ( " +
				"	for $file0 in doc('/home/robert/workspace/runtime-UltimateEdition.product/test/XLIFF/zh-CN/user_defineed_filter4.doc.xlf')/ns0:xliff/ns0:file[upper-case(@source-language)='EN-US' " +
				"		and upper-case(@target-language)='ZH-CN'], " +
				"	$tu0 in $file0/ns0:body//ns0:trans-unit " +
				"		return <tu fileName='/home/robert/workspace/runtime-UltimateEdition.product/test/XLIFF/zh-CN/user_defineed_filter4.doc.xlf' original='{$file0/@original}' tuid='{$tu0/@id}' source='{$tu0/ns0:source/text()}' />), " +
				"	$allTU := for $allTU1 in $allTU_1  return <tu fileName='{$allTU1/@fileName}'  original='{$allTU1/@original}'  tuid='{$allTU1/@tuid}'  source='{normalize-space($allTU1/@source)}' /> ," +
				"	$id := (for $src in distinct-values($allTU/@source) " +
				"		return <root>{if (count($allTU[@source=$src])>1) then <src>{$src}</src> else ''}</root>)/src/text(), " +
				"	$tu := $allTU[@source=$id] return $tu) order by $t/@source " +
				"		return <tu fileName='{$t/@fileName}' original='{$t/@original}' tuid='{$t/@tuid}' /> ";
		
		String xqueryStr = "declare namespace ns0='urn:oasis:names:tc:xliff:document:1.2'; \n" +
		"declare function local:getPureText ($srcText1 as xs:anyType) as xs:anyType {\n" +
		"let $result := srcText1 \n" +
		"return  $result };  \n" +
		
		"for $t in (let $allTU := ( \n" +
		"	for $file0 in doc('/home/robert/workspace/runtime-UltimateEdition.product/test/XLIFF/zh-CN/user_defineed_filter4.doc.xlf')/ns0:xliff/ns0:file[upper-case(@source-language)='EN-US'  \n" +
		"		and upper-case(@target-language)='ZH-CN'],  \n" +
		"	$tu0 in $file0/ns0:body//ns0:trans-unit  \n" +
		"		return <tu fileName='/home/robert/workspace/runtime-UltimateEdition.product/test/XLIFF/zh-CN/user_defineed_filter4.doc.xlf' original='{$file0/@original}' tuid='{$tu0/@id}' source='{$tu0/ns0:source/text()}' />) \n" +
		" 	return $allTU )\n " +
		"	return <tu fileName='{$t/@fileName}' original='{$t/@original}' tuid='{$t/@tuid}' source='{$t/@source}'/>  \n";
		
		XQDataSource dataSource = new SaxonXQDataSource();
		XQConnection conn = null;
		XQExpression expression = null;
		XQSequence results = null;
		try {
			conn = dataSource.getConnection();
			expression = conn.createExpression();
			
			results = expression.executeQuery(xqueryStr);
			while (results.next()) {
				Node node = results.getNode();
				String fileName = node.getAttributes().getNamedItem("fileName").getNodeValue();
				String original = node.getAttributes().getNamedItem("original").getNodeValue();
				String tuid = node.getAttributes().getNamedItem("tuid").getNodeValue();
				String source = node.getAttributes().getNamedItem("source").getNodeValue();
				System.out.println(source);
//				System.out.println(tuid);
			}
			return;
		} finally {			
			// 释放资源
			if (results != null && !results.isClosed()) {				
				results.close();
			}
			if (expression != null && !expression.isClosed()) {				
				expression.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
	}
	
	public static void main(String[] args) throws XQException {
		testGetRepeatedSegment();
		testRpeateed();
	}
}
