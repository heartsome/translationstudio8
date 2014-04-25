/**
 * ExecuteBingTransHandler.java
 *
 * Version information :
 *
 * Date:2012-6-14
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.bingtrans.handler;

import net.heartsome.cat.ts.bingtrans.SimpleMatcherBingImpl;
import net.heartsome.cat.ts.bingtrans.bean.PrefrenceParameters;
import net.heartsome.cat.ts.bingtrans.resource.Messages;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.translation.view.MatchViewPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class ExecuteBingTransHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof IXliffEditor)) {
			return null;
		}

		// check the google translation state: check the key availability
		PrefrenceParameters ps = PrefrenceParameters.getInstance();
		if (!ps.getState()) {
			MessageDialog.openError(window.getShell(),
					Messages.getString("handler.ExecuteBingTransHandler.msgTitle"),
					Messages.getString("handler.ExecuteBingTransHandler.msg"));
			return null;
		}
		String tshelp = System.getProperties().getProperty("TSHelp");
		String tsstate = System.getProperties().getProperty("TSState");
		if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
			LoggerFactory.getLogger(ExecuteBingTransHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
			System.exit(0);
		}
		final IXliffEditor xliffEditor = (IXliffEditor) editor;

		final int[] selectedRowIndexs = xliffEditor.getSelectedRows();
		if (selectedRowIndexs.length == 0) {
			return null;
		}
//		int currentRowIndex = selectedRowIndexs[0];
//		TransUnitBean transUnitBean = xliffEditor.getRowTransUnitBean(currentRowIndex);
//		if (transUnitBean == null) {
//			return null;
//		}
//		String srcPureText = transUnitBean.getSrcText();
//		String tgtLanguage = xliffEditor.getTgtColumnName();
//		String srcLanguage = xliffEditor.getSrcColumnName();

//		TransUnitInfo2TranslationBean tuInfo2Trans = new TransUnitInfo2TranslationBean();
//		tuInfo2Trans.setSrcPureText(srcPureText);
//		tuInfo2Trans.setSrcLanguage(srcLanguage);
//		tuInfo2Trans.setTgtLangugage(tgtLanguage);

		ISimpleMatcher matcher = new SimpleMatcherBingImpl();
//		String tgtText = matcher.executeMatch(tuInfo2Trans);
//		if (tgtText.equals("")) {
//			return null;
//		}

//		AltTransBean bean = new AltTransBean(srcPureText, tgtText, srcLanguage, tgtLanguage,
//				matcher.getMathcerOrigin(), matcher.getMathcerToolId());
//		bean.getMatchProps().put("match-quality", "100");
//		bean.getMatchProps().put("hs:matchType", matcher.getMatcherType());
//		bean.setSrcContent(srcPureText);
//		bean.setTgtContent(tgtText);

//		List<AltTransBean> newAltTrans = new ArrayList<AltTransBean>();
//		newAltTrans.add(bean);
		
		// check if need save the AltTrans to file
//		if (CommonFunction.checkEdition("U") && matcher.isSuportPreTrans()) {
//			List<String> oldToolIds = new ArrayList<String>();
//			oldToolIds.add(matcher.getMathcerToolId());
//			xliffEditor.getXLFHandler().updateAltTrans(xliffEditor.getXLFHandler().getRowId(currentRowIndex), newAltTrans, oldToolIds);
//		}

		IViewPart viewPart = window.getActivePage().findView(MatchViewPart.ID);
		if (viewPart != null && viewPart instanceof MatchViewPart) {
			MatchViewPart matchView = (MatchViewPart) viewPart;
			//matchView.refreshView(xliffEditor, selectedRowIndexs[0]);
//			matchView.refreshViewByToolId(xliffEditor, newAltTrans, matcher.getMathcerToolId());
//			matchView.replaceMatchs(newAltTrans);
//			newAltTrans.clear();
			matchView.manualExecSimpleTranslation(matcher);
		}

		return null;
	}

}
