package net.heartsome.cat.ts.ui.xliffeditor.nattable.exception;

/**
 * 在程序中遇到了非预期的类型时抛出此异常。如重写 NatTabel 的 paintControl 方法中，预期 NatTable 的直接底层布局为 CompositeLayer，如果不是，则抛出异常。
 * @author cheney
 * @since JDK1.6
 */
public class UnexpectedTypeExcetion extends RuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1875863249595095139L;

	/**
	 * 构建一个无详细信息的 UnexpectedTypeExcetion。
	 */
	public UnexpectedTypeExcetion() {
		super();
	}

	/**
	 * 构建一个有详细说明的 UnexpectedTypeExcetion。
	 * @param s
	 *            描述异常的信息说明。
	 */
	public UnexpectedTypeExcetion(String s) {
		super(s);
	}

	/**
	 * 构建一个包含详细说明和堆栈的 UnexpectedTypeExcetion。
	 * @param message
	 *            描述异常的信息说明。
	 * @param cause
	 *            堆栈信息。
	 */
	public UnexpectedTypeExcetion(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 构建一个包含堆栈的 UnexpectedTypeExcetion。
	 * @param cause
	 *            堆栈信息。
	 */
	public UnexpectedTypeExcetion(Throwable cause) {
		super(cause);
	}

}
