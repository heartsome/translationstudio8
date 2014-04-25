package net.heartsome.cat.converter.mif.bean;

public class Table {
	private String id;
	private int offset;
	private int endOffset;

	public Table() {

	}
	
	public boolean validate(){
		if(id == null || id.equals("")){
			return false;
		}
		if(offset == 0 && endOffset == 0){
			return false;
		}
		return true;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
