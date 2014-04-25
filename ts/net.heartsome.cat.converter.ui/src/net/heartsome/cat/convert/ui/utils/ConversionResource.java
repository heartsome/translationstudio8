package net.heartsome.cat.convert.ui.utils;

import java.io.File;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.converter.Converter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * 本类用于管理转换的资源
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class ConversionResource {

	private String direction;

	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); // 工作空间的 root

	private IFile file; // 源文件

	private IProject project; // 所在项目

	private IPath projectRelativePath; // 项目路径

	/**
	 * 本类用于管理转换的资源
	 * @param direction
	 *            转换方向
	 * @param conversionItem
	 *            转换项目对象
	 * @throws CoreException
	 */
	public ConversionResource(String direction, IConversionItem conversionItem) throws CoreException {
		this(direction, conversionItem.getLocation());
	}

	/**
	 * 本类用于管理转换的资源
	 * @param direction
	 *            转换方向
	 * @param path
	 *            绝对路径
	 * @throws CoreException
	 */
	public ConversionResource(String direction, String path) throws CoreException {
		this(direction, new Path(path));
	}

	/**
	 * 本类用于管理转换的资源
	 * @param direction
	 *            转换方向
	 * @param location
	 *            绝对路径
	 * @throws CoreException
	 */
	public ConversionResource(String direction, IPath location) throws CoreException {
		this.direction = direction;
		file = root.getFileForLocation(location);
		if (file != null) {
			project = file.getProject();
			projectRelativePath = file.getProjectRelativePath();
			if (projectRelativePath.segmentCount() > 1) {
				projectRelativePath = projectRelativePath.removeFirstSegments(1); // 移除 project 下的第一级目录。
			}
		} else {
			throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, Messages.getString("utils.ConversionResource.msg1") + location.toOSString()));
		}
	}

	/**
	 * 得到源文件路径
	 * @return ;
	 */
	public String getSourcePath() {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) {
			return file.getFullPath().toOSString();
		} else {
			throw new RuntimeException(Messages.getString("utils.ConversionResource.msg2") + direction);
		}
	}

	/**
	 * 得到 XLIFF 文件路径
	 * @return ;
	 */
	public String getXliffPath() {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) { // 正向转换
			IPath projectPath = project.getFullPath(); // 得到项目路径
			IPath targetPath = projectRelativePath.addFileExtension(CommonFunction.R8XliffExtension); // 添加 hsxliff 后缀名
			// 放到 XLIFF 文件夹下
			String xliffFilePath = projectPath.append(Constant.FOLDER_XLIFF).append(targetPath).toOSString();
			return xliffFilePath;
		} else { // 反向转换
			return file.getFullPath().toOSString();
		}
	}

	/**
	 * 得到骨架文件路径
	 * @return ;
	 */
	public String getSkeletonPath() {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) { // 正向转换
			IPath projectPath = project.getFullPath(); // 得到项目路径
			IPath skeletonPath = projectRelativePath.addFileExtension("skl"); // 添加 skl 后缀名
			// 放到 SKL 文件夹下
			String skeletonFileFile = projectPath.append(Constant.FOLDER_INTERMEDDIATE).append(Constant.FOLDER_SKL).append(skeletonPath).toOSString();
			return skeletonFileFile;
		} else { // 反向转换
			throw new RuntimeException(Messages.getString("utils.ConversionResource.msg3") + direction);
		}
	}

	/**
	 * 得到目标文件路径
	 * @return ;
	 */
	public String getTargetPath() {
		return getTargetPath(Constant.FOLDER_TGT);
	}

	/**
	 * 得到目标文件路径
	 * @param tgtFolder
	 *            目标文件存放文件夹，默认值为{@link net.heartsome.cat.bean.Constant#FOLDER_TGT}，默认情况下建议使用 {@link #getTargetPath()}
	 * @return ;
	 */
	public String getTargetPath(String tgtFolder) {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) { // 正向转换
			throw new RuntimeException(Messages.getString("utils.ConversionResource.msg4") + direction);
		} else {
			IPath projectPath = project.getFullPath(); // 得到项目路径
			String fileExtension = projectRelativePath.getFileExtension();
			IPath targetPath;
			if (CommonFunction.R8XliffExtension.equalsIgnoreCase(fileExtension)) {
				targetPath = projectRelativePath.removeFileExtension(); // 去除 .hsxliff 后缀
			} else {
				targetPath = projectRelativePath;
			}
			// 放到 Target 文件夹下
			String targetFilePath = projectPath.append(tgtFolder).append(targetPath).toOSString();
			return targetFilePath;
		}
	}

	/**
	 * 得到目标文件路径
	 * @param tgtFolder
	 *            目标文件存放文件夹，默认值为{@link net.heartsome.cat.bean.Constant#FOLDER_TGT}，默认情况下建议使用 {@link #getTargetPath()}
	 * @return ;
	 */
	public String getPreviewPath() {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) { // 正向转换
			throw new RuntimeException(Messages.getString("utils.ConversionResource.msg4") + direction);
		} else {
			IPath projectPath = project.getFullPath(); // 得到项目路径
			String fileExtension = projectRelativePath.getFileExtension();
			IPath targetPath;
			if (CommonFunction.R8XliffExtension.equalsIgnoreCase(fileExtension)) {
				targetPath = projectRelativePath.removeFileExtension(); // 去除 .hsxliff 后缀
			} else {
				targetPath = projectRelativePath;
			}
			// 放到 Intermediate/other 文件夹下
			String targetFilePath = projectPath.append(Constant.FOLDER_INTERMEDDIATE)
					.append(Constant.FOLDER_OTHER).append(targetPath).toOSString();
			int index = targetFilePath.lastIndexOf(".");
			if (index > -1 && index > targetFilePath.lastIndexOf(File.separatorChar)) {
				targetFilePath = targetFilePath.substring(0, index) + "_Preview" + targetFilePath.substring(index);
			}
			return targetFilePath;
		}
	}

	public String getXliffDir() {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) { // 正向转换
			IPath projectPath = project.getFullPath();
			String xliffFilePath = projectPath.append(Constant.FOLDER_XLIFF).toOSString();
			return xliffFilePath;
		} else { // 反向转换
			return file.getFullPath().toOSString();
		}
	}
}
