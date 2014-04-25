package net.heartsome.cat.converter.taggedrtf.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.taggedrtf.Xliff2TaggedRtf;

import org.junit.Before;
import org.junit.Test;

public class Xliff2TaggedRtfTest {
	public static Xliff2TaggedRtf converter = new Xliff2TaggedRtf();
	private static String tgtFile = "rc/Test_en-US.rtf";
	private static String xlfFile = "rc/Test.rtf.xlf";
	private static String sklFile = "rc/Test.rtf.skl.tg.skl";

	@Before
	public void setUp() {
		File tgt = new File(tgtFile);
		if (tgt.exists()) {
			tgt.delete();
		}
	}

	@Test
	public void testConvertODT() throws ConverterException {
		String rootFolder = "/data/john/Workspaces/CAT/HSTS7/";
		Map<String, String> args = new HashMap<String, String>();
		args.put(Converter.ATTR_TARGET_FILE, tgtFile);
		args.put(Converter.ATTR_XLIFF_FILE, xlfFile);
		args.put(Converter.ATTR_SKELETON_FILE, sklFile);
		args.put(Converter.ATTR_SOURCE_ENCODING, "BIG5");
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
