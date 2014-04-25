package net.heartsome.cat.common.file;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一个抽象的文件处理类。实现了文件处理接口中的创建文件历史访问列表方法。
 * 
 * @author John Zhu
 * @see net.heartsome.cat.common.file.FileHandler
 */
public abstract class AbstractFileHandler {
	/**
	 * 打开一个指定文件名称的文件。
	 * @param filename 要打开的文件名。
	 * @return
	 * key = result，value 为 String 类型, 返回打开文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> openFile(String filename);
	
	/**
	 * 打开一个指定文件名称的文件。
	 * @param filename 要打开的文件名。
	 * @param tuCount 已统计的翻译单元数目，用于限制缓存大小。默认为 0。
	 * @return
	 * key = result，value 为 String 类型, 返回打开文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	protected abstract Map<String,Object> openFile(String filename,int tuCount);
	
	/**
	 * 打开一个指定文件实例的文件。
	 * @param file 要打开的文件实例。
	 * @return
	 * key = result，value 为 String 类型, 返回打开文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> openFile(File file);
	
	/**
	 * 打开一个指定文件实例的文件。
	 * @param file 要打开的文件实例。
	 * @param tuCount 已统计的翻译单元数目，用于限制缓存大小。默认为 0。
	 * @return
	 * key = result，value 为 String 类型, 返回打开文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	protected abstract Map<String,Object> openFile(File file, int tuCount);
	
	/**
	 * 打开一组指定文件名称的文件。
	 * @param files 要打开的一组文件名。
	 * @return
	 * key = result，value 为 String 类型, 返回打开文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> openFiles(List<String> files);
	
	/**
	 * 关闭指定文件名称的文件。
	 * @param filename 要关闭的文件名。
	 * @return
	 * key = result，value 为 String 类型, 返回关闭文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> closeFile(String filename);
	
	/**
	 * 关闭指定文件实例的文件。
	 * @param file 要关闭的文件实例。
	 * @return
	 * key = result，value 为 String 类型, 返回关闭文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> closeFile(File file);
	
	/**
	 * 关闭一组指定文件名称的文件。
	 * @param files 要关闭的一组文件名。
	 * @return 
	 * key = result，value 为 String 类型, 返回关闭文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> closeFiles(List<String> files);
	
	/**
	 * 保存指定的源文件为目标文件。
	 * @param srcFile 源文件。<br/>
	 * @param tgtFile 目标文件。
	 * @return 
	 * key = result，value 为 String 类型, 返回保存文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> saveFile(String srcFile,String tgtFile);
	
	/**
	 * 保存指定的源文件为目标文件。
	 * @param srcFile 源文件实例。<br/>
	 * @param tgtFile 目标文件实例。
	 * @return
	 * key = result，value 为 String 类型, 返回保存文件的结果。 1 成功， 0 失败。<br/>
	 * key = errorMsg，value 为 String 类型，返回错误消息。<br/>
	 * key = exception，value 为 Exception 类型，返回具体的异常实例。
	 * */
	public abstract Map<String,Object> saveFile(File srcFile,File tgtFile);
	
	/**
	 * 创建文件历史访问列表。
	 * @param initSize 容器初始化大小。
	 * @param maxSize 容器最大大小。
	 * @return 返回一个同步的有序的文件历史访问列表容器。<br/>
	 * key 	 为历史访问文件路径。<br/>
	 * value 为历史访问文件关闭时焦点所在的文本段或是术语的索引。
	 * */
	public Map<String, String> createFileHistory(final int initSize,
			final int maxSize) {
		return Collections.synchronizedMap(new LinkedHashMap<String, String>(
				initSize, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("rawtypes")
			public boolean removeEldestEntry(Map.Entry entry) {
				return size() > maxSize;
			}

		});
	}

}
