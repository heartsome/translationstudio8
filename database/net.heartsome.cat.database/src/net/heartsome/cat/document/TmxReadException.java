/**
 * TmxReadException.java
 *
 * Version information :
 *
 * Date:2013-1-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxReadException extends Exception {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int exceptionCode;

	public TmxReadException() {
		super();
	}

	public TmxReadException(String message) {
		super(message);
		this.exceptionCode = -1;
	}
	
	public TmxReadException(String message, int exceptionCode) {
		this(message);
		this.exceptionCode = exceptionCode;
	}

	public TmxReadException(String message, Throwable cause) {
		super(message, cause);
		exceptionCode = -1;
	}
	
	public TmxReadException(String message, Throwable cause, int exceptionCode) {
		this(message, cause);
		this.exceptionCode =  exceptionCode;
	}
	
	public int getExcetpitonCode(){
		return exceptionCode;
	}
}
