package net.heartsome.license.encrypt;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class EncryptRSA implements Encrypt {

	// 非对称加密密钥算法
	private static final String algorithm = "RSA";
	// 密钥长度，用来初始化
	private static final int key_size = 1024;
	// 公钥
	private final byte[] publicKey;
	// 私钥
	private final byte[] privateKey;

	public EncryptRSA() {
		SecureRandom sr = new SecureRandom();

		KeyPairGenerator kpg = null;
		try {
			kpg = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		kpg.initialize(key_size, sr);

		KeyPair kp = kpg.generateKeyPair();
		Key keyPublic = kp.getPublic();
		publicKey = keyPublic.getEncoded();

		Key keyPrivate = kp.getPrivate();
		privateKey = keyPrivate.getEncoded();
	}

	/**
	 * 加密
	 * 
	 * @param publicKeyArray
	 * @param srcBytes
	 * @return
	 */
	public byte[] encrypt(byte[] publicKeyArray, byte[] srcBytes) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyArray);
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PublicKey keyPublic = kf.generatePublic(keySpec);
	
			Cipher cipher;
			cipher = Cipher.getInstance(algorithm,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
			cipher.init(Cipher.ENCRYPT_MODE, keyPublic);
			int blockSize = cipher.getBlockSize();
			int outputSize = cipher.getOutputSize(srcBytes.length);
			int leavedSize = srcBytes.length % blockSize;
			int blocksSize = leavedSize != 0 ? srcBytes.length / blockSize + 1
					: srcBytes.length / blockSize;
			byte[] raw = new byte[outputSize * blocksSize];
			int i = 0;
			while (srcBytes.length - i * blockSize > 0) {
				if (srcBytes.length - i * blockSize > blockSize)
					cipher.doFinal(srcBytes, i * blockSize, blockSize, raw, i
							* outputSize);
				else
					cipher.doFinal(srcBytes, i * blockSize, srcBytes.length - i
							* blockSize, raw, i * outputSize);
				i++;
			}
			return raw;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解密
	 * 
	 * @param privateKeyArray
	 * @param srcBytes
	 * @return
	 */
	public byte[] decrypt(byte[] privateKeyArray, byte[] srcBytes) {
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyArray);
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PrivateKey keyPrivate = kf.generatePrivate(keySpec);
	
			Cipher cipher = Cipher.getInstance(algorithm,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
			cipher.init(Cipher.DECRYPT_MODE, keyPrivate);
	
			int blockSize = cipher.getBlockSize();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(blockSize);
			int j = 0;
			while (srcBytes.length - j * blockSize > 0) {
				byte[] temp = cipher.doFinal(srcBytes, j * blockSize, blockSize);
				bout.write(temp);
				j++;
			}
			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}
}
