package net.heartsome.cat.converter.word2007;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;

import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.word2007.common.PathConstant;
import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.common.ZipUtil;
import net.heartsome.cat.converter.word2007.partOper.DocumentPart;
import net.heartsome.cat.converter.word2007.partOper.DocumentRelation;

public class TestWord2007 {
	private XliffOutputer xlfOutput;
	public TestWord2007(){}
	
	public void docx2Xliff(){
		try {
//			String inputFile = "/home/robert/Desktop/测试简单word文档.docx";
//			String inputFile = "/home/robert/Desktop/Word2007 for test.docx";
//			String xliffFile = "/home/robert/workspace/runtime-UltimateEdition.product/testDocxConverter/XLIFF/zh-CN/测试简单word文档.docx.hsxliff";
//			String sourceLanguage = "en-US";
//			String targetLanguage = "zh-CN";
//			String srx = "/home/robert/workspace/newR8/.metadata/.plugins/org.eclipse.pde.core/UltimateEdition.product/net.heartsome.cat.converter/srx/default_rules.srx";
//			String catalogue = "/home/robert/workspace/newR8/.metadata/.plugins/org.eclipse.pde.core/UltimateEdition.product/net.heartsome.cat.converter/catalogue/catalogue.xml";
			
			String inputFile = "C:\\Users\\Administrator\\Desktop\\test word 2007 converter\\测试简单word文档.docx";
//			String inputFile = "C:\\Users\\Administrator\\Desktop\\test word 2007 converter\\Word2007 for test.docx";
			String xliffFile = "E:\\workspaces\\runtime-UltimateEdition.product\\testWord2007Convert\\XLIFF\\zh-CN\\测试简单word文档.docx.hsxliff";
			String sourceLanguage = "en-US";
			String targetLanguage = "zh-CN";
			String srx = "E:\\workspaces\\newR8\\.metadata\\.plugins\\org.eclipse.pde.core\\UltimateEdition.product\\net.heartsome.cat.converter\\srx\\default_rules.srx";
			String catalogue = "E:\\workspaces\\newR8\\.metadata\\.plugins\\org.eclipse.pde.core\\UltimateEdition.product\\net.heartsome.cat.converter\\catalogue\\catalogue.xml";
			
			StringSegmenter segmenter = new StringSegmenter(srx, sourceLanguage, catalogue);
			
			// 先解压 docx 文件
			String docxFolderPath = ZipUtil.upZipFile(inputFile, null);
			PathUtil pathUtil = new PathUtil(docxFolderPath);
			
			// 定义一个 hsxliff 文件的写入方法
			xlfOutput = new XliffOutputer(xliffFile, sourceLanguage, targetLanguage);
			xlfOutput.writeHeader(inputFile, "skeletonFile", true, "UTF-8", "");
			
			
			// 先从主文档 document 文件入手
			String docPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENT, false);
			DocumentPart docPart = new DocumentPart(docPath, pathUtil, xlfOutput, segmenter, inputFile, new NullProgressMonitor());
			docPart.converter();
			System.out.println("--------------");
			
			xlfOutput.outputEndTag();
			
			// 开始将文件进行压缩
//			String zipPath = "/home/robert/Desktop/word 2007 skeleton/" + new File(pathUtil.getSuperRoot()).getName();
			String zipPath = "C:\\Users\\Administrator\\Desktop\\word 2007 skeleton\\" + new File(pathUtil.getSuperRoot()).getName();
			ZipUtil.zipFolder(zipPath, pathUtil.getSuperRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				xlfOutput.close();
				System.out.println("----------");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	
	public void xliff2Docx() {
		try {
			String xliffFile = "/home/robert/workspace/runtime-UltimateEdition.product/testDocxConverter/XLIFF/zh-CN/测试简单word文档.docx.hsxliff";
			String outputFile = "/home/robert/Desktop/word 2007 skeleton/最终word文档.docx";
			String skeletonFile = "/home/robert/Desktop/word 2007 skeleton/测试简单word文档.docx_files";
			
//			String xliffFile = "E:\\workspaces\\runtime-UltimateEdition.product\\testWord2007Convert\\XLIFF\\zh-CN\\测试简单word文档.docx.hsxliff";
//			String outputFile = "/home/robert/Desktop/word 2007 skeleton/最终word文档.docx";
//			String skeletonFile = "C:\\Users\\Administrator\\Desktop\\word 2007 skeleton\\测试简单word文档.docx_files";
			
			// 先解压 docx 文件
			String docxFolderPath = ZipUtil.upZipFile(skeletonFile, null);
			PathUtil pathUtil = new PathUtil(docxFolderPath);
			
			// 定义一个 hsxliff 的读入器
			XliffInputer xlfInput = new XliffInputer(xliffFile, pathUtil);
			
			// 正转换是从 主文档入手的，而逆转换则是从 word/_rels/document.xml.rels 入手，先处理掉 页眉，页脚，脚注，批注，尾注
			String docRelsPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENTRELS, false);
			DocumentRelation docRels = new DocumentRelation(docRelsPath, pathUtil);
			docRels.arrangeRelations(xlfInput, new NullProgressMonitor());
			
			// 再处理主文档
//			pathUtil.setSuperRoot();
//			String docPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENT, false);
//			DocumentPart documentPart = new DocumentPart(docPath, xlfInput);
//			documentPart.reverseConvert();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("----------");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	
	public void testSegDocument(){
//		String inputFile = "/home/robert/Desktop/测试简单word文档.docx";
//		String inputFile = "/home/robert/Desktop/Word2007 for test.docx";
//		String xliffFile = "/home/robert/workspace/runtime-UltimateEdition.product/testDocxConverter/XLIFF/zh-CN/测试简单word文档.docx.hsxliff";
//		String sourceLanguage = "en-US";
//		String targetLanguage = "zh-CN";
//		String srx = "/home/robert/workspace/newR8/.metadata/.plugins/org.eclipse.pde.core/UltimateEdition.product/net.heartsome.cat.converter/srx/default_rules.srx";
//		String catalogue = "/home/robert/workspace/newR8/.metadata/.plugins/org.eclipse.pde.core/UltimateEdition.product/net.heartsome.cat.converter/catalogue/catalogue.xml";
//		String docPath = "/home/robert/Desktop/document.xml";
		
		
		String inputFile = "C:\\Users\\Administrator\\Desktop\\test word 2007 converter\\测试简单word文档.docx";
//		String inputFile = "C:\\Users\\Administrator\\Desktop\\test word 2007 converter\\Word2007 for test.docx";
		String xliffFile = "E:\\workspaces\\runtime-UltimateEdition.product\\testWord2007Convert\\XLIFF\\zh-CN\\测试简单word文档.docx.hsxliff";
		String sourceLanguage = "en-US";
		String targetLanguage = "zh-CN";
		String srx = "E:\\workspaces\\newR8\\.metadata\\.plugins\\org.eclipse.pde.core\\UltimateEdition.product\\net.heartsome.cat.converter\\srx\\default_rules.srx";
		String catalogue = "E:\\workspaces\\newR8\\.metadata\\.plugins\\org.eclipse.pde.core\\UltimateEdition.product\\net.heartsome.cat.converter\\catalogue\\catalogue.xml";
		String docPath = "C:\\Users\\Administrator\\Desktop\\document.xml";
		
		try {
			StringSegmenter segmenter = new StringSegmenter(srx, sourceLanguage, catalogue);
			
			// 定义一个 hsxliff 文件的写入方法
			xlfOutput = new XliffOutputer(xliffFile, sourceLanguage, targetLanguage);
			xlfOutput.writeHeader(inputFile, "skeletonFile", true, "UTF-8", "");
			
			
			// 先从主文档 document 文件入手
			DocumentPart docPart = new DocumentPart(docPath, null, xlfOutput, segmenter, inputFile, new NullProgressMonitor());
			docPart.testSegFile();
			System.out.println("--------------");
			
			xlfOutput.outputEndTag();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		TestWord2007 test = new TestWord2007();
//		test.docx2Xliff();
//		test.xliff2Docx();
		test.testSegDocument();
		
	}
}
