package net.heartsome.cat.converter.msexcel2007.document;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Spreadsheet的资源包<br>
 * 关闭Spreadsheet文档时，需要调用{@link #close()}方法，清理解压后的文件资源
 * @author Jason
 * @since jdk 1.5
 */
public class SpreadsheetPackage {

	/**Spread解压后的第一级目录	 */
	private String superRoot;

	/** 在包中获取文件时的当前目录 */
	private String currentRoot;

	/** 临时目录，用于记录标记 */
	private String tempRoot;

	/**
	 * 构造SpreadsheetPackage对象
	 * @param root
	 *            资源根路径，在此路径下，包含了Spreadsheet解压后的所有文件和正确的目录结构
	 * @throws FileNotFoundException
	 */
	public SpreadsheetPackage(String root) throws FileNotFoundException {
		superRoot = root;
		setCurrentRoot(root);
	}

	/**
	 * 设置当前根目录
	 * @param root
	 * @throws FileNotFoundException
	 *             ;
	 */
	public void setCurrentRoot(String root) throws FileNotFoundException {
		File f = new File(root);
		if (!f.exists()) {
			throw new FileNotFoundException();
		}
		this.currentRoot = root;
	}

	/**
	 * 获取包的根目录
	 * @return
	 */
	public String getPackageSuperRoot() {
		return this.superRoot;
	}

	/**
	 * 获取当前根目录的名称
	 * @return ;
	 */
	public String getRootName() {
		File f = new File(currentRoot);
		return f.getName();
	}

	/**
	 * 回到上一级目录，类似命令行中的cd ..命令，如果当前根目录已经是包的根目录，则不做任何操作
	 */
	public void back2TopLeve() {
		File f = new File(currentRoot);
		if (!f.getParent().equals(superRoot)) {
			try {
				setCurrentRoot(f.getParent());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取包中文件的路径<br>
	 * 如获取workbook.xml，则relativePath的值应该为xl/workbook.xml<br>
	 * 获取xl/workbook.xml后，{@link #currentRoot}/xl将作为根目录
	 * @param relativePath
	 * @return
	 * @throws FileNotFoundException
	 */
	public String getPackageFilePath(String relativePath) throws FileNotFoundException {
		if (relativePath.startsWith("..")) {
			back2TopLeve();
			relativePath = relativePath.substring(relativePath.indexOf('/') + 1, relativePath.length());
		}
		File ret = new File(currentRoot);
		String[] dirs = relativePath.split("/");
		if (!ret.exists()) {
			throw new FileNotFoundException(currentRoot);
		}
		if (dirs.length >= 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				ret = new File(ret, dirs[i]);
			}
			ret = new File(ret, dirs[dirs.length - 1]);
			if (!ret.exists()) {
				throw new FileNotFoundException(relativePath);
			}
		}
		setCurrentRoot(ret.getParent());
		return ret.getAbsolutePath();
	}

	/**
	 * 标记当前根目录位置,参考{@link #resetRoot()}
	 */
	public void markRoot() {
		tempRoot = this.currentRoot;
	}

	/**
	 * 重置当前根目录到标记位置,参考{@link #markRoot()}<br>
	 * 如果未调用{@link #markRoot()} 方法则不做任何操作
	 */
	public void resetRoot() {
		if (tempRoot != null && tempRoot.length() != 0) {
			this.currentRoot = tempRoot;
			tempRoot = "";
		}
	}

	/**
	 * 删除目录或者文件
	 * @param file
	 *            需要处理的文件或文件夹;
	 */
	private void deleteFileOrFolder(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteFileOrFolder(files[i]);
				}
			}
			file.delete();
		}
	}

	/**
	 * 关闭当前包，关闭时清理资源，删除当前包路径 ;
	 */
	public void close() {
		File f = new File(superRoot);
		this.deleteFileOrFolder(f);
	}
}
