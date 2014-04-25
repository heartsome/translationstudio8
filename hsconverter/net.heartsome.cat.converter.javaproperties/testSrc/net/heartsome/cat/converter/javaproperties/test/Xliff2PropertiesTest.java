package net.heartsome.cat.converter.javaproperties.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.javaproperties.Xliff2Properties;

import org.junit.Before;
import org.junit.Test;

public class Xliff2PropertiesTest {
	public static Xliff2Properties converter = new Xliff2Properties();
	private static String tgtFile = "rc/Test_zh-CN.properties";
	private static String xlfFile = "rc/Test.properties.xlf";
	private static String sklFile = "rc/Test.properties.skl";

	@Before
	public void setUp() {
		File tgt = new File(tgtFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}
	
	@Test
	public void testConvert() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}
}
