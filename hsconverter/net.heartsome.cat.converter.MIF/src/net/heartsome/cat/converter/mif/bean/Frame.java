package net.heartsome.cat.converter.mif.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Frame {
	private int offset;
	private int endOffset;
	private String id;

	private List<TextRect> textRects;

	public Frame() {
		textRects = new ArrayList<TextRect>();
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
