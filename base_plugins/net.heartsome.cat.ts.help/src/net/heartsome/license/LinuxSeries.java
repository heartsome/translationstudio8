package net.heartsome.license;

//import java.io.File;
//import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

//import net.heartsome.license.utils.StringUtils;
//
//import org.eclipse.core.runtime.FileLocator;
//import org.eclipse.core.runtime.Platform;
//import org.safehaus.uuid.EthernetAddress;
//import org.safehaus.uuid.NativeInterfaces;

public class LinuxSeries implements SeriesInterface {

	public String getSeries() {
//		try {
////			NativeInterfaces.setLibDir(new File("lib"));
//			NativeInterfaces.setLibDir(new File(FileLocator.toFileURL(Platform.getBundle("net.heartsome.cat.ts.help").getEntry("")).getPath() 
//					+ File.separator + System.getProperty("sun.arch.data.model")));
//			EthernetAddress[] macs = NativeInterfaces.getAllInterfaces();
//			String series = "";
//			for (EthernetAddress a : macs) {
//				series += a.toString() + "+";
//			}
//			return "".equals(series) ? null : StringUtils.removeColon(series.substring(0, series.length() - 1));
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}	
		
		try {
			String series = "";
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface network = networkInterfaces.nextElement();
				if (!network.getName().startsWith("vmnet") && !network.getName().startsWith("vboxnet")) {
					byte[] mac = network.getHardwareAddress();
					if (mac != null && mac.length == 6 && !network.isLoopback() && !network.isVirtual()) {
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < mac.length; i++) {
							sb.append(String.format("%02x", mac[i]));
						}
						sb.append("+");
						series += sb.toString();
					}
				}
			}
			return "".equals(series) ? null : series.substring(0, series.length() - 1);
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}

}
