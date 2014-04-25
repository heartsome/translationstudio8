package net.heartsome.cat.convert.ui.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.heartsome.cat.convert.ui.model.DefaultConversionItem;
import net.heartsome.cat.convert.ui.model.IConversionItem;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadAdapter;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件上传对话框
 * @author cheney
 */
class FileUploadConversionItemDialog implements IConversionItemDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadConversionItemDialog.class);

	private FileUploadDialog dialog;
	private IConversionItem conversionItem;

	/**
	 * 构建函数
	 * @param shell
	 */
	public FileUploadConversionItemDialog(Shell shell) {
		dialog = new FileUploadDialog(shell);
	}

	@Override
	public int open() {
		int result = dialog.open();
		UploadItem uploadItem = null;
		if (result == IDialogConstants.OK_ID) {
			uploadItem = dialog.getLastUploadItem();
			if (uploadItem != null) {
				InputStream is = uploadItem.getFileInputStream();
				// 把文件流写到临时目录中
				if (is != null) {
					File file = null;
					OutputStream os = null;
					try {
						file = File.createTempFile("conversion", "co");
						os = new FileOutputStream(file);
						byte[] buffer = new byte[2048];
						int length = -1;
						while ((length = is.read(buffer)) != -1) {
							os.write(buffer, 0, length);
						}
						os.flush();
					} catch (FileNotFoundException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("文件不存在。", e);
						}
					} catch (IOException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("操作文件失败。", e);
						}
					} finally {

						try {
							if (os != null) {
								os.close();
							}
							if (is != null) {
								is.close();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					conversionItem = new DefaultConversionItem(new Path(file.getAbsolutePath()));
					return IDialogConstants.OK_ID;
				}
			}
		}
		return IDialogConstants.CANCEL_ID;
	}

	@Override
	public IConversionItem getConversionItem() {
		if (conversionItem == null) {
			conversionItem = DefaultConversionItem.EMPTY_CONVERSION_ITEM;
		}
		return conversionItem;
	}

	/**
	 * 文件上传对话框的具体实现
	 * @author cheney
	 * @since JDK1.6
	 */
	private static class FileUploadDialog extends TitleAreaDialog {

		private Upload upload;
		// 最后上传成功的 upload item
		private UploadItem lastUploadItem;
		private Shell shell;

		/**
		 * 构造函数
		 * @param parentShell
		 */
		public FileUploadDialog(Shell parentShell) {
			super(parentShell);
			setTitle("上传文件");
			// setMessage("请选择需要上传的文件");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			shell = parent.getShell();

			Composite parentComposite = (Composite) super.createDialogArea(parent);

			Composite contents = new Composite(parentComposite, SWT.NONE);
			GridLayout layout = new GridLayout();
			contents.setLayout(layout);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			contents.setLayoutData(gridData);

			Composite uploadComposite = new Composite(contents, SWT.NONE);
			uploadComposite.setLayout(new GridLayout());

			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			uploadComposite.setLayoutData(gridData);

			upload = new Upload(uploadComposite, SWT.BORDER, Upload.SHOW_UPLOAD_BUTTON | Upload.SHOW_PROGRESS);
			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			upload.setLayoutData(gridData);

			upload.addUploadListener(new UploadAdapter() {
				@Override
				public void uploadFinished(UploadEvent uploadEvent) {
					if (uploadEvent.isFinished()) {
						MessageDialog.openInformation(shell, "上传成功", "文件上传成功");
						// 确保关闭之前的文件 IO
						if (lastUploadItem != null) {
							InputStream is = lastUploadItem.getFileInputStream();
							if (is != null) {
								try {
									is.close();
								} catch (IOException e) {
									// ignore the exception
									if (LOGGER.isErrorEnabled()) {
										LOGGER.error("关闭上传的文件流失败。", e);
									}
								}
							}
						}
						lastUploadItem = upload.getUploadItem();
					}
				}
			});

			Dialog.applyDialogFont(parentComposite);

			Point defaultMargins = LayoutConstants.getMargins();
			GridLayoutFactory.fillDefaults().numColumns(2).margins(defaultMargins.x, defaultMargins.y).generateLayout(
					contents);

			return contents;
		}

		/**
		 * 获得最后一次上传的 upload item
		 * @return
		 */
		public UploadItem getLastUploadItem() {
			return lastUploadItem;
		}

	}

}
