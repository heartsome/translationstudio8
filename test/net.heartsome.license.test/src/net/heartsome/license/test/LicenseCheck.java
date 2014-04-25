package net.heartsome.license.test;

import net.heartsome.license.webservice.ServiceUtilTest;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class LicenseCheck extends AbstractJavaSamplerClient {

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.sampleStart();

		String licenseID = context.getParameter("LicenseID");
		String hardwareCode = context.getParameter("HardwareCode");
		String installCode = context.getParameter("InstallCode");
		int intervalTime = Integer.valueOf(context
				.getParameter("IntervalSeconds")) * 1000;
		String strResult = licenseID + "：\n";
		long startTime = System.currentTimeMillis();

		// 验证许可证的激活状态
		try {
			String result = ServiceUtilTest.check(licenseID, hardwareCode,
					installCode);
			if (result.equals("Check Success")) {
				results.setSuccessful(true);
				results.setResponseCodeOK();
				strResult += "验证通过！\n";
			} else {
				results.setSuccessful(true);
				results.setResponseCodeOK();
				strResult += "验证未通过，返回结果为：" + result + "\n";
			}
		} catch (Exception e) {
			results.setSuccessful(false);
			strResult += "验证失败，异常信息：\n" + e.getMessage() + "\n";
		}

		// 等待两次测试之间的间隔
		if (intervalTime != 0) {
			try {
				Thread.sleep(intervalTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		long pastTime = System.currentTimeMillis() - startTime - intervalTime;

		results.setResponseMessage(strResult + "\n实际耗时（毫秒）：" + pastTime);
		results.sampleEnd();
		return results;
	}

	public Arguments getDefaultParameters() {
		Arguments args = new Arguments();
		args.addArgument("LicenseID", "89U1jiKrhD5IG1yNU0O2CinG");
		args.addArgument("HardwareCode", "TestHW");
		args.addArgument("InstallCode", "TestInstall");
		args.addArgument("IntervalSeconds", "30");
		return args;
	}
}
