package net.heartsome.license.generator;

public interface IKeyGenerator {
	byte[] generateKey(String licenseId, byte[] b) throws Exception;
	
	byte[] generateKey(String licenseId, String installKey, byte[] b) throws Exception;
	
	String getInstallKey();
}
