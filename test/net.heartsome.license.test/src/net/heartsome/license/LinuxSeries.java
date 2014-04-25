package net.heartsome.license;

import java.io.File;

import net.heartsome.license.utils.StringUtils;

import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.NativeInterfaces;

public class LinuxSeries implements SeriesInterface {

	@Override
	public String getSeries() {
//		try {
			NativeInterfaces.setLibDir(new File("lib"));
//			NativeInterfaces.setLibDir(new File(FileLocator.toFileURL(Platform.getBundle("net.heartsome.license").getEntry("")).getPath()));
			EthernetAddress[] macs = NativeInterfaces.getAllInterfaces();
			String series = "";
			for (EthernetAddress a : macs) {
				series += a.toString() + "+";
			}
			return "".equals(series) ? null : StringUtils.removeColon(series.substring(0, series.length() - 1));
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}	
	}

}
