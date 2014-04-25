package net.heartsome.cat.ts.handlexlf.split;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;

public class SplitOrMergeXlfModel {
	/** 要切割的文件 */
	private IFile splitFile;
	/** 切割点的集合，保存的是切割点的trans-unit在文件中的位置 */
	private List<Integer> splitXlfPointsIndex = new LinkedList<Integer>();
	/** 切割点的集合，保存的是切割点的trans-unit的rowid */
	private List<String> splitXlfPointsRowId = new LinkedList<String>();
	/** xliffEditor */
	private XLIFFEditorImplWithNatTable xliffEditor;
	/** 从hander那边传过来的shell */
	private Shell shell;
	/** 切割后文件所陈放的文件夹 */
	private IContainer splitXlfsContainer;
	/** 要合并的xliff文件 */
	private Vector<IFile> mergeXliffFile;
	/** 分割后生成的子文件 */
	private LinkedList<String> newSplitedFilesName = new LinkedList<String>();
	
	public SplitOrMergeXlfModel(){}
	

	public IFile getSplitFile() {
		return splitFile;
	}
	public void setSplitFile(IFile splitFile) {
		this.splitFile = splitFile;
	}
	public XLIFFEditorImplWithNatTable getXliffEditor() {
		return xliffEditor;
	}
	public void setXliffEditor(XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
	}
	public Shell getShell() {
		return shell;
	}
	public void setShell(Shell shell) {
		this.shell = shell;
	}
	public IContainer getSplitXlfsContainer() {
		return splitXlfsContainer;
	}
	public void setSplitXlfsContainer(IContainer splitXlfsContainer) {
		this.splitXlfsContainer = splitXlfsContainer;
	}
	public Vector<IFile> getMergeXliffFile() {
		return mergeXliffFile;
	}
	public void setMergeXliffFile(Vector<IFile> mergeXliffFile) {
		this.mergeXliffFile = mergeXliffFile;
	}
	public LinkedList<String> getNewSplitedFilesName() {
		return newSplitedFilesName;
	}
	public void setNewSplitedFilesName(LinkedList<String> newSplitedFilesName) {
		this.newSplitedFilesName = newSplitedFilesName;
	}
	public List<Integer> getSplitXlfPointsIndex() {
		return splitXlfPointsIndex;
	}
	public void setSplitXlfPointsIndex(List<Integer> splitXlfPointsIndex) {
		this.splitXlfPointsIndex = splitXlfPointsIndex;
	}
	public List<String> getSplitXlfPointsRowId() {
		return splitXlfPointsRowId;
	}
	public void setSplitXlfPointsRowId(List<String> splitXlfPointsRowId) {
		this.splitXlfPointsRowId = splitXlfPointsRowId;
	}

}
