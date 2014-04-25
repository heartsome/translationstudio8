package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.InstallKeyEncrypt;
import net.heartsome.license.generator.IKeyGenerator;
import net.heartsome.license.generator.KeyGeneratorImpl;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.utils.StringUtils;

public class OffLineActiveService {
	
	private String installKey;

	/**
	 * 脱机激活时选择授权文件对话框激活时的验证
	 * @param fileName 文件名
	 * @return ;
	 */
	public int activeByGrantFile(String fileName) {
		boolean result = FileUtils.writeFile(new byte[] {'1','1'}, ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		if (!result) {
			return Constants.EXCEPTION_INT8;
		} else {
			FileUtils.removeFile(ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		}
		
		byte[] key = FileUtils.readFile(fileName);
		if (key == null) {
			return Constants.EXCEPTION_INT1;
		}
		
		byte[] t = new LicenseReader().getLicenseInfo(key);
		if (t == null) {
			return Constants.EXCEPTION_INT2;
		}
		
		String[] arrInfo = getStrFromInfo(StringUtils.reverse(new String(t), 1, 5, 2));
		// 修改分隔符之后，旧版本的许可文件会导致异常，所以需要在这里判断一下，如果有异常，则会删掉原有文件，重新激活即可。
		if (arrInfo.length<3){
			return Constants.STATE_INVALID;
		}
		String strKeyCode = arrInfo[0];
		String strMacCode = arrInfo[1];
		String strInstallCode = arrInfo[2];
		
		LicenseIdValidator va = new LicenseIdValidator(strKeyCode);
		if (!va.checkLicense()) {
			return Constants.STATE_INVALID;
		}
		
		if (!va.checkEdition()) {
			return Constants.STATE_INVALID;
		}
		
		if (va.getType()) {
			return Constants.STATE_INVALID;
		}
		
		String curMacCode = ProtectionFactory.getSeries();
		if (!strMacCode.equals(curMacCode)) {
			return Constants.STATE_INVALID;
		}
		
		byte[] b = FileUtils.readFile(ProtectionFactory.getFileName(2, Constants.PRODUCTID));
		if (b == null) {
			return Constants.EXCEPTION_INT3;
		} else {
			b = InstallKeyEncrypt.decrypt(b);
			if (b == null) {
				return Constants.EXCEPTION_INT4;
			}
		}
		
		if (!StringUtils.reverse(new String(b), 1, 3, 2).equals(strInstallCode)) {
			return Constants.STATE_INVALID;
		}
		
		result = FileUtils.writeFile(key, ProtectionFactory.getFileName(1, Constants.PRODUCTID));
		if (!result) {
			return Constants.EXCEPTION_INT12;
		}
		
		System.getProperties().setProperty("TSState", "true");
		return Constants.STATE_VALID;
	}
	
	/**
	 * 生成安装码文件
	 * @return ;
	 */
	public int generateInstallFile() {
		boolean result = FileUtils.writeFile(new byte[] {'1','1'}, ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		if (!result) {
			return Constants.EXCEPTION_INT8;
		} else {
			FileUtils.removeFile(ProtectionFactory.getFileName(0, Constants.PRODUCTID));
		}
		
		IKeyGenerator gen = new KeyGeneratorImpl();
		installKey = gen.generateInstallKey();
		byte[] t = InstallKeyEncrypt.encrypt(StringUtils.handle(gen.getInstallKey(), 1, 3, 2).getBytes());
		if (t == null) {
			return Constants.EXCEPTION_INT10;
		}
		result = FileUtils.writeFile(t, ProtectionFactory.getFileName(2, Constants.PRODUCTID));
		if (!result) {
			return Constants.EXCEPTION_INT11;
		}
		
		return Constants.STATE_VALID;
	}
	
	public int readInstallFile() {
		byte[] b = FileUtils.readFile(ProtectionFactory.getFileName(2, Constants.PRODUCTID));
		if (b == null) {
			return Constants.EXCEPTION_INT3;
		} else {
			b = InstallKeyEncrypt.decrypt(b);
			if (b == null) {
				return Constants.EXCEPTION_INT4;
			}
		}
		
		installKey = StringUtils.reverse(new String(b), 1, 3, 2);
		
		return Constants.STATE_INVALID;
	}
	
	public String getInstallKey() {
		return installKey;
	}
	
	private String[] getStrFromInfo(String info) {
		return info.split(Constants.SEPARATOR);
	}
}
