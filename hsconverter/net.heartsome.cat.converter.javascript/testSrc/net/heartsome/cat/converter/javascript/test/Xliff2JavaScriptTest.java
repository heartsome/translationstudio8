package net.heartsome.cat.converter.javascript.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.javascript.Xliff2JavaScript;

import org.junit.Before;
import org.junit.Test;


public class Xliff2JavaScriptTest {
	public static Xliff2JavaScript converter = new Xliff2JavaScript();
	private static String tgtFile = "rc/Test_zh-CN.js";
	private static String xlfFile = "rc/Test.js.xlf";
	private static String sklFile = "rc/Test.js.skl";
	
	@Before
	public void setUp(){
		File tgt = new File(tgtFile);
		if(tgt.exists()){
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
