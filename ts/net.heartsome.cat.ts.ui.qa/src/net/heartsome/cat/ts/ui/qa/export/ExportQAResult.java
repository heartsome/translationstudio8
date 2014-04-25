package net.heartsome.cat.ts.ui.qa.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 导出品质检查结果视图
 * @author robert	2013-07-31
 */
public class ExportQAResult {
	/** 是否合并打开 */
	private boolean isMultiFile;
	private String exportFilePath;
	private List<String> filePathList = new ArrayList<String>();
	public static final Logger LOGGER = LoggerFactory.getLogger(ExportQAResult.class);
	/** 已经合并的合并　id,　之后遇到不再合并 */
	private Set<String> mergedIdSet = new HashSet<String>();
	
	protected PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();
	protected XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);
	
	public ExportQAResult(){ }

	public ExportQAResult(boolean isMultiFile, String exportFilePath){
		this.isMultiFile = isMultiFile;
		this.exportFilePath = exportFilePath;
	}
	

	
	public void beginExport(List<QAResultBean> dataList, List<String> fileLCList, IProgressMonitor monitor){
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// 分成十份，其中解析文件 1 份，其余 9 份
		monitor.beginTask("", 10);
		monitor.setTaskName(Messages.getString("qa.export.ExportQAResult.monitor.title"));
		
		// 将　fileLCList 转换成相对路径
		List<File> fileList = new ArrayList<File>();
		for (String fileLC : fileLCList) {
			fileList.add(new File(fileLC));
		}
		for(IFile iFile : ResourceUtils.filesToIFiles(fileList)){
			filePathList.add(iFile.getFullPath().toOSString());
		}
		
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.worked(1);
		
		// UNDO 这里按文件排序给注释了。。。。
//		// 先按文件排序
//		sort(dataList);
		
		// 工作簿
		XSSFWorkbook workbook = new XSSFWorkbook();
		// 创建sheet页
		XSSFSheet sheet = workbook.createSheet();
		
		// 设置sheet名称
		workbook.setSheetName(0, Messages.getString("qa.export.ExportQAResult.sheet.title"));
		sheet.setColumnWidth(0, 255*6);
		sheet.setColumnWidth(1, 255*20);
		sheet.setColumnWidth(2, 255*30);
		sheet.setColumnWidth(3, 255*60);
		sheet.setColumnWidth(4, 255*60);

		XSSFFont titleFont = workbook.createFont();
		titleFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
		titleFont.setBold(true);
		titleFont.setFontHeight(20);

		XSSFFont headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeight(14);
		
		XSSFFont errorFont = workbook.createFont();
		errorFont.setColor(IndexedColors.RED.getIndex());
		
		XSSFCellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setFont(titleFont);
		
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		headerStyle.setFont(headerFont);
		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		
		XSSFCellStyle errorCellStyle = workbook.createCellStyle();
		errorCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		errorCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		errorCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		errorCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		errorCellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		errorCellStyle.setWrapText(true);
		errorCellStyle.setFont(errorFont);
		
		
		// 生成标题行
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(Messages.getString("qa.export.ExportQAResult.titleCell"));
		
		String[] headers = new String[] {
				Messages.getString("qa.export.ExportQAResult.header.errorLeavel"), // 级别
				Messages.getString("qa.export.ExportQAResult.header.qaType"), // 类型
				Messages.getString("qa.export.ExportQAResult.header.location"), // 位置
				Messages.getString("qa.export.ExportQAResult.header.srcText"), // 源文
				Messages.getString("qa.export.ExportQAResult.header.tgtText") // 　译文
		};
		
		
        // 产生表格标题行
		row = sheet.createRow(1);
        for (short i = 0; i < headers.length; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers[i]);
        }
        
        // 开始生成数据
        int index = 1;
        String rowId = null;

        // 先处理品质检查结果数据为空的情况
        if (dataList.size() <= 0) {
        	if (isMultiFile) {
        		String multiFileStr = getMultiResouce();
        		index++;
				row = sheet.createRow(index);
				for (int i = 0; i < headers.length; i++) {
					cell = row.createCell(i);
					cell.setCellStyle(cellStyle);
					if (i == headers.length - 1) {
						cell.setCellValue(multiFileStr);
					}
				}
			}else {
				for (String filePath : this.filePathList) {
					index++;
					row = sheet.createRow(index);
					for (int i = 0; i < headers.length; i++) {
						cell = row.createCell(i);
						cell.setCellStyle(cellStyle);
						if (i == headers.length - 1) {
							cell.setCellValue(filePath);
						}
					}
				}
			}
        	sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
		}else {
			 int interval = 1;
		        if (dataList.size() > 9) {
		        	interval = dataList.size() / 9;
				}
		        
		        int startMergeRow = -1;
		        int endMergeRow = -1;
		        
		        for (int i = 0; i < dataList.size(); i++) {
					QAResultBean bean = dataList.get(i);
		        	index++;
		        	System.out.println(index);
		        	if (index % interval == 0) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						monitor.worked(1);
					}
		        	
		            row = sheet.createRow(index);
		            
		        	// 处理合并　级别　与　类型　两列的行
		            mergeIF:if (bean.getMergeId() != null) {
		            	if (mergedIdSet.contains(bean.getMergeId())) {
							break mergeIF;
						}
		            	
		            	startMergeRow = index;
		            	mergeFor:for (int j = i + 1; j < dataList.size(); j++) {
		            		if (dataList.get(j).getMergeId() != null && dataList.get(j).getMergeId().equals(bean.getMergeId())) {
		            			mergedIdSet.add(bean.getMergeId());
								endMergeRow = index + (j - i);
							}else {
								break mergeFor;
							}
						}
		            	
						if (startMergeRow >= 0 && endMergeRow >= 0) {
							sheet.addMergedRegion(new CellRangeAddress(startMergeRow, endMergeRow, 0, 0));
							sheet.addMergedRegion(new CellRangeAddress(startMergeRow, endMergeRow, 1, 1));
							startMergeRow = -1;
							endMergeRow = -1;
						}
					}
		            
		        	// 循环当前行的每一列
		            for (int h = 0; h < headers.length; h++) {
		            	cell = row.createCell(h);
		            	cell.setCellStyle(cellStyle);
		            	
		            	String text = null;
		            	switch (h) {
						case 0:
							if (bean.getLevel() == 0) {
								text = Messages.getString("qa.export.ExportQAResult.errorLeavel.error");
								cell.setCellStyle(errorCellStyle);
							}else if (bean.getLevel() == 1) {
								text = Messages.getString("qa.export.ExportQAResult.errorLeavel.warning");
							}
							cell.setCellValue(text);
							break;
						case 1:
							text = bean.getQaTypeText();
							cell.setCellValue(text);
							break;
						case 2:
							text = bean.getFileName() + " [" + bean.getLineNumber() + "]";
							cell.setCellValue(text);
							break;
						case 3:
							text = bean.getSrcContent();
							cell.setCellValue(getDisplayText(text));
							break;
						case 4:
							text = bean.getTgtContent();
							cell.setCellValue(getDisplayText(text));
							break;
						default:
							break;
						}
					}
		        }
			    
