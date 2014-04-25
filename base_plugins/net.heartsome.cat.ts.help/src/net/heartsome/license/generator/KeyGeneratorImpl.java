package net.heartsome.license.generator;

import net.heartsome.license.ProtectionFactory;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.utils.RandomUtils;

public class KeyGeneratorImpl implements IKeyGenerator {
	
	private String installKey;

	public byte[] generateKey(String licenseId, String series, byte[] b) {
		String key = licenseId + Constants.SEPARATOR + series + Constants.SEPARATOR + generateInstallKey();
		return ProtectionFactory.getEncrypt().encrypt(b, key.getBytes());
	}

	public String generateInstallKey() {
		installKey = RandomUtils.generateRandom(20);
		return installKey;
	}

	public String getInstallKey() {
		return installKey;
	}

	public byte[] generateKey(String licenseId, String series, String installKey, byte[] b) {
		String key = licenseId + Constants.SEPARATOR + series + Constants.SEPARATOR + installKey;
		return ProtectionFactory.getEncrypt().encrypt(b, key.getBytes());
	}
}
