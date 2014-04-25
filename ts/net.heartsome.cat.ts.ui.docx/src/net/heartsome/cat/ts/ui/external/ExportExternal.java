package net.heartsome.cat.ts.ui.external;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.heartsome.cat.ts.ui.docx.Activator;
import net.heartsome.cat.ts.ui.docx.resource.Messages;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 导出外部文件，常量、工具。
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class ExportExternal {

	public static final String NAMESPACE_HS = "http://www.heartsome.net.cn/2008/XLFExtension";
	public static final String NAMESPACE_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	
	public static final int EXPORT_HSPROOF = 0;
	public static final int EXPORT_SDLUNCLEAN = 1;
	public static final int EXPORT_TMX = 2;
	public static final int EXPORT_SDLXLIFF = 3;

	public static final int CONFIRM_YES = 0;
	public static final int CONFIRM_YESTOALL = 1;
	public static final int CONFIRM_NO = 2;
	public static final int CONFIRM_NOTOALL = 3;
	
	
	public static final String TEMPLATE_DOCX = "temple/temple.docx";
	
	private static LinkedHashMap<Integer, String> exportSets = new LinkedHashMap<Integer, String>();

	private static final VTDGen vg = new VTDGen();
	
	static {
		exportSets.put(EXPORT_HSPROOF, Messages.getString("ExportDocxDialog.lable.exporttype.hsproof"));
		exportSets.put(EXPORT_SDLUNCLEAN, Messages.getString("ExportDocxDialog.lable.exporttype.unclean"));
		exportSets.put(EXPORT_TMX, Messages.getString("ExportDocxDialog.lable.exporttype.tmx"));
		// exportSets.put(EXPORT_SDLXLIFF, Messages.getString("ExportDocxDialog.lable.exporttype.sdlxliff"));
	}

	public static String[] getExportTypes() {
		String[] rs = new String[exportSets.size()];
		int loop = 0;
		for (Entry<Integer, String> entry : exportSets.entrySet()) {
			rs[loop++] = entry.getValue(); 
		}
		return rs;
	}
	
	public static int getExportTypeCode(int index) {
		if (index < exportSets.size()) {
			int loop = 0;
			for (Entry<Integer, String> entry : exportSets.entrySet()) {
				if (loop++ == index) {
					return entry.getKey();
				}
			}
		}
		return EXPORT_HSPROOF;
	}
	
	public static String decodeXml(String str) {
		StringBuilder builder = new StringBuilder();
		builder.append("<r>").append(str).append("</r>");
		vg.setDoc(builder.toString().getBytes());
		try {
			vg.parse(true);
			VTDNav vn = vg.getNav();
			vn.toElement(VTDNav.ROOT);
			return vn.toString(vn.getText());
		} catch (Exception e) {
			//should never come here
			return str;
		}
	}

	public static void openErrorDialog(final Shell shell, final Throwable e) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog.openError(shell, Messages.getString("all.dialog.error"), e.getMessage(), new Status(
						IStatus.ERROR, Activator.PLUGIN_ID, e.getCause() == null ? null : e.getCause().getMessage(), e));
			}
		});
	}

	public static boolean openErrorDialog(final Shell shell, final String message) {
		if (shell != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(shell, Messages.getString("all.dialog.error"), message);
				}
			});
		}
		return true;
	}

	public static int openConfirmDialog(final Shell shell, final String message) {
		final int[] bools = new int[1];
		bools[0] = 0;
		if (shell != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog md = new MessageDialog(shell, Messages.getString("all.dialog.confirm"), null, message, 0,
							new String[] {Messages.getString("all.dialog.yes"),
						Messages.getString("all.dialog.yestoall"),
						Messages.getString("all.dialog.no"), 
						Messages.getString("all.dialog.notoall")}, CONFIRM_NO);
					bools[0] = md.open();
				}
			});
		}
		return bools[0];
	}
}
