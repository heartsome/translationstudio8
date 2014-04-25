/**
 * TxtRowReader.java
 *
 * Version information :
 *
 * Date:2013-12-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document.txt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This instance must call close method
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TxtRowReader {
	private Logger LOGGER = LoggerFactory.getLogger(TxtRowReader.class);
	private BufferedReader reader;
	private String encoding;
	private File txtFile;
	private int lineNumber;

	/**
	 * This instance must call close method
	 * @param txtFile
	 * @param fileEncoding
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public TxtRowReader(File txtFile, String fileEncoding) throws FileNotFoundException, IOException {
		if (txtFile == null || !txtFile.exists()) {
			throw new FileNotFoundException();
		}
		this.encoding = fileEncoding;
		this.txtFile = txtFile;
		lineNumber = computeLineNumber();
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));
	}

	public List<String[]> read(int size) throws IOException {
		if (size < 1) {
			return null;
		}
		List<String[]> result = new ArrayList<String[]>();
		for (int i = 0; i < size; i++) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			String[] r = line.split("\\t");
			result.add(r);
		}
		if (result.size() == 0) {
			return null;
		}
		return result;
	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}
	}

	public int getLineNumber() {
		return lineNumber;
	}

	private int computeLineNumber() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));
		int ch = -1;
		int count = 0;
		boolean isline = false;
		try {
			while ((ch = reader.read()) != -1) {
				if (ch == '\r' || ch == '\n') {
					if (!isline) {
						isline = true;
						count++;
					}
				} else {
					isline = false;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}
		return count;
	}
}
