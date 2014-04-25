package net.heartsome.cat.ts.ui.docx.common;

/**
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class XlfFragment {

	// <g>, <x/>, <bx/>, <ex/>, <bpt>, <ept>, <sub>, <it>, <ph>.
	public static final int CDDATA = 0;
	public static final int TAG_G = 1;
	public static final int TAG_SUB = 2;
	public static final int TAG_PH = 3;
	public static final int TAG_IT = 4;
	public static final int TAG_X = 5;
	public static final int TAG_BX = 6;
	public static final int TAG_EX = 7;
	public static final int TAG_BPT = 8;
	public static final int TAG_EPT = 9;
	
	public static final int ELEM_SELFCLOSE = 1;
	public static final int ELEM_START = 2;
	public static final int ELEM_END = 3;
	
	private int tag = 0;
	private int tagType = 0;
	private long fragment = 0;// 高32 offset，低32 length
	
	public void setFragment(int offset, int length) {
		this.fragment = offset;
		this.fragment <<=32;
		this.fragment += length;
	}
	
	public long getFragment() {
		return fragment;
	}
	
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTagType() {
		return tagType;
	}

	public void setTagType(int tagType) {
		this.tagType = tagType;
	}
	
}
