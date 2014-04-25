package net.heartsome.cat.converter.word2007.common;

public class DocxConverterException  extends Exception{
	private static final long serialVersionUID = 1L;

	public DocxConverterException() {
		super();
	}

	public DocxConverterException(String message) {
		super("\n"+message);
	}
}
