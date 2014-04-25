package net.heartsome.cat.converter.trados2009;

import java.util.LinkedList;
import java.util.List;

/**
 * 这是sdlXliff文件trans-unit／source或target节点下mrk[mtype='seg']节点的内容 pojo类
 * @author robert	2012-06-29
 */
public class TuMrkBean {
	private String mid;
	private String content;
	private boolean isSource;
	/** 批注 */
	private List<CommentBean> commentList;
	/**
	 * <div>sdl文件的状态，下面是sdl至r8 xliff文件状态的转换</div>
	 * <table border='1' cellSpacing='1' cellSpadding='0'>
	 *  <tr><td>sdl</td><td>R8 xliff</td><td>备注</td></tr>
	 * 	<tr><td>Draft</td><td>new</td><td>草稿</td></tr>
	 * 	<tr><td>Translated</td><td>translated</td><td>已翻译－＞完成翻译</td></tr>
	 * 	<tr><td>RejectedTranslation</td><td>new</td><td>翻译被否决－＞草稿</td></tr>
	 * 	<tr><td>ApprovedTranslation</td><td colSpan='2'>批准翻译，在trans-unit节点中设置 approved="yes"</td></tr>
	 * 	<tr><td>RejectedSignOff</td><td colSpan='2'>签发被拒绝，回到批准状态</td></tr>
	 * <tr><td>ApprovedSignOff</td><td>state="signed-off"</td><td>签发</td></tr>
	 * </table>
	 */
	private String status;
	/** 是否锁定
	 *  <div style="color:red">在R8中的锁定格式为 &lt trans-unit translate="no"&gt ... &lt/trans-unit&gt，
	 *  <br>而在sdl中这种表达方式为不需翻译，即不会加载到翻译工具界面中。</div>
	 */
	private boolean isLocked;
	
	private String quality;
	
	private String matchType;
	public TuMrkBean(){}
	
	public TuMrkBean(String mid, String content, List<CommentBean> commentList, String status, boolean isSource){
		this.mid = mid;
		this.content = content;
		this.commentList = commentList;
		this.isSource = isSource;
		this.status = status;
	}
	
	/**
	 * 判断当前mrk节点的文本是否为空
	 * @return
	 */
	public boolean isTextNull(){
		if (content == null || "".equals(content)) {
			return true;
		}
		return false;
	}
	

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<CommentBean> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<CommentBean> commentList) {
		this.commentList = commentList;
	}

	public boolean isSource() {
		return isSource;
	}

	public void setSource(boolean isSource) {
		this.isSource = isSource;
	}
	
	/**
	 * <div>sdl文件的状态，下面是sdl至r8 xliff文件状态的转换，这个状态与锁定分开</div>
	 * <table border='1' cellSpacing='1' cellSpadding='0'>
	 *  <tr><td>sdl</td><td>R8 xliff</td><td>备注</td></tr>
	 * 	<tr><td>Draft</td><td>new</td><td>草稿</td></tr>
	 * 	<tr><td>Translated</td><td>translated</td><td>已翻译－＞完成翻译</td></tr>
	 * 	<tr><td>RejectedTranslation</td><td>new</td><td>翻译被否决－＞草稿</td></tr>
	 * 	<tr><td>ApprovedTranslation</td><td colSpan='2'>批准翻译，在trans-unit节点中设置 approved="yes"</td></tr>
	 * 	<tr><td>RejectedSignOff</td><td colSpan='2'>签发被拒绝，回到批准状态</td></tr>
	 * <tr><td>ApprovedSignOff</td><td>state="signed-off"</td><td>签发</td></tr>
	 * <tr><td>percent</td><td>hs:quality</td><td>匹配率</td></tr>
	 * <tr><td>origin</td><td>hs:matchType="TM"</td><td>匹配类型</td></tr>
	 * </table>
	 */
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	/** 是否锁定
	 *  <div style="color:red">在R8中的锁定格式为 &lt trans-unit translate="no"&gt ... &lt/trans-unit&gt，
	 *  <br>而在sdl中这种表达方式为不需翻译，即不会加载到翻译工具界面中。</div>
	 */
	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	/**
	 * 添加全局批注变量
	 * @param fileComments
	 */
	public void addFileComments(List<CommentBean> fileComments){
		if (fileComments == null || fileComments.size() <= 0) {
			return;
		}
		if (commentList == null) {
			commentList = new LinkedList<CommentBean>();
		}
		commentList.addAll(fileComments);
	}

	/**
	 * 匹配率
	 * @return
	 */
	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

}
