package net.heartsome.license;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MacosxSeries implements SeriesInterface {

	@Override
	public String getSeries() {
		try {
			String[] commands = new String[] {"/bin/bash", "-c", "ioreg -rd1 -c IOPlatformExpertDevice | " +
			"awk '/IOPlatformSerialNumber/ { split($0, line, \"\\\"\"); printf(\"%s\\n\", line[4]); }'"};
			Process process = Runtime.getRuntime().exec(commands);
			
			ReadThread inputReadThread = new ReadThread(process.getInputStream());
			inputReadThread.start();
			
			//确保标准与错误流都读完时才向外界返回执行结果
			while (true) {
				if (inputReadThread.flag) {
					break;
				} else {
					Thread.sleep(1000);
				}
			}
			String series = inputReadThread.getResult();
			return "".equals(series) ? null : series;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 标准流与错误流读取线程
	 */
	private static class ReadThread extends Thread {
		private InputStream is;

		private ArrayList<Byte> result = new ArrayList<Byte>();

		public boolean flag;// 流是否读取完毕

		public ReadThread(InputStream is) {
			this.is = is;
		}

		// 获取命令执行后输出信息，如果没有则返回空""字符串
		protected String getResult() {
			byte[] byteArr = new byte[result.size()];
			for (int i = 0; i < result.size(); i++) {
				byteArr[i] = ((Byte) result.get(i)).byteValue();
			}
			return new String(byteArr);
		}

		public void run() {
			try {
				int readInt = is.read();
				while (readInt != -1) {
					result.add(Byte.valueOf(String.valueOf((byte) readInt)));
					readInt = is.read();
				}

				flag = true;// 流已读完
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
