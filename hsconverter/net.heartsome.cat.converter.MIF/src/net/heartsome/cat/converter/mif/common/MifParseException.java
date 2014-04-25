package net.heartsome.cat.converter.mif.common;

public class MifParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MifParseException() {
		super();
	}

	public MifParseException(String message) {
		super("\n"+message);
	}
}
