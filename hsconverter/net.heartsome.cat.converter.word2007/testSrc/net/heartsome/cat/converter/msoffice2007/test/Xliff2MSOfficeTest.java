package net.heartsome.cat.converter.msoffice2007.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.msoffice2007.Xliff2MSOffice;

import org.junit.BeforeClass;
import org.junit.Test;

public class Xliff2MSOfficeTest {
	public static Xliff2MSOffice converter = new Xliff2MSOffice();
	private String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
	private static String xlfDocxFile = "rc/Test.docx.xlf";
	private static String sklDocxFile = "rc/Test.docx.skl";
	private static String tgtDocxFile = "rc/Test_en-US.docx";

	private static String xlfXlsxFile = "rc/Test.xlsx.xlf";
	private static String sklXlsxFile = "rc/Test.xlsx.skl";
	private static String tgtXlsxFile = "rc/Test_en-US.xlsx";

	private static String xlfPptxFile = "rc/Test.pptx.xlf";
	private static String sklPptxFile = "rc/Test.pptx.skl";
	private static String tgtPptxFile = "rc/Test_en-US.pptx";

	@BeforeClass
	public static void setUp() {
		File tgt = new File(tgtXlsxFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtDocxFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtPptxFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test
	public void testConvertXlsx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtXlsxFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfXlsxFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklXlsxFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		// args.put(Converter.ATTR_PROGRAM_FOLDER,rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

	@Test
	public void testConvertDocx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtDocxFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfDocxFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklDocxFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		// args.put(Converter.ATTR_PROGRAM_FOLDER,rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

	@Test
	public void testConvertPptx() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtPptxFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfPptxFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklPptxFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "UTF-8");
		args.put(Converter.ATTR_CATALOGUE, rootFolder
				+ "catalogue/catalogue.xml");
		// args.put(Converter.ATTR_PROGRAM_FOLDER,rootFolder);

		Map<String, String> result = converter.convert(args, null);
		String target = result.get(Converter.ATTR_TARGET_FILE);
		assertNotNull(target);

		File tgtFile = new File(target);
		assertNotNull(tgtFile);
		assertTrue(tgtFile.exists());
	}

}
