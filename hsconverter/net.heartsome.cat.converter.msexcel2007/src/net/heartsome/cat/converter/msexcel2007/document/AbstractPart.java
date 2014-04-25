/**
 * AbstractPart.java
 *
 * Version information :
 *
 * Date:2012-8-1
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class AbstractPart {

	public static final Logger logger = LoggerFactory.getLogger(AbstractPart.class);
	
	protected String partXmlPath;

	protected VTDUtils vu;

	protected XMLModifier xm;

	protected AbstractPart(String partXmlPath) throws InternalFileException {
		this.partXmlPath = partXmlPath;
		loadFile();
	}

	protected void loadFile() throws InternalFileException {
		File f = new File(this.partXmlPath);

		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		byte[] b = new byte[(int) f.length()];
		try {
			fis = new FileInputStream(f);
			fis.read(b);
		} catch (IOException e) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
		vg.setDoc(b);
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
			xm = new XMLModifier(vu.getVTDNav());
		} catch (VTDException e) {
			String message = Messages.getString("msexcel.converter.exception.msg1");
			message += "\nFile:"+f.getName()+"\n"+e.getMessage();
			throw new InternalFileException(message);
		}
	}

	public String getPartXmlPath() {
		return this.partXmlPath;
	}

	public void save() throws InternalFileException {
		FileOutputStream os;
		File f = new File(this.partXmlPath);
		try {
			os = new FileOutputStream(f);
			xm.output(os);
			os.close();
		} catch (Exception e) {
			throw new InternalFileException("保存文件出错\n" + e.getMessage());
		}
	}

	public void saveAndReload() throws InternalFileException {
		save();
		loadFile();
	}
}
