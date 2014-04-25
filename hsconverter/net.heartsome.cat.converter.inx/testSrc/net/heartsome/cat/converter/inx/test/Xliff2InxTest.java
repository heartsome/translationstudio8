package net.heartsome.cat.converter.inx.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.inx.Xliff2Inx;

import org.junit.Test;

public class Xliff2InxTest {
	public static Xliff2Inx converter = new Xliff2Inx();
	private static String tgtFile1 = "rc/Test_en-US.inx";
	private static String sklFile1 = "rc/Test.inx.skl";
	private static String xlfFile1 = "rc/Test.inx.xlf";

	private static String tgtFile2 = "rc/Test10M_en-US.inx";
	private static String xlfFile2 = "rc/Test10M.inx.xlf";
	private static String sklFile2 = "rc/Test10M.inx.skl";

	private static String tgtFile3 = "rc/TestSC_en-US.inx";
	private static String xlfFile3 = "rc/TestSC.inx.xlf";
	private static String sklFile3 = "rc/TestSC.inx.skl";

	public void setUp() {
		File tgt = new File(tgtFile1);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtFile2);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtFile3);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test(timeout = 60000)
	public void testConvert() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtFile1);
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile1);
		args.put(Converter.ATTR_SKELETON_FILE, sklFile1);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());

		// 10M Inx
		args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtFile2);
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile2);
		args.put(Converter.ATTR_SKELETON_FILE, sklFile2);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");

		result = converter.convert(args, null);
		target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());

		// zh-CN Inx
		args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtFile3);
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile3);
		args.put(Converter.ATTR_SKELETON_FILE, sklFile3);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");

		result = converter.convert(args, null);
		target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}
}
