package net.heartsome.cat.common.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.common.core.resource.Messages;

/**
 * 对文件进行操作的工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class FileManager {

	/**
	 * 创建压缩包
	 * @param baseDir
	 *            所要压缩的根目录（包含绝对路径）
	 * @param zos
	 * @param removeFolderPath
	 *            所排除的目录名或文件名（此目录为 baseDir 的子目录）
	 * @throws Exception
	 *             ;
	 */
	public void createZip(String baseDir, ZipOutputStream zos, String removeFolderPath) throws Exception {
		List<String> lstRemoveFolderPath = new ArrayList<String>();
		lstRemoveFolderPath.add(removeFolderPath);
		createZip(baseDir, zos, lstRemoveFolderPath);
	}

	/**
	 * 创建压缩包
	 * @param baseDir
	 *            所要压缩的根目录（包含绝对路径）
	 * @param zos
	 * @param lstRemoveFolderPath
	 *            所排除的目录名或文件名集合（目录为 baseDir 的子目录）
	 * @throws Exception
	 *             ;
	 */
	public void createZip(String baseDir, ZipOutputStream zos, List<String> lstRemoveFolderPath) throws Exception {
		if (zos == null) {
			return;
		}
		File folderObject = new File(baseDir);
		if (folderObject.exists()) {
			List<File> fileList = getSubFiles(new File(baseDir), lstRemoveFolderPath);
			ZipEntry ze = null;
			byte[] buf = new byte[1024];
			int readLen = 0;
			for (int i = 0; i < fileList.size(); i++) {
				File f = (File) fileList.get(i);

				// 创建一个ZipEntry，并设置Name和其它的一些属性
				ze = new ZipEntry(getAbsFileName(baseDir, f));
				ze.setSize(f.length());
				ze.setTime(f.lastModified());

				// 将ZipEntry加到zos中，再写入实际的文件内容
				zos.putNextEntry(ze);
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				while ((readLen = is.read(buf, 0, 1024)) != -1) {
					zos.write(buf, 0, readLen);
				}
				is.close();
			}
		} else {
			throw new Exception(MessageFormat.format(Messages.getString("file.FileManager.msg1"),
					folderObject.getAbsolutePath()));
		}
	}

	/**
	 * 将文件添加到压缩包中
	 * @param zos
	 * @param baseDir
	 *            压缩包目录名
	 * @param sourceFileName
	 *            压缩文件的绝对路径
	 * @throws Exception
	 *             ;
	 */
	public void addFileToZip(ZipOutputStream zos, String baseDir, String sourceFileName) throws Exception {
		if (zos == null) {
			return;
		}
		File sourceFile = new File(sourceFileName);
		byte[] buf = new byte[1024];
		ZipEntry ze = null;
		// 创建一个ZipEntry，并设置Name和其它的一些属性
		ze = new ZipEntry(getAbsFileName(baseDir, sourceFile));
		ze.setSize(sourceFile.length());
		ze.setTime(sourceFile.lastModified());
		// 将ZipEntry加到zos中，再写入实际的文件内容
		zos.putNextEntry(ze);
		InputStream is = new BufferedInputStream(new FileInputStream(sourceFile));
		int readLen = -1;
		while ((readLen = is.read(buf, 0, 1024)) != -1) {
			zos.write(buf, 0, readLen);
		}
		zos.flush();
		is.close();
		zos.closeEntry();
	}

	/**
	 * 对压缩包解压
	 * @param sourceZip
	 *            压缩包路径
	 * @param outFileName
	 *            解压路径
	 * @throws IOException
	 *             ;
	 */
	public void releaseZipToFile(String sourceZip, String outFileName) throws IOException {
		ZipFile zfile = new ZipFile(sourceZip);
		@SuppressWarnings("rawtypes")
		Enumeration zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		while (zList.hasMoreElements()) {
			// 从ZipFile中得到一个ZipEntry
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				continue;
			}
			// 以ZipEntry为参数得到一个InputStream，并写到OutputStream中
			OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(outFileName, ze.getName())));
			InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			int readLen = 0;
			while ((readLen = is.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			is.close();
			os.close();
		}
		zfile.close();
	}

	/**
	 * 取得指定目录下的所有文件列表，包括子目录.
	 * @param baseDir
	 *            File 指定的目录
	 * @param lstRemoveFolerPath
	 *            排除的目录名或文件名
	 * @return 包含java.io.File的List
	 */
	public List<File> getSubFiles(File baseDir, List<String> lstRemoveFolerPath) {
		List<File> ret = new ArrayList<File>();
		// File base=new File(baseDir);
		File[] tmp = baseDir.listFiles();
		String filePath;
		tmpLoop: for (int i = 0; i < tmp.length; i++) {
			filePath = tmp[i].getAbsolutePath();
			if (lstRemoveFolerPath != null && lstRemoveFolerPath.size() > 0) {
				for (String path : lstRemoveFolerPath) {
					if (filePath.startsWith(path)) {
						continue tmpLoop;
					}
				}
			}
			if (tmp[i].isFile()) {
				ret.add(tmp[i]);
			}
			if (tmp[i].isDirectory()) {
				ret.addAll(getSubFiles(tmp[i], lstRemoveFolerPath));
			}
		}
		return ret;
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 * @param baseDir
	 *            指定根目录
	 * @param absFileName
	 *            相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	private File getRealFileName(String baseDir, String absFileName) {
//		String[] dirs = absFileName.split("/");
//		File ret = new File(baseDir);
//		if (dirs.length > 1) {
//			for (int i = 0; i < dirs.length - 1; i++) {
//				ret = new File(ret, dirs[i]);
//			}
//		}
//		if (!ret.exists()) {
//			ret.mkdirs();
//		}
//		ret = new File(ret, dirs[dirs.length - 1]);
//		return ret;
		
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		if (!ret.exists()) {
			ret.mkdirs();
		}
		
		if ("/".equals(System.getProperty("file.separator"))) {
			for (int i = 0; i < dirs.length; i++) {
				dirs[i] = dirs[i].replace("\\", "/");
			}
		}
		
		if (dirs.length >= 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				ret = new File(ret, dirs[i]);
			}
			if (!ret.exists()) {
				ret.mkdirs();
			}
			ret = new File(ret, dirs[dirs.length - 1]);
			if(!ret.exists()){
				File p  = ret.getParentFile();
				if(!p.exists()){
					p.mkdirs();
				}
			}
		}
		return ret;
	}

	/**
	 * 给定根目录，返回另一个文件名的相对路径，用于zip文件中的路径.
	 * @param baseDir
	 *            java.lang.String 根目录
	 * @param realFileName
	 *            java.io.File 实际的文件名
	 * @return 相对文件名
	 */
	private String getAbsFileName(String baseDir, File realFileName) {
		File real = realFileName;
		File base = new File(baseDir);
		String ret = real.getName();
		while (true) {
			real = real.getParentFile();
			if (real == null)
				break;
			if (real.equals(base))
				break;
			else {
				ret = real.getName() + "/" + ret;
			}
		}
		return ret;
	}

	/**
	 * 删除文件，如果 file 代表的为目录，则删除此目录和目录下的所有文件
	 * @param file
	 */
	public void deleteFileOrFolder(File file) {
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
}
