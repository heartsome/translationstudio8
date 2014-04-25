package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.InstallKeyEncrypt;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.utils.StringUtils;

public class LocalAuthorizationValidator {
	
	private boolean isTrial = false;
	private String licenseId = "";
	private String macCode = null;

	private byte[] b;
	
	public int checkLicense() {
		byte[] t = FileUtils.readFile(ProtectionFactory.getFileName(1, Constants.PRODUCTID));
		if (t == null) {
			return Constants.EXCEPTION_INT1;
		}
		
		t = new LicenseReader().getLicenseInfo(t);
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
		
		licenseId = strKeyCode;
		
		if (!va.checkEdition()) {
			return Constants.EXCEPTION_INT14;
		}
		
		isTrial = va.getType();
		
		b = FileUtils.readFile(ProtectionFactory.getFileName(2, Constants.PRODUCTID));
		if (b == null) {
			return Constants.EXCEPTION_INT3;
		} else {
			b = InstallKeyEncrypt.decrypt(b);
			if (b == null) {
				return Constants.EXCEPTION_INT4;
			}
		}
		
		if (!strInstallCode.equals(StringUtils.reverse(new String(b), 1, 3, 2))) {
			return Constants.STATE_INVALID;
		}
		
		String curMacCode = ProtectionFactory.getSeries();
		if (!isExsitMac(curMacCode)) {
			return Constants.EXCEPTION_INT6;
		}
		
		if (!compareMacCode(curMacCode, strMacCode)) {
			return Constants.EXCEPTION_INT15;
		}
		
		macCode = curMacCode;
		
		System.getProperties().setProperty("TSState", "true");
		return Constants.STATE_VALID;
	}
	
	private boolean isExsitMac(String curMacCode) {
		if (curMacCode != null && !"".equals(curMacCode)) {
			String[] str = curMacCode.split("%");
			if (str.length == 1) {
				if (str[0] != null && !"".equals(str[0])) {
					return true;
				}
			} else if (str.length == 3) {
				if (str[2] != null && !"".equals(str[2])) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private String[] getStrFromInfo(String info) {
		return info.split(Constants.SEPARATOR);
	}
	
	/**
	 * 比较两个硬件指纹是否可以认定为同一台机器的方法
	 * 
	 * @param curMacCode
	 * @param parmMacCode
	 * @return
	 */
	private boolean compareMacCode(String curMacCode, String parmMacCode) {
		if (curMacCode == null) {
			return false;
		}
		if (curMacCode.equals(parmMacCode)) {
			return true;
		} else {
			String[] str1 = curMacCode.split("%");
			String[] str2 = parmMacCode.split("%");
			
			if (str1.length == str2.length) {
				if (str1.length == 1) {
					String[] str3 = curMacCode.split("[+]");
					String[] str4 = parmMacCode.split("[+]");
					for (int i = 0; i < str3.length; i++) {
						for (int j = 0; j < str4.length; j++) {
							if (str3[i].equals(str4[j])) {
								return true;
							}
						}
					}
				} else if (str1.length == 3) {
					if (str1[0].equals(str2[0]) && str1[1].equals(str2[1])) {
						String[] str3 = str1[2].split("[+]");
						String[] str4 = str2[2].split("[+]");
						for (int i = 0; i < str3.length; i++) {
							for (int j = 0; j < str4.length; j++) {
								if (str3[i].equals(str4[j])) {
									return true;
								}
							}
						}
					} 
				} 
			} 
		}
		return false;
	}
	
	public boolean isTrial() {
		return isTrial;
	}
	
	public String getLicenseId() {
		return licenseId;
	}
	
	public byte[] getInstall() {
		return b;
	}
	
	public String getMacCode() {
		return macCode;
	}
}
