package net.heartsome.cat.converter.xml.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.xml.Xliff2Xml;

import org.junit.Before;
import org.junit.Test;


public class Xliff2XmlTest {
	public static Xliff2Xml converter = new Xliff2Xml();
	private static String xlfFile = "rc/Test.xml.xlf";
	private static String sklFile = "rc/Test.xml.skl";
	private static String tgtFile = "rc/Test_en-US.xml";
	
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
		args.put(Converter.ATTR_TARGET_FILE, tgtFile); 
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile); 
		args.put(Converter.ATTR_SKELETON_FILE, sklFile); 
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); 
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
//		args.put(Converter.ATTR_PROGRAM_FOLDER,rootFolder);
		
		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

}
