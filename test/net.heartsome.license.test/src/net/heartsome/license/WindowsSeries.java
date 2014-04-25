package net.heartsome.license;

public class WindowsSeries implements SeriesInterface {

	static {
		try {
			// String realLibName = System.mapLibraryName("win_x86_Series");
			// File f = new File("lib", realLibName);
			// f = f.getCanonicalFile();
			// System.load(f.getAbsolutePath());

			System.loadLibrary("win_x86_Series_"
					+ System.getProperty("sun.arch.data.model") + "bit");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSeries() {
		return new Series().getSeries();
	}

}
