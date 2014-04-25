package net.heartsome.cat.tmx.converter;

import java.io.FileNotFoundException;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.tmx.converter.bean.TmxTemple;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxWriter extends AbstractWriter {

	private String attibuteString;
	/**
	 * 
	 */
	public TmxWriter() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public TmxWriter(String filePath) throws FileNotFoundException {
		super(filePath);
		// TODO Auto-generated constructor stub
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getHeaderXml()
	 */
	@Override
	protected String getHeaderXml(String srcLang) {
		return TmxTemple.getDefaultTmxPrefix(srcLang);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getEndXml()
	 */
	@Override
	protected String getEndXml() {
		// TODO Auto-generated method stub
		return "</body>\n</tmx>";
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#writeTmxTU(net.heartsome.cat.common.bean.TmxTU)
	 */
	@Override
	protected void writeTmxTU(TmxTU tu) {
		// TODO Auto-generated method stub
		writeXmlString(Model2String.TmxTU2TmxXmlString(tu, true,getAttibuteString()));

	}
	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getWriterEnconding()
	 */
	@Override
	protected String getWriterEnconding() {
		// TODO Auto-generated method stub
		return "utf-8";
	}
	/** @return the attibuteString */
	public String getAttibuteString() {
		return attibuteString;
	}
	/** @param attibuteString the attibuteString to set */
	public void setAttibuteString(String attibuteString) {
		this.attibuteString = attibuteString;
	}

}