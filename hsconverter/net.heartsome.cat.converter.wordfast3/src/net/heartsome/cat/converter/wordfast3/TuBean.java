package net.heartsome.cat.converter.wordfast3;

import java.util.List;

/**
 * wordFast 的 txml 文件与 hsxliff 文件的翻译单元的 pojo 类
 * @author robert	2012-12-13
 */
public class TuBean {
	private String srcContent;
	private String tgtContent;
	/** 匹配率 */
	private String match;
	private List<CommentBean> commentList;
	
	public TuBean(){}
	
	public TuBean(String srcContent, String tgtContent, String match, List<CommentBean> commentList){
		this.srcContent = srcContent;
		this.tgtContent = tgtContent;
		this.match = match;
		this.commentList = commentList;
	}

	public String getSrcContent() {
		return srcContent;
	}

	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}

	public String getTgtContent() {
		return tgtContent;
	}

	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public List<CommentBean> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<CommentBean> commentList) {
		this.commentList = commentList;
	}
	
	

}
