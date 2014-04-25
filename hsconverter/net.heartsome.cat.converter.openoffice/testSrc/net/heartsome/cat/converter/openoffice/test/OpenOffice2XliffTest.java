package net.heartsome.cat.converter.openoffice.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.openoffice.OpenOffice2Xliff;

import org.junit.BeforeClass;
import org.junit.Test;

public class OpenOffice2XliffTest {
	public static OpenOffice2Xliff converter = new OpenOffice2Xliff();
	private static String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
	private static String srcODTFile = "rc/Test.odt";
	private static String xlfODTFile = "rc/Test.odt.xlf";
	private static String sklODTFile = "rc/Test.odt.skl";

	private static String srcODSFile = "rc/Test.ods";
	private static String xlfODSFile = "rc/Test.ods.xlf";
	private static String sklODSFile = "rc/Test.ods.skl";

	private static String srcODGFile = "rc/Test.odg";
	private static String xlfODGFile = "rc/Test.odg.xlf";
	private static String sklODGFile = "rc/Test.odg.skl";

	@BeforeClass
	public static void setUp() {
		File xlf = new File(xlfODTFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		File skl = new File(sklODTFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfODSFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklODSFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfODGFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklODGFile);
		if (skl.exists()) {
			skl.delete();
		}
	}

	//
	// @Test(expected = ConverterException.class)
	// public void testConvertMissingCatalog() throws ConverterException {
	// Map<String, String> args = new HashMap<String, String>();
	//		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
	// // args.put(Converter.ATTR_CATALOGUE, rootFolder +
	// // "catalogue/catalogue.xml");
	// args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
	// args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);
	//
	// Map<String, String> result = converter.convert(args, null);
	// String xliff = result.get(Converter.ATTR_XLIFF_FILE);
	// assertNotNull(xliff);
	//
	// File xlfFile = new File(xliff);
	// assertNotNull(xlfFile);
	// assertTrue(xlfFile.exists());
	// }
	//
	// @Test(expected = ConverterException.class)
	// public void testConvertMissingSRX() throws ConverterException {
	// Map<String, String> args = new HashMap<String, String>();
	//		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
	// args.put(Converter.ATTR_CATALOGUE, rootFolder
	// + "catalogue/catalogue.xml");
	// // args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
	// args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);
	//
	// Map<String, String> result = converter.convert(args, null);
	// String xliff = result.get(Converter.ATTR_XLIFF_FILE);
	// assertNotNull(xliff);
	//
	// File xlfFile = new File(xliff);
	// assertNotNull(xlfFile);
	// assertTrue(xlfFile.exists());
	// }
	//
	// @Test(expected = ConverterException.class)
	// public void testConvertMissingINI() throws ConverterException {
	// Map<String, String> args = new HashMap<String, String>();
	//		args.put(Converter.ATTR_SOURCE_FILE, srcODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
	//		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
	// args.put(Converter.ATTR_CATALOGUE, rootFolder
	// + "catalogue/catalogue.xml");
	// args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
	// // args.put(Converter.ATTR_PROGRAM_FOLDER,rootFolder);
	//
	// Map<String, String> result = converter.convert(args, null);
	// String xliff = result.get(Converter.ATTR_XLIFF_FILE);
	// assertNotNull(xliff);
	//
	// File xlfFile = new File(xliff);
	// assertNotNull(xlfFile);
	// assertTrue(xlfFile.exists());
	// }

	@Test
	public void testConvertODS() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODSFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODSFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODSFile); //$NON-NLS-1$
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
	}

	@Test
	public void testConvertODT() throws ConverterException {
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
	}

	@Test
	public void testConvertODG() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcODGFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfODGFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklODGFile); //$NON-NLS-1$
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
	}
}
