package net.heartsome.cat.converter.rtf.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.rtf.Xliff2Rtf;

import org.junit.Before;
import org.junit.Test;

public class Xliff2RtfTest {
	public static Xliff2Rtf converter = new Xliff2Rtf();
	private static String tgtODTFile = "rc/TestODT_en-US.rtf";
	private static String xlfODTFile = "rc/TestODT.rtf.xlf";
	private static String sklODTFile = "rc/TestODT.rtf.skl";

	private static String tgtDocFile = "rc/TestDoc_en-US.rtf";
	private static String xlfDocFile = "rc/TestDoc.rtf.xlf";
	private static String sklDocFile = "rc/TestDoc.rtf.skl";

	@Before
	public void setUp() {
		File tgt = new File(tgtODTFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtDocFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test
	public void testConvertODT() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtODTFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

	@Test
	public void testConvertDoc() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtDocFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklDocFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
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
