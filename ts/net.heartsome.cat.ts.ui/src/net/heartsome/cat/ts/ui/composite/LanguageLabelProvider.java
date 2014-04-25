/**
 * LanguageLabelProvider.java
 *
 * Version information :
 *
 * Date:Mar 21, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.composite;

import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.ts.ui.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * 语言相关的Viewer标签提供器
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class LanguageLabelProvider extends LabelProvider {	
	private Map<String, Image> imageCache = new HashMap<String, Image>();
	
	public Image getImage(Object element) {		
		if (element instanceof Language) {
			Language lang = (Language) element;
			String code = lang.getCode();
			String imagePath = lang.getImagePath();
			if (imagePath != null && !imagePath.equals("")) {				
				ImageDescriptor imageDesc = Activator.getImageDescriptor(imagePath);
				if (imageDesc != null) {
					ImageData data = imageDesc.getImageData().scaledTo(16, 12);
					Image image = new Image(Display.getDefault(), data);
					
					// 销毁原来的图片
					Image im = imageCache.put(code, image);
					if (im != null && !im.isDisposed()) {
						im.dispose();
					}
					return image;
				}
			}
		}
		return null;
	}
	
	public void dispose(){
		for (String code : imageCache.keySet()) {
			Image im = imageCache.get(code);
			if (im != null && !im.isDisposed()) {
				im.dispose();
			}
		}
		imageCache.clear();
		super.dispose();
	}
}