//	            // 这是合并　文件路径
//	            if (isMultiFile) {
//	            	sheet.addMergedRegion(new CellRangeAddress(resourceIndex, index, 6, 6));
//	    		}else {
//	    			sheet.addMergedRegion(new CellRangeAddress(resourceIndex, index, 6, 6));
//	    		}
//	            sheet.addMergedRegion(new CellRangeAddress(rowidIndex, index, 1, 1));
//	    		sheet.addMergedRegion(new CellRangeAddress(rowidIndex, index, 4, 4));
//	    		sheet.addMergedRegion(new CellRangeAddress(rowidIndex, index, 5, 5));
		        
	    		// 标题行合并(处理未合并完的部份)
	    		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
		}
        	
       

        try {
			FileOutputStream fileoutputstream = new FileOutputStream(exportFilePath);
			workbook.write(fileoutputstream);
			fileoutputstream.close();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							Messages.getString("qa.all.dialog.info"),
							Messages.getString("qa.export.ExportQAResult.MSG.exportSuccess"));
				}
			});
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							Messages.getString("qa.all.dialog.info"),
							Messages.getString("qa.export.ExportQAResult.MSG.exportFail"));
				}
			});
			LOGGER.error(Messages.getString("qa.export.ExportQAResult.LOG.exportError"), e);
		}
	}
	
	/**
	 * 根据传入的源文或译文的全文本，获取要显示的文本段内容。
	 * @param srcOrTgtContent
	 * @return
	 */
	private String getDisplayText(String srcOrTgtContent){
		innerTagFactory.reset();
		String displayText = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag(srcOrTgtContent));
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			displayText = displayText.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
			displayText = displayText.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
			displayText = displayText.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
		}
		
		Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(displayText);
		int tagIdx = 1;
		while(matcher.find()){
			displayText = matcher.replaceFirst("{" + (tagIdx ++) + "}");
			matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(displayText);
		}
		return displayText;
	}

	
//	private void sort(List<QAResultBean> dataList){
//		//排序
//		Collections.sort(dataList, new Comparator<QAResultBean>() {
//			public int compare(QAResultBean o1, QAResultBean o2) {
//				return o1.getResource().compareTo(o2.getResource());
//			}
//		});
//	}
	
	/**
	 * 如果是合并打开，那么返回
	 * @param datalist
	 * @return
	 */
	private String getMultiResouce(){
		StringBuffer sb = new StringBuffer(); 
		sb.append(Messages.getString("qa.export.ExportQAResult.multifyOpen"));
		for(String resource : filePathList){
			sb.append(resource).append("\n");
		}
		
		return sb.toString();
	}
	
	
	public static byte[] getImageData(String imgPath){
		byte[] arrayByte = null;
		try {
			Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
			URL defaultUrl = buddle.getEntry(imgPath);
			String imagePath = imgPath;
			imagePath = FileLocator.toFileURL(defaultUrl).getPath();
			arrayByte = IOUtils.toByteArray(new FileInputStream(new File(imagePath)));
		} catch (Exception e) {
		}
		
		return arrayByte;
	}
	
	
	public static void main(String[] args) {
		int index = 0;
		thisIF:if (index < 10) {
			System.out.println(index);
			index += 10;
			if (index > 5) {
				break thisIF;
			}
			System.out.println(index);
		}
		
		
		
	}
	
}
