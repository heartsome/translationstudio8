package net.heartsome.cat.converter.memoq6;

public class TuBean {
	private String segId;
	private String srcText;
	private String tgtText;
	private String status;
	private boolean isLocked;
	private String note;
	
	
	public TuBean(){}


	public String getSegId() {
		return segId;
	}
	public void setSegId(String segId) {
		this.segId = segId;
	}
	public String getSrcText() {
		return srcText;
	}
	public void setSrcText(String srcText) {
		this.srcText = srcText;
	}
	public String getTgtText() {
		return tgtText;
	}
	public void setTgtText(String tgtText) {
		this.tgtText = tgtText;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	public boolean isTgtNull(){
		return tgtText == null || "".equals(tgtText);
	}
}
