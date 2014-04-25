package net.heartsome.cat.converter.word2007.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;

import net.heartsome.cat.converter.word2007.resource.Messages;

/**
 * word 2007 的资源包<br>
 * 以最后一次处理前文件所在的目录做为根
 * @author robert	2012-08-08 
 */
public class PathUtil {
	/** word 2007 文件解压后的根目录，即根文件夹 */
	private String superRoot;
	/** 当前所处在的目录 */
	private String root;
	/** 保存的临时路径 */
	private String tempRoot;
	/** word 文件夹的路径 */
	private String wordRoot;
	/** interTag.xml 的位置*/
	private String interTagPath;
	
	
	
	/**
	 * 构造SpreadsheetPackage对象
	 * @param root
	 *            资源根路径，在此路径下，包含了Spreadsheet解压后的所有文件和正确的目录结构
	 * @throws Exception 
	 */
	public PathUtil(String superRoot) throws Exception {
		this.superRoot = superRoot;
		setRoot(superRoot); 
		wordRoot = superRoot + System.getProperty("file.separator") + PathConstant.WORD_FOLDER;
		interTagPath = superRoot + System.getProperty("file.separator") + PathConstant.interTag_FILE;
		if (!new File(wordRoot).exists() && !new File(wordRoot).isDirectory()) {
			throw new Exception(MessageFormat.format(Messages.getString("PathUtil.msg1"), PathConstant.WORD_FOLDER));
		}
	}

	/**
	 * 设置当前根路径
	 * @param root
	 * @throws FileNotFoundException
	 *             ;
	 */
	public void setRoot(String root) throws FileNotFoundException {
		File f = new File(root);
		if (!f.exists()) {
			throw new FileNotFoundException(root);
		}
		this.root = root;
	}
	
	/**
	 * 将当前目标设置成根目录
	 * @throws Exception
	 */
	public void setSuperRoot() throws Exception {
		setRoot(superRoot); 
	}

	/**
	 * 获取当前根路径
	 * @return
	 */
	public String getPackageRoot() {
		return this.root;
	}

	/**
	 * 获取当前根路径的名称
	 * @return ;
	 */
	public String getRootName() {
		File f = new File(root);
		return f.getName();
	}

	/**
	 * 回到上一级目录，类似cd ..命令
	 */
	public void back2TopLeve() {
		File f = new File(root);
		if (!f.getParent().equals(superRoot)) {
			try {
				setRoot(f.getParent());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取Spreadsheet包中的文件路径<br>
	 * 如获取workbook.xml，则relativePath的值应该为xl/workbook.xml<br>
	 * 获取xl/workbook.xml后，{@link #root}/xl将作为根目录
	 * @param relativePath
	 * @param isSetRoot 是否将当前文件的文件夹设置成当前目录
	 * @return
	 * @throws FileNotFoundException
	 */
	public String getPackageFilePath(String relativePath, boolean isSetRoot) throws FileNotFoundException {
		if (relativePath.startsWith("..")) {
			back2TopLeve();
			relativePath = relativePath.substring(relativePath.indexOf('/') + 1, relativePath.length());
		}
		File ret = new File(root);
		String[] dirs = relativePath.split("/");
		if (!ret.exists()) {
			throw new FileNotFoundException(root);
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
		if (isSetRoot) {
			setRoot(ret.getParent());
		}
		return ret.getAbsolutePath();
	}
	

	/**
	 * 标记当前Root位置,参考{@link #resetRoot()}
	 */
	public void markRoot() {
		tempRoot = this.root;
	}

	/**
	 * 重置当前Root到标记位置,参考{@link #markRoot()}
	 */
	public void resetRoot() {
		if (tempRoot != null && tempRoot.length() != 0) {
			this.root = tempRoot;
			tempRoot = "";
		}
	}
	
	/**
	 * 将当前目录设置成 word 目录
	 */
	public void setWordRoot(){
		root = wordRoot;
	}

	public String getSuperRoot() {
		return superRoot;
	}

	public String getInterTagPath() {
		return interTagPath;
	}
	
	
}
