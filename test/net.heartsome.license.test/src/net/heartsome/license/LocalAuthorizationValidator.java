package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.InstallKeyEncrypt;
import net.heartsome.license.utils.FileUtils;

public class LocalAuthorizationValidator {
	
	private boolean isTrial = false;
	private String licenseId = "";
	private String s;
	private byte[] b;
	
	public int checkLicense() {
		LicenseReader reader = new LicenseReader(Constants.PRODUCTID);
		s = reader.getLicenseInfo();
		if (s == null) {
			return Constants.STATE_INVALID;
		}
		
		String[] arrInfo = getStrFromInfo(s);
		// 修改分隔符之后，旧版本的许可文件会导致异常，所以需要在这里判断一下，如果有异常，则会删掉原有文件，重新激活即可。
		if (arrInfo.length<3){
			return Constants.STATE_INVALID;
		}
		String strKeyCode = arrInfo[0];
		String strMacCode = arrInfo[1];
		String strInstallCode = arrInfo[2];
		
		LicenseIdValidator va = new LicenseIdValidator();
		if (!va.checkLicense(strKeyCode)) {
			return Constants.STATE_INVALID;
		}
		
		licenseId = strKeyCode;
		isTrial = va.getType();
		
		b = FileUtils.readFile(ProtectionFactory.getFileName(2, Constants.PRODUCTID));
		if (b == null) {
			return Constants.STATE_INVALID;
		}
		try {
			b = InstallKeyEncrypt.decrypt(b);
		} catch (Exception e) {
			e.printStackTrace();
			return Constants.STATE_INVALID;
		}
		if (!strInstallCode.equals(new String(b))) {
			return Constants.STATE_INVALID;
		}
		
		if (!compareMacCode(ProtectionFactory.getSeries(), strMacCode)) {
			return Constants.STATE_INVALID;
		}
		
		return Constants.STATE_VALID;
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
	
	public String getLicenseInfo() {
		return s;
	}
	
	public byte[] getInstall() {
		return b;
	}
}
