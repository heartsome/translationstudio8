package net.heartsome.license;

public class WindowsSeries implements SeriesInterface {

	public String getSeries() {
		 try {
			 System.loadLibrary("win_x86_Series_" + System.getProperty("sun.arch.data.model") + "bit");
        } catch (UnsatisfiedLinkError e) {
        	return null;
        } catch (SecurityException sex) {
        	return null;
        }
		
		return new Series().getSeries();
	}
}
