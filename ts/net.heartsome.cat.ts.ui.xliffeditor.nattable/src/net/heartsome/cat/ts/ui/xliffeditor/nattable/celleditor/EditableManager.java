package net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor;

/**
 * 可编辑属性管理器，用于 StyledTextCellEditor 的可编辑属性
 * @author weachy
 * @version
 * @since JDK1.5
 */
public abstract class EditableManager {

	/** 是否可编辑 */
	protected boolean editable = true;

	/** 是否是源文本 */
	private boolean isSource;

	/** 不可编辑状态提示信息 */
	private String uneditableMessage;

	/**
	 * 可编辑属性管理器
	 * @param isSource
	 *            是否为源文本
	 */
	public EditableManager(boolean isSource) {
		this.isSource = isSource;
	}

	/** 源文本编辑模式 */
	private SourceEditMode sourceEditMode = SourceEditMode.DISEDITABLE;

	/**
	 * 设置源文本编辑模式
	 * @param editMode
	 *            ;
	 */
	public void setSourceEditMode(SourceEditMode editMode) {
		this.sourceEditMode = editMode;
	}

	/**
	 * 得到源文本编辑模式
	 * @return ;
	 */
	public SourceEditMode getSourceEditMode() {
		return sourceEditMode;
	}

	/**
	 * 得到可编辑状态
	 * @return ;
	 */
	public boolean getEditable() {
		if (isSource) {
			if (sourceEditMode != SourceEditMode.DISEDITABLE) {
				return true;
			}
		}
		return editable;
	}

	/**
	 * 设置可编辑状态
	 * @param editable
	 *            是否为可编辑;
	 */
	public void setEditable(boolean editable) {
		// this.editable = editable;
		if (editable) {
			setupEditMode();
		} else {
			setupReadOnlyMode();
		}
	}

	/**
	 * 设置不可编辑提示信息，通常提示不可编辑的原因。
	 * @param uneditableMessage
	 *            不可编辑状态提示信息 ;
	 */
	protected void setUneditableMessage(String uneditableMessage) {
		this.uneditableMessage = uneditableMessage;
	}

	/**
	 * 得到不可编辑状态的提示信息。
	 * @return ;
	 */
	public String getUneditableMessage() {
		return uneditableMessage;
	}

	/**
	 * 判断“是否可编辑”状态 ;
	 */
	public abstract void judgeEditable();

	/**
	 * 设置只读模式 ;
	 */
	protected abstract void setupReadOnlyMode();

	/**
	 * 设置为编辑模式 ;
	 */
	protected abstract void setupEditMode();
}
