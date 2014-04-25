/**
 * AbstractDrawing.java
 *
 * Version information :
 *
 * Date:2012-8-8
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document.drawing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.converter.msexcel2007.common.Constants;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLByteOutputStream;
import com.ximpleware.XMLModifier;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class AbstractDrawing {
	
	public static final Logger logger = LoggerFactory.getLogger(AbstractDrawing.class);
	protected VTDUtils vu;

	private String xmlContent;

	protected XMLModifier xm;

	public AbstractDrawing(String xmlContent) {
		this.xmlContent = appendNS(xmlContent);
		loadXML();
	}

	protected void save() {
		try {
			XMLByteOutputStream xbos = new XMLByteOutputStream(xm.getUpdatedDocumentSize());
			xm.output(xbos);
			this.xmlContent = new String(xbos.getXML());
			xbos.close();
			// System.out.println(this.xmlContent);
		} catch (VTDException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
	}

	protected void saveAndReload() {
		save();
		loadXML();
	}

	public String getXmlContent(){
		return this.xmlContent.replace(getNSDeclare2XML(), "");
	}
	
	public void setXmlContent(String newValue){
		this.xmlContent = newValue;
	}
	
	/**
	 * 获取带有命名空间申明的AtutoPilot对象
	 * @return ;
	 */
	protected AutoPilot getDeclareNSAutoPilot() {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.declareXPathNameSpace(Constants.NAMESPACE_DRAWING_PREFIX_XDR, Constants.NAMESPACE_DRAWING_URL_XDR);
		ap.declareXPathNameSpace(Constants.NAMESPACE_DRAWING_PREFIX_A, Constants.NAMESPACE_DRAWING_URL_A);
		return ap;
	}

	private void loadXML() {
		VTDGen vg = new VTDGen();
		vg.setDoc(this.xmlContent.getBytes());
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
			xm = new XMLModifier(vu.getVTDNav());
		} catch (VTDException e) {
			logger.error("",e);
		}
	}

	private String appendNS(String xml) {
		String nsDecl = getNSDeclare2XML();
		StringBuffer xbf = new StringBuffer(xml);
		int p = xbf.indexOf(">");
		xbf.insert(p, nsDecl);
		return xbf.toString();
	}

	private String getNSDeclare2XML(){
		StringBuffer bf = new StringBuffer();
		bf.append(" xmlns:").append(Constants.NAMESPACE_DRAWING_PREFIX_XDR);
		bf.append("=\"").append(Constants.NAMESPACE_DRAWING_URL_XDR).append("\"");
		bf.append(" xmlns:").append(Constants.NAMESPACE_DRAWING_PREFIX_A);
		bf.append("=\"").append(Constants.NAMESPACE_DRAWING_URL_A).append("\"");
		return bf.toString();
	}

}
