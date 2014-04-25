package net.heartsome.cat.converter.trados2009;

import net.heartsome.cat.common.util.DateUtils;


/**
 * 批注信息，针对trados 2009的文件以及R8的XLIFF文件
 * @author robert	2012-07-02
 */
public class CommentBean {
	/** 标注的文件 */
	private String commentText;
	/** 标注的作者 */
	private String user;
	/** 标注的添加时间 */
	private String date;
	/** trados 2009文件批注的提示级别，取值为Low供参考, Medium警告, High错误 */
	private String severity;
	/** 是否是全局批注 */
	private boolean isCurrentSeg;
	
	public CommentBean(){}
	
	public CommentBean(String user, String date, String severity, String commentText, boolean isCurrentSeg){
		this.user = user;
		this.date = date;
		this.severity = severity;
		this.commentText = commentText;
		this.isCurrentSeg = isCurrentSeg;
	}
	
	
	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public boolean isCurrent() {
		return isCurrentSeg;
	}
	public void setCurrent(boolean isCurrentSeg) {
		this.isCurrentSeg = isCurrentSeg;
	}
	
	/**
	 * 获取R8状态下的批注文本
	 * @return
	 */
	public String getR8NoteText(){
		String newDate = DateUtils.dateToStr(DateUtils.strToDate(date));
		return newDate + ":" + commentText;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommentBean) {
			CommentBean curBean = (CommentBean) obj;
			if (curBean.getUser().equals(this.user) && curBean.getDate().equals(this.date) 
					&& curBean.getCommentText().equals(this.commentText)) {
				return true;
			}
		}
		return false;
	}

}
