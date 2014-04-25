package net.heartsome.cat.converter.msoffice2003.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.msoffice2003.MSOffice2Xliff;

import org.junit.BeforeClass;
import org.junit.Test;

public class MSOffice2XliffTest {
	public static MSOffice2Xliff converter = new MSOffice2Xliff();
	private static String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
	private static String srcDocFile = "rc/Test.doc";
	private static String xlfDocFile = "rc/Test.doc.xlf";
	private static String sklDocFile = "rc/Test.doc.skl";

	private static String srcXlsFile = "rc/Test.xls";
	private static String xlfXlsFile = "rc/Test.xls.xlf";
	private static String sklXlsFile = "rc/Test.xls.skl";

	private static String srcPptFile = "rc/Test.ppt";
	private static String xlfPptFile = "rc/Test.ppt.xlf";
	private static String sklPptFile = "rc/Test.ppt.skl";

	@BeforeClass
	public static void setUp() {
		File xlf = new File(xlfDocFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		File skl = new File(sklDocFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfXlsFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklXlsFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfPptFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklPptFile);
		if (skl.exists()) {
			skl.delete();
		}
	}

	@Test
	public void testConvertXls() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcXlsFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfXlsFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklXlsFile); //$NON-NLS-1$
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
	public void testConvertDoc() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklDocFile); //$NON-NLS-1$
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
	public void testConvertPpt() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcPptFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfPptFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklPptFile); //$NON-NLS-1$
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
