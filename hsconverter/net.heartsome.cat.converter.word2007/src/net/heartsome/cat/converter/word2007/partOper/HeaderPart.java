package net.heartsome.cat.converter.word2007.partOper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.word2007.PartOperate;
import net.heartsome.cat.converter.word2007.XliffInputer;
import net.heartsome.cat.converter.word2007.XliffOutputer;

/**
 * 处理 页眉 header*.xml 文件
 * @author robert
 */
public class HeaderPart extends PartOperate {

	/**
	 * 正向转换所用到的构造方法
	 * @param partPath
	 * @param xlfOutput
	 * @param segmenter
	 * @throws Exception
	 */
	public HeaderPart(String partPath, XliffOutputer xlfOutput, StringSegmenter segmenter) throws Exception{
		super(partPath, xlfOutput, segmenter);
		
		init();
	}
	
	/**
	 * 逆转换用到的构造函数
	 * @param partPath
	 * @param xlfInput
	 */
	public HeaderPart (String partPath, XliffInputer xlfInput) throws Exception {
		super(partPath, xlfInput);
		
		init();
	}
	
	
	private void init() throws Exception {
		nameSpaceMap = new HashMap<String, String>();
		nameSpaceMap.put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		nameSpaceMap.put("v", "urn:schemas-microsoft-com:vml");
		loadFile(nameSpaceMap);
	}

	/**
	 * <div style='color:red;'>这个 run() 主体方法内的内容，headerPart, footerPart 除了xpath 不一致，其他应保持一致 , 切记 。<br/>
	 * 另外，页眉页脚是没有批注，尾注，脚注等信息</div>。
	 */
	@Override
	protected void converter() throws Exception {
		operateTransAttributes("/w:hdr/descendant::w:p/w:r/w:pict/v:shape/@alt");

		// 处理的单元为 w:p
		String xpath = "/w:hdr/descendant::w:p";
		ap.selectXPath(xpath);
		
		while(ap.evalXPath() != -1){
			List<String> transAttrList = new LinkedList<String>();
			
			vn.push();
			// 寻找可翻译的属性，并添加到 transAttrList 中，这时的属性值已经为占位符了。
			childAP.selectXPath("./w:r/w:pict/v:shape/@alt");
			while(childAP.evalXPath() != -1){
				String altText = vn.toRawString(vn.getCurrentIndex() + 1);
				transAttrList.add(altText);
			}
			vn.pop();
			
			analysisNodeP();
			
			// transAttrPlaceHolderStr 为可翻译属性的占位符，通过占位符获取其代替的值，再将值进行分割后写入 trans-unit 中。
			if (transAttrList.size() > 0) {
				for (String transAttrPlaceHolderStr : transAttrList) {
					String transAttrStr = translateAttrMap.get(transAttrPlaceHolderStr);
					if (transAttrStr != null) {
						String segIdStr = getSegIdFromPlaceHoderStr(transAttrPlaceHolderStr);
						String[] segs = segmenter.segment(transAttrStr);
						for(String seg : segs){
							// 生成 trans-unit 节点
							xlfOutput.addTransUnit(seg, segIdStr);
						}
					}
				}
			}
		}
		
		xm.output(partPath);
	}

	
	
	
	
	
// ------------------------------------------  下面是逆转换的代码  ------------------------------------------------
	@Override
	protected void reverseConvert() throws Exception {
		// 处理的单元为 w:p
		String xpath = "/w:hdr/descendant::w:p";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			// DOLATER 先处理所有的属性
			analysisReversePnode();
		}
		xm.output(partPath);
		
		//再处理可翻译属性
		xpath = "/w:hdr/descendant::w:p/w:r/w:pict/v:shape/@alt";
		reverseTranslateAttributes(xpath);
	}
	
	
	public static void main(String[] args) {
		String text = "1=-2   - English";
		System.out.println(text.replaceAll(" ", ""));
		
		String transAttrPlaceHolderStr = "%%%21%%%";
		int firstPlaceHIdx = transAttrPlaceHolderStr.indexOf("%%%");
		System.out.println(firstPlaceHIdx);
		System.out.println(transAttrPlaceHolderStr.substring(firstPlaceHIdx + 3, transAttrPlaceHolderStr.indexOf("%%%", firstPlaceHIdx + 1)));
		
	}
	
	

}
