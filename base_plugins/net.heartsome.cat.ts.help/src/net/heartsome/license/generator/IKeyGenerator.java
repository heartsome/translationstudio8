package net.heartsome.license.generator;

public interface IKeyGenerator {
	byte[] generateKey(String licenseId, String series, byte[] b);
	
	byte[] generateKey(String licenseId, String series, String installKey, byte[] b);
	
	String getInstallKey();
	
	String generateInstallKey();
}
