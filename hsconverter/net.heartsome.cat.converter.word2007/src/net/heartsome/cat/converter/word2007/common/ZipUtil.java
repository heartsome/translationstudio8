package net.heartsome.cat.converter.word2007.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	/**
	 * 压缩文件夹
	 * @param zipPath
	 *            生成的zip文件路径
	 * @param filePath
	 *            需要压缩的文件夹路径
	 * @throws Exception
	 */
	public static void zipFolder(String zipPath, String filePath) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipPath));
		File f = new File(filePath);
		zipFiles(out, f, "");
		out.close();
	}

	/**
	 * 将压缩文件中的内容解压到指定目录中<br>
	 * 如果<code>baseDir</code>的值为空，则将文件解压到相同的目录中，目录名称为"zipFile_files"
	 * @param zipFile
	 *            压缩文件路径
	 * @param baseDir
	 *            解压的目标路径,可以为null
	 * @throws IOException
	 */
	public static String upZipFile(String zipFile, String baseDir) throws IOException {
		File f = new File(zipFile);
		if (baseDir == null) {
			baseDir = f.getPath() + "_files";
		}
		ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		ZipEntry ze;
		byte[] buf = new byte[1024];
		while ((ze = zis.getNextEntry()) != null) {
			File outFile = getRealFileName(baseDir, ze.getName());
			FileOutputStream os = new FileOutputStream(outFile);
			int readLen = 0;
			while ((readLen = zis.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			os.close();
		}
		zis.close();
		return baseDir;
	}

	/**
	 * 递归调用，压缩文件夹和子文件夹的所有文件
	 * @param out
	 * @param f
	 * @param base
	 * @throws Exception
	 */
	private static void zipFiles(ZipOutputStream out, File f, String base) throws IOException {
		if (f.isDirectory()) {
			File[] fl = f.listFiles();
			// out.putNextEntry(new ZipEntry(base + "/"));
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < fl.length; i++) {
				zipFiles(out, fl[i], base + fl[i].getName());// 递归压缩子文件夹
			}
		} else {
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(f);
			int b;
			// System.out.println(base);
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			in.close();
		}
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 * @param baseDir
	 *            指定根目录
	 * @param absFileName
	 *            相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	private static File getRealFileName(String baseDir, String absFileName) {
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
}
