package net.heartsome.cat.ts.ui.external;

/**
 * 导出异常，批量处理中，抛出此异常意味着此任务结束，其他任务继续。
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class ExportCanceledException extends Exception {

	/**  serialVersionUID. */
	private static final long serialVersionUID = -4741066001274188704L;

	public ExportCanceledException() {
		super();
	}
	
	public ExportCanceledException(String message) {
		super(message);
	}
	
	public ExportCanceledException(String message, Throwable cause) {
		super(message, cause);
	}
}
