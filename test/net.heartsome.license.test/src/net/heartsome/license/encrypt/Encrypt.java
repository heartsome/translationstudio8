package net.heartsome.license.encrypt;

public interface Encrypt {
	byte[] getPublicKey();

	byte[] getPrivateKey();

	byte[] encrypt(byte[] publicKeyArray, byte[] srcBytes) throws Exception;

	byte[] decrypt(byte[] privateKeyArray, byte[] srcBytes) throws Exception;
}
