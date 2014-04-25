package net.heartsome.license.generator;

import net.heartsome.license.ProtectionFactory;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.Encrypt;
import net.heartsome.license.encrypt.EncryptRSA;
import net.heartsome.license.utils.RandomUtils;

public class KeyGeneratorImpl implements IKeyGenerator {
	
	private static Encrypt en;
	private String installKey;
	
	public KeyGeneratorImpl() {
		try {
			en = new EncryptRSA();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String generateActiveKey(String licenseId) {
		return licenseId;
	}

	@Override
	public byte[] generateKey(String licenseId, byte[] b) throws Exception {
		String key = generateActiveKey(licenseId) + Constants.SEPARATOR + ProtectionFactory.getSeries() 
				+ Constants.SEPARATOR + generateInstallKey();
//		System.out.println("原文：" + key);
		return en.encrypt(b, key.getBytes());
	}

	private String generateInstallKey() {
		installKey = RandomUtils.generateRandom(20);
		return installKey;
	}
	
	public static void main(String[] argv) {
		KeyGeneratorImpl impl = new KeyGeneratorImpl();
		try {
			impl.generateKey("111", en.getPublicKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getInstallKey() {
		return installKey;
	}

	@Override
	public byte[] generateKey(String licenseId, String installKey, byte[] b)
			throws Exception {
		String key = licenseId + Constants.SEPARATOR + ProtectionFactory.getSeries() 
				+ Constants.SEPARATOR + installKey;
		return en.encrypt(b, key.getBytes());
	}
}
