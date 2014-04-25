package net.heartsome.cat.converter.po.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.po.Po2Xliff;

import org.junit.Before;
import org.junit.Test;

public class Po2XliffTest {
	public static Po2Xliff converter = new Po2Xliff();
	private static String srcFile = "rc/Test.po";
	private static String xlfFile = "rc/Test.po.xlf";
	private static String sklFile = "rc/Test.po.skl";
	
	@Before
	public void setUp(){
		File skl = new File(sklFile);
		if(skl.exists()){
			skl.delete();
		}
		
		File xlf = new File(xlfFile);
		if(xlf.exists()){
			xlf.delete();
		}
	}
	
	@Test(timeout = 10000)
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
