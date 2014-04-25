package net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor;

public enum SourceEditMode {

	/**
	 * 不可编辑
	 */
	DISEDITABLE,

	/**
	 * 仅可编辑一次
	 */
	ONCE_EDITABLE,

	/**
	 * 总是可编辑
	 */
	ALWAYS_EDITABLE;

	/**
	 * 取得下一状态
	 * @return ;
	 */
	public SourceEditMode getNextMode() {
		switch (this) {
		case DISEDITABLE:
			return ONCE_EDITABLE;
		case ONCE_EDITABLE:
			return ALWAYS_EDITABLE;
		case ALWAYS_EDITABLE:
			return DISEDITABLE;
		default:
			return DISEDITABLE;
		}
	}

	/**
	 * 取得下一状态
	 * @return ;
	 */
	public String getImagePath() {
		switch (this) {
		case DISEDITABLE:
			return "images/SourceEditable/disable.png";
		case ONCE_EDITABLE:
			return "images/SourceEditable/once.png";
		case ALWAYS_EDITABLE:
			return "images/SourceEditable/always.png";
		default:
			return "images/SourceEditable/disable.png";
		}
	}
}