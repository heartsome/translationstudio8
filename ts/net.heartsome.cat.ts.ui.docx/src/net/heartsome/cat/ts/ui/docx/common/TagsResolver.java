package net.heartsome.cat.ts.ui.docx.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * source 或 target 中的含标记文本，导出至外部文件需要转换。
 * <li>1. 外部文件为 TMX，如果标记不是 TMX 标准，则删除。</li>
 * <li>2. 外部文件为 unclean，标记需要替换。</li>
 */
public class TagsResolver {

	private char[] ch;
	
	private LinkedList<XlfFragment> list = new LinkedList<XlfFragment>();
	
	public void reset(String content) {
		ch = content.toCharArray();
		list.clear();
	}
	
	public List<XlfFragment> reslove() {
		XlfFragment fg = null;
		int endOffset = 0;
		for (int i = 0; i < ch.length; i++) {
			if (ch[i] == '<') {
				endOffset = i;
				fg = new XlfFragment();
				while (++endOffset < ch.length && ch[endOffset] != '>');
				if(ch[endOffset - 1] == '/') {
					fg.setTagType(XlfFragment.ELEM_SELFCLOSE);
				} else if (ch[i+1] == '/') {
					fg.setTagType(XlfFragment.ELEM_END);
				} else {
					fg.setTagType(XlfFragment.ELEM_START);
				}
				fg.setTag(getTag(ch, i, endOffset));
				fg.setFragment(i, endOffset - i + 1);
				list.add(fg);
				i = endOffset;
			} else {
				endOffset = i;
				fg = new XlfFragment();
				while (++endOffset < ch.length && ch[endOffset] != '<');
				fg.setTag(XlfFragment.CDDATA);
				fg.setFragment(i, endOffset - i);
				list.add(fg);
				i = endOffset - 1;
			}
		}
		return list;
	}
	
	public static void main(String[] args) {
		TagsResolver tr = new TagsResolver();
		tr.reset("<g id=\"\">G[<sub id=\"\">sub should <sub>(hidden sub!)</sub>never<sub>(another hidden sub)</sub> shown</ph> in</sub> g]</g>a");
		tr.reslove();
		tr.test();
		tr.getDisplayText();
	}
	
	public void test() {
		for (XlfFragment frag : list) {
			long l = frag.getFragment();
			int offset = (int) (l >> 32);
			int length = (int) l;
			System.out.println(frag.getTag() + ":" + new String(ch, offset, length));
		}
	}
	
	private int getTag(char[] ch, int offset, int length) {
		String str = tagName(ch, offset, length);
		if (str.charAt(0) == '/') {
			str = str.substring(1);
		}
		if (str.equals("g")) {
			return XlfFragment.TAG_G;
		} else if (str.equals("x")){
			return XlfFragment.TAG_X;
		} else if (str.equals("bx")){
			return XlfFragment.TAG_BX;
		} else if (str.equals("ex")){
			return XlfFragment.TAG_EX;
		} else if (str.equals("bpt")){
			return XlfFragment.TAG_BPT;
		} else if (str.equals("ept")){
			return XlfFragment.TAG_EPT;
		} else if (str.equals("sub")){
			return XlfFragment.TAG_SUB;
		} else if (str.equals("it")){
			return XlfFragment.TAG_IT;
		} else if (str.equals("ph")){
			return XlfFragment.TAG_PH;
		}
		return XlfFragment.CDDATA;
	}
	
	private String tagName(char[] ch, int offset, int length) {
		for (int i = 0; i <= length; i++) {
			if (isBorder(ch[i + offset])) {
				return new String(ch, offset + 1, i - 1);
			}
		}
		return null;
	}
	
	private boolean isBorder(char ch) {
		return ch == ' ' || ch == '\r' || ch == '\n' || ch=='>';
	}
	
	public List<DisplayTags> getDisplayText() {
		LinkedList<DisplayTags> dl = new LinkedList<DisplayTags>();
		Stack<XlfFragment> stack = new Stack<XlfFragment>();
		int index = 0;
		boolean inPh = false;
		for (XlfFragment frg : list) {
			DisplayTags dt = new DisplayTags();
			if (frg.getTag() == XlfFragment.CDDATA) {
				if (inPh) {
					dl.getLast().appendContent(ch, frg.getFragment());
					continue;
				} else {
					dt.appendContent(ch, frg.getFragment());
					dt.setShow(stack.isEmpty() ? true : testShow(stack.lastElement()));
				}
			} else if (frg.getTagType() == XlfFragment.ELEM_START) {
				if (frg.getTag() == XlfFragment.TAG_PH) {
					dt.appendContent(ch, frg.getFragment());
					dt.setShow(false);
					dt.setDisplayText("&lt;" + (++index) + "/&gt;");
					inPh = true;
				} else {
					stack.push(frg);
					dt.appendContent(ch, frg.getFragment());
					dt.setShow(false);
					dt.setDisplayText("&lt;" + (++index) + "&gt;");
				}
			} else if (frg.getTagType() == XlfFragment.ELEM_END) {
				if (frg.getTag() == XlfFragment.TAG_PH) {
					dl.getLast().appendContent(ch, frg.getFragment());
					inPh = false;
					continue;
				} else {
					stack.pop();
					dt.appendContent(ch, frg.getFragment());
					dt.setShow(false);
					dt.setDisplayText("&lt;/" + index + "&gt;");
				}
			} else if (frg.getTagType() == XlfFragment.ELEM_SELFCLOSE){
				dt.appendContent(ch, frg.getFragment());
				dt.setShow(false);
				dt.setDisplayText("&lt;" + ++index + "/&gt;");
			}
			dl.add(dt);
		}
		
//		for (DisplayTags dt : dl) {
//			if (dt.isShow()) {
//				System.out.println(dt.getContent());
//			}
//		}
		return dl;
	}
	
	private boolean testShow(XlfFragment f) {
		return f.getTag() == XlfFragment.TAG_G ||
				f.getTag() == XlfFragment.TAG_SUB;
	}
}
