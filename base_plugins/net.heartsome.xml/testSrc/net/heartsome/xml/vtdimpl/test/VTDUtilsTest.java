package net.heartsome.xml.vtdimpl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.xml.vtdimpl.VTDUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class VTDUtilsTest {
	private final static String testFile = "testSrc/net/heartsome/xml/vtdimpl/test/Test_UTF16LE.txt.xlf";
	private static VTDNav vn = null;
	private static VTDUtils vu = null;

	@BeforeClass
	public static void setUp() throws Exception {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(testFile, true)) {
			vn = vg.getNav();
			vu = new VTDUtils(vn);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		vn = null;
		vu = null;
	}

	@Test(expected = NavException.class)
	public void testVTDUtils() throws NavException {
		vu = new VTDUtils(null);
	}
	
	@Test
	public void testVTDNavStatus(){
		assertNotNull(vn);
		assertNotNull(vu);
	}
	
	@Test
	public void testGetChildContent() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=1]");
		int inx = ap.evalXPath();
		assertNotSame(-1, inx);
		assertEquals("This <ph id=\"1\">&lt;b&gt;</ph>is the first <ph id=\"2\">&lt;/b&gt;</ph>test.",vu.getChildContent("source"));
		
		ap.resetXPath();
		ap.selectXPath("//trans-unit");
		inx = ap.evalXPath();
		assertNotSame(-1, inx);
		String txt = vu.getChildContent("note");
		assertEquals("The note 1.", txt);
	}

	@Test
	public void testGetChildrenContent() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=2]");
		ap.evalXPath();
		Vector<String> notes = vu.getChildrenContent("note");
		assertEquals(3, notes.size());
		assertEquals("The 1 note.",notes.get(0));
		assertEquals("The 2 note.",notes.get(1));
		assertEquals("The 3 note.",notes.get(2));
	}
	
	@Test
	public void testGetCurrentElementAttributsNoParams() throws XPathEvalException, NavException, XPathParseException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=2]");
		ap.evalXPath();
		
		Hashtable<String,String> eAtts = new Hashtable<String,String>();
		eAtts.put("approved","yes");
		eAtts.put("id","1");
		eAtts.put("merged-trans","yes");
		eAtts.put("reformat","yes");
		eAtts.put("size-unit","pixel");
		eAtts.put("translate","yes");
		eAtts.put("xml:space","preserve");
		eAtts.put("hs:ext","yes");
		eAtts.put("xsi:test", "test");
		
		Hashtable<String,String> aAtts = vu.getCurrentElementAttributs();
		assertEquals(eAtts,aAtts);
	}
	
	@Test
	public void testGetCurrentElementAttributs1Param() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=2]");
		ap.evalXPath();
		
		Hashtable<String,String> eAtts = new Hashtable<String,String>();
		eAtts.put("approved","yes");
		eAtts.put("id","1");
		eAtts.put("merged-trans","yes");
		eAtts.put("reformat","yes");
		eAtts.put("size-unit","pixel");
		eAtts.put("translate","yes");
		eAtts.put("xml:space","preserve");
		eAtts.put("hs:ext","yes");
		eAtts.put("xsi:test", "test");
		
		Hashtable<String,String> aAtts = vu.getCurrentElementAttributs("es","http://www.heartsome.net.cn/2008/XLFExtension");
		assertEquals(eAtts,aAtts);
	}
	
	@Test
	public void testGetCurrentElementAttributs2Params() throws XPathParseException, XPathEvalException, NavException{
		
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=2]");
		ap.evalXPath();
		
		Hashtable<String,String> eAtts = new Hashtable<String,String>();
		eAtts.put("approved","yes");
		eAtts.put("id","1");
		eAtts.put("merged-trans","yes");
		eAtts.put("reformat","yes");
		eAtts.put("size-unit","pixel");
		eAtts.put("translate","yes");
		eAtts.put("xml:space","preserve");
		eAtts.put("hs:ext","yes");
		eAtts.put("xsi:test", "test");
		
		Hashtable<String,String> ns = new Hashtable<String,String>();
		ns.put("http://www.heartsome.net.cn/2008/XLFExtension", "es");
		ns.put("http://www.w3.org/2001/XMLSchema-instance", "es");
		Hashtable<String,String> aAtts = vu.getCurrentElementAttributs(ns);
		assertEquals(eAtts,aAtts);
	}
	

	@Test
	public void testGetElementContent() throws NavException, XPathParseException, XPathEvalException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit/note[position()=1]");
		int inx = ap.evalXPath();
		assertNotSame(-1, inx);
		String txt = vu.getElementContent();
		assertEquals("The note 1.", txt);
	}
	

	@Test
	public void testGetElementContent1Param() throws NavException, XPathParseException, XPathEvalException{
		String txt = vu.getElementContent("//trans-unit/note[position()=1]");
		assertEquals("The note 1.", txt);
	}
	
	@Test
	public void testGetElementPureText() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=1]/source");
		int inx = ap.evalXPath();
		assertNotSame(-1, inx);
		String txt = vu.getElementPureText();
		assertEquals("This is the first test.", txt);
	}
	
	@Test
	public void testGetVTDNav(){
		assertNotNull(vu.getVTDNav());
		assertSame(vn,vu.getVTDNav());
	}
	
	@Test
	public void testGetCurrentElementName() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=1]/source");
		int inx = ap.evalXPath();
		String name = vu.getCurrentElementName(inx);
		assertEquals(name, "source");
	}
	
	@Test
	public void testGetCurrentElementNameWithoutInx() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit[position()=1]/source");
		ap.evalXPath();
		String name = vu.getCurrentElementName();
		assertEquals(name, "source");
	}
	
	@Test
	public void testGetChildElementsCount() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/xliff");
		ap.evalXPath();
		int count = vu.getChildElementsCount();
		assertEquals(2, count);
	}
	
	@Test
	public void testGetChildElementsCount2() throws XPathParseException, XPathEvalException, NavException{
		int count = vu.getChildElementsCount("/xliff");
		assertEquals(2, count);
	}
	
	@Test
	public void testGetElementFragment() throws XPathParseException, XPathEvalException, NavException{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/xliff/file/header/tool");
		ap.evalXPath();
		String str = vu.getElementFragment();
		assertEquals(str, "<tool tool-id=\"XLFEditor auto-Quick Translation\" tool-name=\"HSStudio\" />");
	}
}
