package net.heartsome.cat.ts.ui.xliffeditor.nattable.painter;

import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * 获取状态图片的工具类
 * @author Leakey,weachy
 * @version
 * @since JDK1.5
 */
public class XliffEditorGUIHelper {

	private static final String FILE_SEPARATER = System.getProperty("file.separator");

	public enum ImageName {
		// APPROVE("approve"), DISAPPROVE("disapprove"), EDITNOTE("editnote"), REMOVENOTE("removenote"), FINAL("final"),
		// NEEDS_ADAPTATION(
		// "needs-adaptation"), NEEDS_L10N("needs-l10n"), NEEDS_REVIEW_ADAPTATION("needs-review-adaptation"),
		// NEEDS_REVIEW_L10N(
		// "needs-review-l10n"), NEEDS_REVIEW_TRANSLATION("needs-review-translation"), NEEDS_TRANSLATION(
		// "needs-translation"), NEW("new"), SIGNED_OFF("signed-off"), TRANSLATED("translated"), SPLITPOINT(
		// "splitPoint");
		APPROVE("approved"), TRANSLATED("translated"), HAS_NOTE("note"), DONT_ADDDB("not-sent-db"), HAS_QUESTION(
				"questioning"), DRAFT("draft"), LOCKED("locked"),SINGED_OFF("sign-off"),SPLITPOINT("cut-point"),EMPTY("not-translated");

		private final String value;

		private ImageName(String value) {
			this.value = value;
		}

		public static ImageName getItem(String value) {
			ImageName[] imageNames = values();
			for (ImageName imageName : imageNames) {
				if (imageName.value.equals(value)) {
					return imageName;
				}
			}
			return null;
		}
	}

	/** 存放图片的路径数组. */
	private static final String[] IMAGE_DIRS = new String[] { "images" + FILE_SEPARATER + "state"+FILE_SEPARATER };

	/** 图片的后缀数组. */
	private static final String[] IMAGE_EXTENSIONS = new String[] { ".png" };

	/** 所有图片的Map. */
	private static Map<ImageName, Image> images = new HashMap<ImageName, Image>();

	/**
	 * 得到图片
	 * @param ImageName
	 *            图片名
	 * @return 图片;
	 */
	public static Image getImage(ImageName imageName) {
		return images.get(imageName);
	}

	/**
	 * 根据给定的图片文件名（不带后缀）创建图片对象
	 * @param ImageName
	 *            图片名
	 * @return 图片;
	 */
	private static Image createImage(ImageName imageName) {
		ImageDescriptor imageDescriptor = getImageDescriptor(imageName.value);
		if (imageDescriptor != null) {
			return imageDescriptor.createImage();
		}
		return null;
	}

	/**
	 * 得到图片描述符
	 * @param imageName
	 *            图片名
	 * @return 图片描述符;
	 */
	private static ImageDescriptor getImageDescriptor(String imageName) {
		for (String dir : IMAGE_DIRS) {
			for (String ext : IMAGE_EXTENSIONS) {
				ImageDescriptor imageDescriptor = Activator.getImageDescriptor(dir + imageName + ext);
				if (imageDescriptor != null) {
					return imageDescriptor;
				}
			}
		}
		return null;
	}

	static { // 初始化
		ImageName[] imageNames = ImageName.values();
		for (ImageName imageName : imageNames) {
			images.put(imageName, createImage(imageName));
		}
	}

}
