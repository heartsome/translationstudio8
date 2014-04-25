package net.heartsome.cat.converter.word2007.common;

/**
 * 正向转换时，读取到每个节点后存放相应的数据
 * @author robert	2012-08-10
 */
public class TagBean {
	/** 该标记的样式 */
	private String style;
	/** 文本值 */
	private String text;
	/** 文本值在整个文本段的起始值 */
	private int start;
	/** 文本值在整个文本段的结束值 */
	private int end;
	
	public TagBean (){}
	
	public TagBean(String style, String text, int start, int end){
		this.style = style;
		this.text = text;
		this.start = start;
		this.end = end;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public static void main(String[] args) {
		String seg = "The pump can be mounted on a standard IV pole or a horizontal mounting rail using the removable pole \" clamp  in Figure 2).";
		String text= "The pump can be mounted on a standard IV pole or a horizontal mounting rail using the removable pole \" clamp  in ";
		text = text.replace("(", "\\("); 
		System.out.println(text);
		System.out.println(seg.replaceFirst(text, ""));
	}
	
	

}
