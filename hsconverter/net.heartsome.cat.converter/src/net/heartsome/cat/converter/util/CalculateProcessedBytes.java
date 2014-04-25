package net.heartsome.cat.converter.util;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 在按行对文件进行处理的过程中，把文件的读取和处理统一模拟分成 100 个任务，根据当前已处理的字节数计算出当前文件的处理进度。如果文件的总数节数小于或等于 总任务量，则总任务数即为其总字节数。
 * @author cheney
 * @since JDK1.6
 */
public class CalculateProcessedBytes {
	/**
	 * 默认的模拟任务量
	 */
	public static final int DEFAULT_TOTAL_TASK = 100;
	private long totalSize;
	private int totalTask;
	// 每个任务的工作量（所需要处理的字节数）
	private long oneTaskSize;

	// 每次计算时，剩余的不足一个任务工作量的字节数
	private long remainSize;

	// TODO 在处理大文件时，可以考虑扩大模拟的任务量，以缩小每个任务所需要处理的字节量。

	/**
	 * @param totalSize
	 *            字节总数
	 * @param totalTask
	 *            总任务量
	 */
	public CalculateProcessedBytes(long totalSize, int totalTask) {
		initialize(totalSize, totalTask);
	}

	/**
	 * 初始化总字节数和总任务数
	 * @param totalSize
	 *            总字节数
	 * @param totalTask
	 *            总任务数 ;
	 */
	private void initialize(long totalSize, int totalTask) {
		if (totalSize < 0) {
			totalSize = 0;
		}
		if (totalTask < 0) {
			totalTask = 0;
		}
		this.totalSize = totalSize;
		this.totalTask = totalTask;
		if (totalSize <= totalTask) {
			this.totalTask = (int) totalSize;
		}
		oneTaskSize = this.totalSize / this.totalTask;
	}

	/**
	 * @param totalSize
	 *            字节总数
	 */
	public CalculateProcessedBytes(long totalSize) {
		this(totalSize, DEFAULT_TOTAL_TASK);
	}

	/**
	 * @param filePath
	 *            文件路径
	 */
	public CalculateProcessedBytes(String filePath) {
		// 计算总任务数
		File temp = new File(filePath);
		long totalSize = 0;
		if (temp.exists()) {
			totalSize = temp.length();
		}
		if (totalSize == 0) {
			totalSize = 1;
		}
		initialize(totalSize, DEFAULT_TOTAL_TASK);
	}

	/**
	 * @return 返回总任务量;
	 */
	public int getTotalTask() {
		return totalTask;
	}

	/**
	 * 根据所接收的字节数，计算这些字节数所占的任务量。在处理的过程中，需要把之前处理的字节数剩余量（即上一次处理的字节数中不足一个任务工作量的字节数）加进来一起进行计算。
	 * @param size
	 * @return ;
	 */
	public int calculateProcessed(long size) {
		remainSize += size;
		int tasks = (int) (remainSize / oneTaskSize);
		remainSize %= oneTaskSize;
		return tasks;
	}

	/**
	 * 计算当前的转换进度
	 * @param monitor
	 *            IProgressMonitor
	 * @param line
	 *            当前处理的行;
	 * @param encoding
	 *            所处理字符串的字符编码，允许为 NULL，如果为 NULL 则使用平台默认的编码
	 */
	public void calculateProcessed(IProgressMonitor monitor, String line, String encoding) {
		if (line != null) {
			int size = 0;
			try {
				if (encoding != null) {
					size = line.getBytes(encoding).length;
				} else {
					size = line.getBytes().length;
				}
				// 加上换行符所占的一个字节
				size += 1;
			} catch (UnsupportedEncodingException e) {
				// ignore the exception
				e.printStackTrace();
			}
			if (size > 0) {
				int workedTask = calculateProcessed(size);
				if (workedTask > 0) {
					monitor.worked(workedTask);
				}
			}
		}
	}
}
