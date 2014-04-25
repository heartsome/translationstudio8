package net.heartsome.license.generator;

import net.heartsome.license.utils.RandomUtils;

public class LicenseIdGenerator {
	private String licenseId;

	public LicenseIdGenerator(String licenseId) {
		this.licenseId = licenseId;
	}

	public LicenseIdGenerator(String productId, String version, String isTrial) {
		this.licenseId = productId + version + isTrial + RandomUtils.generateRandom(20);	
	}
	
	public String getProductId() {
		if (licenseId == null) {
			return null;
		}
		
		return licenseId.substring(0, 2);
	}
	
	public String getVersion() {
		if (licenseId == null) {
			return null;
		}
		return licenseId.substring(2, 3);
	}
	
	public String getIsTrial() {
		if (licenseId == null) {
			return null;
		}
		return licenseId.substring(3, 4);
	}
	
	public boolean checkLength() {
		return licenseId.length() == 24;
	}
}
