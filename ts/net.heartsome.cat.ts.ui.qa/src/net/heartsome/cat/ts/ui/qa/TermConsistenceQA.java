package net.heartsome.cat.ts.ui.qa;

import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.tb.match.TbMatcher;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

/**
 * 术语一致性检查
 * 进度条提示格式为	MessageFormat.format("检查{0}文件，术语一致性检查：术语比较...）
 * @author robert	2011-11-09
 */
public class TermConsistenceQA extends QARealization {
	private TbMatcher matcher = new TbMatcher();
	private int level;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	private boolean isContinue;
	
	public TermConsistenceQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		level = preferenceStore.getInt(QAConstant.QA_PREF_term_TIPLEVEL);
	}
	
	@Override
	void setParentQaResult(QAResult qaResult) {
		super.setQaResult(qaResult);
	}
	
	/**
	 * 如果返回空，则标示未配置术语库
	 */
	@Override
	public String startQA(final QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			QATUDataBean tuDataBean) {
		
		if (tuDataBean.getTgtContent() == null || "".equals(tuDataBean.getTgtContent())) {
			return "";
		}
		hasError = false;
		isContinue = false;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (!matcher.checkTbMatcher(iFile.getProject())) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					String message = Messages.getString("qa.TermConsistenceQA.addTip1");
					isContinue = MessageDialog.openConfirm(model.getShell(), Messages.getString("qa.all.dialog.confirm"), message);
				}
			});
			if (!isContinue) {
				monitor.setCanceled(true);
			}
			// 未设置术语库，返回 null
			return null;
		}
		
		//获取操作术语库的操作类，设置新的项目
		matcher.setCurrentProject(iFile.getProject());
		
		//现在循环扇历该xliff文件的每一个trans-unit节点，去比较它的术语存在性,monitor推进3格
		compareTerms(iFile, xmlHandler, tuDataBean);
		
		String result = "";
		if (hasError && level == 0) {
			result = QAConstant.QA_TERM;
		}
		return result;
	}

	/**
	 * 比较术语
	 * @param iFile		要检查的文件
	 * @param handler	xml文件操作类
	 */
	private void compareTerms(IFile iFile, QAXmlHandler xmlHandler, QATUDataBean tuDataBean){
		String srcLang = tuDataBean.getSrcLang();
		String tgtLang = tuDataBean.getTgtLang();
		
		boolean souTerm_Context = getContext(srcLang);
		boolean tarTerm_Context = getContext(tgtLang);
		
		String sourceText = TextUtil.resetSpecialString(tuDataBean.getSrcPureText());
		String targetText = TextUtil.resetSpecialString(tuDataBean.getTgtPureText());
		
		targetText = TextUtil.normalise(targetText, true);
//		targetText = TextUtil.cleanString(targetText);
		
		sourceText = TextUtil.normalise(sourceText, true);
//		sourceText = TextUtil.cleanString(sourceText);
		
		Vector<Hashtable<String, String>> termsVector = matcher.serachTransUnitTerms(sourceText, srcLang, tgtLang, false);
		
		String lineNumber = tuDataBean.getLineNumber();
		String rowId = tuDataBean.getRowId();
		
		//开始循环术语库中的术语，进行比较
		for (int termIndex = 0; termIndex < termsVector.size(); termIndex++) {
			String sourceTerm = termsVector.get(termIndex).get("srcWord");
			String targetTerm = termsVector.get(termIndex).get("tgtWord");
			
			if (containsTerm(sourceText, sourceTerm, souTerm_Context)) {
				if (!containsTerm(targetText, targetTerm, tarTerm_Context)) {
//					String errorTip = MessageFormat.format(Messages.getString("qa.TermConsistenceQA.tip1"),
//							new Object[] { "'" + sourceTerm + "'", "'" + targetTerm + "'" });
					hasError = true;
					String qaTypeText = Messages.getString("qa.all.qaItem.TermConsistenceQA");
					super.printQAResult(new QAResultBean(level, QAConstant.QA_TERM, qaTypeText, null, tuDataBean.getFileName(), lineNumber, tuDataBean.getSrcContent(), tuDataBean.getTgtContent(), rowId));
				}
			}
		}
	}

	/**
     * 比较一个文本段是否包插指定术语，如果包括，返回true
     * @param context : 是否进行比较上下文,如果是英文，则要进行比较，因为英文每个单词之间有空格，
     * 如果是中文，就不需要比较上下文
     */
	private boolean containsTerm(String text, String term, boolean context){
		text = resetCleanString(text);	//因为查询出的术语已经进行转义了
	    int idxTerm = text.toLowerCase().indexOf(term.toLowerCase());
	    boolean exsit = false;
	    while(idxTerm != -1){
	    	char blankChar = ' ';
		    if (idxTerm < 0){
		        continue;
		    }
		    if (context) {
		    	char prevChar = blankChar;
			    char nextChar = blankChar;
			    
			    if (term.charAt(0) == blankChar) {
			    	prevChar = blankChar;
				}else {
					if (idxTerm > 0){
				        prevChar = text.charAt(idxTerm -1);
				    }
				}
			    if (term.charAt(term.length() - 1) == blankChar) {
			    	nextChar = blankChar;
				}else {
					if (idxTerm + term.length() +1 < text.length()){
				        nextChar = text.charAt(idxTerm + term.length());
				    }
				}
			    exsit = validContainsChar(prevChar) && validContainsChar(nextChar);
			    if (exsit) {
					return true;
				}
			}else {
				return true;
			}
		    idxTerm = text.toLowerCase().indexOf(term.toLowerCase(), idxTerm + 1);
	    }
	    return exsit;
	}

	private boolean validContainsChar(char ch){
	    return !(Character.isLetterOrDigit(ch) || ch == '@');
	}
	
	/**
	 * 判断语言的种类，如果是英语，那么要进行获取上下文进行判断，
	 * 如果是中文，不需要
	 * @param language
	 * @return
	 */
	private boolean getContext(String language){
		if (language.matches("zh.*|ja.*|ko.*|th.*|he.*")) {
			return false;
		}
		return true;
	}
	
	private static String resetCleanString(String string){
		string = string.replaceAll("&lt;", "<" ); 
		string = string.replaceAll("&gt;", ">"); 
//		string = string.replaceAll("&quot;", "\""); 
		string = string.replaceAll("&amp;", "&"); 
		
		return string;
	}
	
	@Override
	public void closeDB(){
		matcher.clearResources();
	}

}
