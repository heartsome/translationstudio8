package net.heartsome.cat.converter.openoffice.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.openoffice.Xliff2OpenOffice;

import org.junit.BeforeClass;
import org.junit.Test;

public class Xliff2OpenOfficeTest {
	public static Xliff2OpenOffice converter = new Xliff2OpenOffice();
	private String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
	private static String xlfODTFile = "rc/Test.odt.xlf";
	private static String sklODTFile = "rc/Test.odt.skl";
	private static String tgtODTFile = "rc/Test_en-US.odt";

	private static String xlfODSFile = "rc/Test.ods.xlf";
	private static String sklODSFile = "rc/Test.ods.skl";
	private static String tgtODSFile = "rc/Test_en-US.ods";

	private static String xlfODGFile = "rc/Test.odg.xlf";
	private static String sklODGFile = "rc/Test.odg.skl";
	private static String tgtODGFile = "rc/Test_en-US.odg";

	@BeforeClass
	public static void setUp() {
		File tgt = new File(tgtODSFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtODTFile);
		if (tgt.exists()) {
			tgt.delete();
		}

		tgt = new File(tgtODGFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test
	public void testConvertODS() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtODSFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfODSFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklODSFile);
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
	public void testConvertODT() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtODTFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfODTFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklODTFile);
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
	public void testConvertODG() throws ConverterException {
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtODGFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfODGFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklODGFile);
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
