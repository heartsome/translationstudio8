package net.heartsome.cat.converter.msoffice2003.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.msoffice2003.Xliff2MSOffice;

import org.junit.BeforeClass;
import org.junit.Test;

public class Xliff2MSOfficeTest {
	public static Xliff2MSOffice converter = new Xliff2MSOffice();
	private static String rootFolder = "C:\\Documents and Settings\\John\\workspace\\HSTS7\\";
	private static String tgtDocFile = "rc/Test_en-US.doc";
	private static String xlfDocFile = "rc/Test.doc.xlf";
	private static String sklDocFile = "rc/Test.doc.skl";

	private static String tgtXlsFile = "rc/Test_en-US.xls";
	private static String xlfXlsFile = "rc/Test.xls.xlf";
	private static String sklXlsFile = "rc/Test.xls.skl";

	private static String tgtPptFile = "rc/Test_en-US.ppt";
	private static String xlfPptFile = "rc/Test.ppt.xlf";
	private static String sklPptFile = "rc/Test.ppt.skl";

	@BeforeClass
	public static void setUp() {
		File tgt = new File(tgtDocFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtXlsFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtPptFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test
	public void testConvertXls() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtXlsFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfXlsFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklXlsFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);
		Map<String, String> result = converter.convert(args, null);
		String tgt = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(tgt);

		File tgtFile = new File(tgt);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

	@Test
	public void testConvertDoc() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklDocFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String tgt = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(tgt);

		File tgtFile = new File(tgt);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

	@Test
	public void testConvertPpt() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtPptFile); //$NON-NLS-1$
		args.put(Converter.ATTR_XLIFF_FILE, xlfPptFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SKELETON_FILE, sklPptFile); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_LANGUAGE, "zh-CN"); //$NON-NLS-1$
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8"); //$NON-NLS-1$
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		args.put(Converter.ATTR_SRX, rootFolder + "srx/default_rules.srx");
		args.put(Converter.ATTR_PROGRAM_FOLDER, rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String tgt = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(tgt);

		File tgtFile = new File(tgt);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}
}
