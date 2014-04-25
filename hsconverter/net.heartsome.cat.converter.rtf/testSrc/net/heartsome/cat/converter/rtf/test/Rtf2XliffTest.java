package net.heartsome.cat.converter.rtf.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.rtf.Rtf2Xliff;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class Rtf2XliffTest {
	public static Rtf2Xliff converter = new Rtf2Xliff();
	private static String srcODTFile = "rc/TestODT.rtf";
	private static String xlfODTFile = "rc/TestODT.rtf.xlf";
	private static String sklODTFile = "rc/TestODT.rtf.skl";

	private static String srcDocFile = "rc/TestDoc.rtf";
	private static String xlfDocFile = "rc/TestDoc.rtf.xlf";
	private static String sklDocFile = "rc/TestDoc.rtf.skl";

	@Before
	public void setUp() {
		File xlf = new File(xlfODTFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		File skl = new File(sklODTFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfDocFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklDocFile);
		if (skl.exists()) {
			skl.delete();
		}
	}

	@Test(expected = ConverterException.class)
	public void testConvertMissingCatalog() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
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
		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
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

	@Test(expected = ConverterException.class)
	public void testConvertMissingINI() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		// args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String xliff = result.get(Converter.ATTR_XLIFF_FILE);
		assertNotNull(xliff);

		File xlfFile = new File(xliff);
		assertNotNull(xlfFile);
		assertTrue(xlfFile.exists());
	}

	@Test
	public void testConvert() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
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

		args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklDocFile); //$NON-NLS-1$
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

	@AfterClass
	public static void finalConverter() throws ConverterException {

		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
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

		args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklDocFile); //$NON-NLS-1$
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
