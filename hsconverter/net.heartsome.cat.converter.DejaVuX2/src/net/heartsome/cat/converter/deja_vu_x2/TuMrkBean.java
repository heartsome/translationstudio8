package net.heartsome.cat.converter.deja_vu_x2;

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
	private String comment;

	/**
	 * <div>deja vu x2 xliff文件的状态，下面是duxlf至r8 xliff文件状态的转换</div>
	 * <table border='1' cellSpacing='1' cellSpadding='0'>
	 *  <tr><td>duxlf</td><td>R8 xliff</td><td>备注</td></tr>
	 *  <tr><td>needs-translation</td><td colSpan='2'>对应R8的未翻译与草稿</td></tr>
	 *  <tr><td>needs-review-translation</td><td>疑问</td><td></td></tr>
	 *  <tr><td>finish</td><td colSpan='2'>在du中的tu节点设置approved="yes"并在target节点上设置state="translated"，对应R8为已经批准</td></tr>
	 * </table>
	 */
	private String status;
	/** 是否锁定
	 *  <div style="color:red">在R8中的锁定格式为 &lt trans-unit translate="no"&gt ... &lt/trans-unit&gt，
	 *  <br>deja vu x2中与之一样</div>
	 */
	private boolean isLocked;
	
	public TuMrkBean(){}
	
	public TuMrkBean(String mid, String content, String status, boolean isSource){
		this.mid = mid;
		this.content = content;
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


	public boolean isSource() {
		return isSource;
	}

	public void setSource(boolean isSource) {
		this.isSource = isSource;
	}
	
	/**
	 * <div>deja vu x2 xliff文件的状态，下面是duxlf至r8 xliff文件状态的转换</div>
	 * <table border='1' cellSpacing='1' cellSpadding='0'>
	 *  <tr><td>duxlf</td><td>R8 xliff</td><td>备注</td></tr>
	 *  <tr><td>needs-translation</td><td colSpan='2'>对应R8的未翻译与草稿</td></tr>
	 *  <tr><td>needs-review-translation</td><td>疑问</td><td></td></tr>
	 *  <tr><td>finish</td><td colSpan='2'>在du中的tu节点设置approved="yes"并在target节点上设置state="translated"，对应R8为已经批准</td></tr>
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	


}
