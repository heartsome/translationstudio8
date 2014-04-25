package net.heartsome.cat.converter.msoffice2007.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.msoffice2007.MSOffice2Xliff;

import org.junit.BeforeClass;
import org.junit.Test;

public class MSOffice2XliffTest {
	public static MSOffice2Xliff converter = new MSOffice2Xliff();
	private static String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
	private static String srcDocxFile = "rc/Test.docx";
	private static String xlfDocxFile = "rc/Test.docx.xlf";
	private static String sklDocxFile = "rc/Test.docx.skl";

	private static String srcXlsxFile = "rc/Test.xlsx";
	private static String xlfXlsxFile = "rc/Test.xlsx.xlf";
	private static String sklXlsxFile = "rc/Test.xlsx.skl";

	private static String srcPptxFile = "rc/Test.pptx";
	private static String xlfPptxFile = "rc/Test.pptx.xlf";
	private static String sklPptxFile = "rc/Test.pptx.skl";

	@BeforeClass
	public static void setUp() {
		File xlf = new File(xlfDocxFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		File skl = new File(sklDocxFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfXlsxFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklXlsxFile);
		if (skl.exists()) {
			skl.delete();
		}

		xlf = new File(xlfPptxFile);
		if (xlf.exists()) {
			xlf.delete();
		}

		skl = new File(sklPptxFile);
		if (skl.exists()) {
			skl.delete();
		}
	}

	@Test
	public void testConvertXlsx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcXlsxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfXlsxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklXlsxFile); //$NON-NLS-1$
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
	public void testConvertDocx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcDocxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklDocxFile); //$NON-NLS-1$
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
	public void testConvertPptx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_SOURCE_FILE, srcPptxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfPptxFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklPptxFile); //$NON-NLS-1$
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
