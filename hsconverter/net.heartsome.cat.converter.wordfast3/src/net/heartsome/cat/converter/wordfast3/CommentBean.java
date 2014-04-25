package net.heartsome.cat.converter.wordfast3;

/**
 * 批注的 pojo 类
 * @author robert	2012-12-13
 */
public class CommentBean {
	/** 标注的作者，对应 creationid */
	private String user;
	
	/** 标注的添加时间 对应 creationdate */
	private String date;
	/** 标注的类型，对应 type */
	private String type;
	/** 标注的文件 */
	private String commentText;
	/** wf 文件的批注节点的所有属性的字符串 */
	private String commentAttrStr;
	
	public CommentBean(){}
	
	public CommentBean(String user, String date, String type, String commentText, String commentAttrStr){
		this.user = user;
		this.date = date;
		this.type = type;
		this.commentText = commentText;
		this.commentAttrStr = commentAttrStr;
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
	
	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public String getCommentAttrStr() {
		return commentAttrStr;
	}

	public void setCommentAttrStr(String commentAttrStr) {
		this.commentAttrStr = commentAttrStr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
