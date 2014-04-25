package net.heartsome.cat.converter.mif.bean;

public class TextFlow {
	private String textRectId;
	private int offset;
	private int endOffset;

	public String getTextRectId() {
		return textRectId;
	}

	public boolean validate(){
		if(offset == 0 || endOffset == 0){
			return false;
		}
		return true;
	}
	
	public void setTextRectId(String textRectId) {
		this.textRectId = textRectId;
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

}