package net.heartsome.cat.converter.inx.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.inx.Inx2Xliff;

import org.junit.AfterClass;
import org.junit.Test;

public class Inx2XliffTest {
	public static Inx2Xliff converter = new Inx2Xliff();
	private static String srcFile1 = "rc/Test.inx";
	private static String sklFile1 = "rc/Test.inx.skl";
	private static String xlfFile1 = "rc/Test.inx.xlf";

	private static String srcFile2 = "rc/Test10M.inx";
	private static String xlfFile2 = "rc/Test10M.inx.xlf";
	private static String sklFile2 = "rc/Test10M.inx.skl";

	private static String srcFile3 = "rc/TestSC.inx";
	private static String xlfFile3 = "rc/TestSC.inx.xlf";
	private static String sklFile3 = "rc/TestSC.inx.skl";

	public void setUp() {
		File skl = new File(sklFile1);
		if (skl.exists()) {
			skl.delete();
		}

		File xlf = new File(xlfFile1);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklFile2);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfFile2);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklFile3);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfFile3);
		if (xlf.exists()) {
			xlf.delete();
		}
	}

	@Test(expected = ConverterException.class)
	public void testConvertMissingCatalog() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "en-US"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		// args.put(Converter.ATTR_CATALOGUE, rootFolder +
		// "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		File xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());
	}

	@Test(expected = ConverterException.class)
	public void testConvertMissingSRX() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "en-US"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		// args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		File xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());
	}

	@AfterClass
	public static void testConvert() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile1); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "en-US"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		File xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());

		// 10M INX
		args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile2); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile2); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile2); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "en-US"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		result = converter.convert(args, null);
		xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());

		// zh-CN INX
		args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcFile3); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile3); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklFile3); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		result = converter.convert(args, null);
		xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());
	}

}
