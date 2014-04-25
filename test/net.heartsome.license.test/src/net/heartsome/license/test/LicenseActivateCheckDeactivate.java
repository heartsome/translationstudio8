package net.heartsome.license.test;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.webservice.ServiceUtilTest;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class LicenseActivateCheckDeactivate extends AbstractJavaSamplerClient {

	private SampleResult results;
	private String licenseID;
	private String hardwareCode;
	private String installCode;
	private int waitTimeMillis;
	private int intervalTimeMillis;
	private String strResult;
	private long startTime;
	private long pastTime;
	private int waitCount = 0;

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		results = new SampleResult();
		results.sampleStart();

		licenseID = context.getParameter("LicenseID");
		hardwareCode = context.getParameter("HardwareCode");
		installCode = context.getParameter("InstallCode");
		waitTimeMillis = Integer.valueOf(context.getParameter("WaitSeconds")) * 1000;
		intervalTimeMillis = Integer.valueOf(context
				.getParameter("IntervalSeconds")) * 1000;
		strResult = licenseID + "：\n";
		startTime = System.currentTimeMillis();

		// 检查许可证状态，应为未激活，然后激活
		check(NextAction.ACTIVATE, "不通过");

		// 等待两次测试之间的间隔时间
		wait(intervalTimeMillis);

		pastTime = System.currentTimeMillis() - startTime - waitTimeMillis
				* waitCount - intervalTimeMillis;

		results.setResponseMessage(strResult + "\n实际耗时（毫秒）：" + pastTime);
		results.sampleEnd();
		return results;
	}

	public Arguments getDefaultParameters() {
		Arguments args = new Arguments();
		args.addArgument("LicenseID", "89U1jiKrhD5IG1yNU0O2CinG");
		args.addArgument("HardwareCode", "TestHW");
		args.addArgument("InstallCode", "TestInstall");
		args.addArgument("WaitSeconds", "5");
		args.addArgument("IntervalSeconds", "30");
		return args;
	}

	/**
	 * 检查许可证状态，
	 * 
	 * @param na
	 *            检查后的下一步操作
	 * @param expected
	 *            预期检查结果
	 */
	private void check(NextAction na, String expected) {
		try {
			String result = ServiceUtilTest.check(licenseID, hardwareCode,
					installCode);
			if (result.equals("Check Success")) {
				strResult += ("预期" + expected + "，验证通过！\n");

				// 若下一步为取消激活，则当前应该是已激活
				if (na.equals(NextAction.DEACTIVATE)) {
					results.setSuccessful(true);
					wait(waitTimeMillis);
					deactivate();
					// 否则应该为未激活状态，非预期结果，故测试失败
				} else {
					results.setSuccessful(false);
				}

			} else {
				strResult += ("预期" + expected + "，验证未通过，返回结果为：" + result + "\n");

				// 下一步为激活，则当前应为未激活
				if (na.equals(NextAction.ACTIVATE)) {
					results.setSuccessful(true);
					wait(waitTimeMillis);
					activate();
					// 若测试结束，应已成功取消激活
				} else if (na.equals(NextAction.END)) {
					results.setSuccessful(true);
					results.setResponseCodeOK();
					// 否则应为已激活状态，非预期结果，故测试失败
				} else {
					results.setSuccessful(false);
				}
			}
		} catch (Exception e) {
			results.setSuccessful(false);
			strResult += "验证失败，异常信息：\n" + e.getMessage() + "\n";
		}
	}

	/**
	 * 激活许可证
	 */
	private void activate() {
		try {
			String result = ServiceUtilTest.active(licenseID, hardwareCode,
					installCode);
			if (result.equals("Active Success")) {
				strResult += "激活成功！\n";
				results.setSuccessful(true);

				// 等待指定时间
				wait(waitTimeMillis);

				// 检查许可证激活状态，并指定下一步操作为取消激活
				check(NextAction.DEACTIVATE, "通过");
			} else {
				results.setSuccessful(false);
				strResult = strResult + "激活失败，返回结果为：" + result + "\n";
			}

		} catch (Exception e) {
			// e.printStackTrace();
			results.setSuccessful(false);
			strResult = strResult + "激活失败，异常信息：\n" + e.getMessage() + "\n";
		}
	}

	/**
	 * 取消激活许可证
	 */
	private void deactivate() {
		try {
			String result = ServiceUtilTest.cancel(licenseID, hardwareCode,
					installCode);
			if (Constants.RETURN_LOGOUTSUCESS.equals(result)) {
				results.setSuccessful(true);
				strResult += "取消激活成功！\n";

				// 等待指定时间
				wait(waitTimeMillis);

				// 检查许可证激活状态，并指明测试结束
				check(NextAction.END, "不通过");
			} else {
				results.setSuccessful(false);
				strResult += "取消激活失败，返回结果为：" + result + "\n";
			}
		} catch (Exception e) {
			results.setSuccessful(false);
			strResult += "取消激活失败，异常信息：\n" + e.getMessage() + "\n";
		}
	}

	/**
	 * 等待指定毫秒数
	 * 
	 * @param waitTimeMillis
	 */
	private void wait(int waitTimeMillis) {
		if (waitTimeMillis != 0) {
			try {
				Thread.sleep(waitTimeMillis);
				waitCount++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author felix_lu 下一步操作：激活、取消激活、结束
	 */
	private enum NextAction {
		ACTIVATE, DEACTIVATE, END
	}
}
