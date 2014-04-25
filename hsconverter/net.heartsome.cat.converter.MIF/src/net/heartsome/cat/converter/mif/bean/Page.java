package net.heartsome.cat.converter.mif.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Page {
	private int offset;
	private int endOffset;
	private String pageType;
	private String pageTag;

	private List<TextRect> textRects;
	
	public Page(){
		textRects = new ArrayList<TextRect>();
	}
	
	public boolean validate(){
		if(offset == 0 || endOffset == 0){
			return false;
		}
		if(pageType == null || pageType.equals("")){
			return false;
		}
		if(pageTag == null && pageTag.equals("")){
			return false;
		}
		
		return true;
	}
	
	/**
	 * return the TextRect order by vertical position
	 * @return
	 */
	public List<TextRect> getTextRects(){
		Collections.sort(textRects, new Comparator<TextRect>() {
			
			public int compare(TextRect o1, TextRect o2) {				
				return o1.getvPosition().compareTo(o2.getvPosition());
			}
		});
		
		return textRects;
	}	
	
	public void appendTextRect(TextRect tr) {
		this.textRects.add(tr);
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public String getPageTag() {
		return pageTag;
	}

	public void setPageTag(String pageTag) {
		this.pageTag = pageTag;
	}

}
