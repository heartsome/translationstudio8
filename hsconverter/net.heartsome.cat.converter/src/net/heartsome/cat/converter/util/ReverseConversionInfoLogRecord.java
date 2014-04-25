package net.heartsome.cat.converter.util;

import java.io.File;

import net.heartsome.cat.converter.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在逆转换过程中，记录日志的帮助类
 * @author cheney
 * @since JDK1.6
 */
public class ReverseConversionInfoLogRecord {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReverseConversionInfoLogRecord.class);

	// 文件转换的开始时间
	private long startTime;

	// 转换过程中，某一转换阶段的开始时间
	private long tempStartTime;

	// 转换过程中，某一转换阶段的结束时间
	private long tempEndTime;

	// 文件转换的结束时间
	private long endTime;

	/**
	 * 记录转换开始的相关信息 ;
	 */
	public void startConversion() {
		if (LOGGER.isInfoEnabled()) {
			startTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger1"), startTime);
		}
	}

	/**
	 * 记录转换过程中涉及的相关文件信息
	 * @param catalogueFile
	 *            catalogue 文件路径
	 * @param iniFile
	 *            ini 文件路径
	 * @param xliffFile
	 *            xliff 文件路径
	 * @param skeletonFile
	 *            骨架文件路径 ;
	 */
	public void logConversionFileInfo(String catalogueFile, String iniFile, String xliffFile, String skeletonFile) {
		if (LOGGER.isInfoEnabled()) {
			long fileSize = 0;
			if (catalogueFile != null) {
				File temp = new File(catalogueFile);
				if (temp.exists()) {
					fileSize = temp.length();
					LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger2"), fileSize);
				}
			}

			fileSize = 0;
			if (iniFile != null) {
				File temp = new File(iniFile);
				if (temp.exists()) {
					fileSize = temp.length();
					LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger3"), fileSize);
				}
			}

			fileSize = 0;
			if (xliffFile != null) {
				File temp = new File(xliffFile);
				if (temp.exists()) {
					fileSize = temp.length();
					LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger4"), fileSize);
				}
			}

			fileSize = 0;
			if (skeletonFile != null) {
				File temp = new File(skeletonFile);
				if (temp.exists()) {
					fileSize = temp.length();
					LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger5"), fileSize);
				}
			}
		}
	}

	/**
	 * 记录开始加载 catalogue 文件的相关信息 ;
	 */
	public void startLoadingCatalogueFile() {
		if (LOGGER.isInfoEnabled()) {
			tempStartTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger6"), tempStartTime);
		}
	}

	/**
	 * 记录加载完 catalogue 文件的相关信息 ;
	 */
	public void endLoadingCatalogueFile() {
		if (LOGGER.isInfoEnabled()) {
			tempEndTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger7"), tempEndTime);
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger8"), tempEndTime - tempStartTime);
		}
	}

	/**
	 * 记录开始加载 ini 文件的相关信息 ;
	 */
	public void startLoadingIniFile() {
		if (LOGGER.isInfoEnabled()) {
			tempStartTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger9"), tempStartTime);
		}
	}

	/**
	 * 记录加载完 ini 文件的相关信息 ;
	 */
	public void endLoadingIniFile() {
		if (LOGGER.isInfoEnabled()) {
			tempEndTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger10"), tempEndTime);
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger11"), tempEndTime - tempStartTime);
		}
	}

	/**
	 * 记录开始加载 xliff 文件的相关信息 ;
	 */
	public void startLoadingXliffFile() {
		if (LOGGER.isInfoEnabled()) {
			tempStartTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger12"), tempStartTime);
		}
	}

	/**
	 * 记录加载完 xliff 文件的相关信息 ;
	 */
	public void endLoadingXliffFile() {
		if (LOGGER.isInfoEnabled()) {
			tempEndTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger13"), tempEndTime);
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger14"), tempEndTime - tempStartTime);
		}
	}

	/**
	 * 记录开始替换 skeleton 文件中的 segment 标志符相关信息 ;
	 */
	public void startReplacingSegmentSymbol() {
		if (LOGGER.isInfoEnabled()) {
			tempStartTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger15"), tempStartTime);
		}
	}

	/**
	 * 记录替换完 skeleton 文件中的 segment 标志符相关信息 ;
	 */
	public void endReplacingSegmentSymbol() {
		if (LOGGER.isInfoEnabled()) {
			tempEndTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger16"), tempEndTime);
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger17"), tempEndTime - tempStartTime);
		}
	}

	/**
	 * 记录转换完成相关信息 ;
	 */
	public void endConversion() {
		if (LOGGER.isInfoEnabled()) {
			endTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger18"), endTime);
			LOGGER.info(Messages.getString("util.ReverseConversionInfoLogRecord.logger19"), endTime - startTime);
		}
	}
}
