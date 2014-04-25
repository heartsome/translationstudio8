package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.generator.LicenseIdGenerator;

public class LicenseIdValidator {
	
	private boolean isTrial;
	private LicenseIdGenerator gen;
	
	public LicenseIdValidator(String licenseId) {
		gen = new LicenseIdGenerator(licenseId);
	}

	public boolean checkLicense() {
		if (!gen.checkLength()) {
			return false;
		}
		
		String temp = gen.getIsTrial();
		if (!Constants.TYPE_TMEP.equals(temp) && !Constants.TYPE_BUSINESS.equals(temp)) {
			return false;
		}
		
		isTrial = Constants.TYPE_TMEP.equals(temp);
		if (!System.getProperty("TSVersion").equals(gen.getProductId())) {
			return false;
		}
		
		temp = gen.getVersion();
		if (!"U".equals(temp) && !"F".equals(temp) && !"P".equals(temp) && !"L".equals(temp)) {
			return false;
		}
		
		return true;
	}
	
	public boolean checkEdition() {
		if (!System.getProperty("TSEdition").equals(gen.getVersion())) {
			return false;
		}
		
		System.getProperties().setProperty("TSHelp", "true");
		return true;
	}
	
	public boolean getType() {
		return isTrial;
	}
}
