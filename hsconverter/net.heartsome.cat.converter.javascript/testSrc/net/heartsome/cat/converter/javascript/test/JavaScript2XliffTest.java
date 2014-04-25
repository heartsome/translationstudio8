package net.heartsome.cat.converter.javascript.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.javascript.JavaScript2Xliff;

import org.junit.Before;
import org.junit.Test;


public class JavaScript2XliffTest {
	public static JavaScript2Xliff converter = new JavaScript2Xliff();
	private static String srcFile = "rc/Test.js";
	private static String xlfFile = "rc/Test.js.xlf";
	private static String sklFile = "rc/Test.js.skl";
	
	@Before
	public void setUp(){
		File xlf = new File(xlfFile);
		if(xlf.exists()){
			xlf.delete();
		}

		File skl = new File(sklFile);
		if(skl.exists()){
			skl.delete();
		}
	}
	
	@Test
	public void testConvert() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "en-US"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$

		Map<String, String> result = converter.convert(args, null);
		String xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		File xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());
	}

}
