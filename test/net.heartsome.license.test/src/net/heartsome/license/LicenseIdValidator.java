package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.generator.LicenseIdGenerator;

public class LicenseIdValidator {
	private boolean isTrial;
	
	public boolean checkLicense(String licenseId) {
		if (licenseId.length() != 24) {
			return false;
		}
		
		LicenseIdGenerator gen = new LicenseIdGenerator(licenseId);
		String temp = gen.getIsTrial();
		if (!Constants.TYPE_TMEP.equals(temp) && !Constants.TYPE_BUSINESS.equals(temp)) {
			return false;
		}
		isTrial = Constants.TYPE_TMEP.equals(temp);
		if (!System.getProperty("TSVersion").equals(gen.getProductId())) {
			return false;
		}
		
		if (!System.getProperty("TSEdition").equals(gen.getVersion())) {
			return false;
		}
		
		return true;
	}
	
	public boolean getType() {
		return isTrial;
	}
}
