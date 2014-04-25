package net.heartsome.cat.common.ui.innertag;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TagType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class InnerTag extends Canvas {

	/** 此 Style 用来加快内部标记重绘的速度，以及消除闪屏现象 */
	private static final int DEFAULT_STYLE_OPTIONS = SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED;

	// private static final int LINE_WIDTH = 1; // 线宽

	private InnerTagBean innerTagBean;

	// private Point tagSize;
	//
	// private Color bgColor;
	// private Color fgColor;
	private boolean isSelected = false;
	private InnerTagRender tagRender;

	/**
	 * 构造一个内部标记组件。
	 * @param parent
	 *            父容器。
	 * @param style
	 *            组件样式。参见 SWT 组件样式相关内容：{@link Widget#getStyle()}、{@link SWT}。
	 * @param tagContent
	 *            标记内容。
	 * @param tagName
	 *            标记名称。
	 * @param tagIndex
	 *            标记索引。
	 * @param tagType
	 *            标记类型。
	 * @param tagStyle
	 *            标记样式。
	 */
	public InnerTag(Composite parent, int style, String tagContent, String tagName, int tagIndex, TagType tagType,
			TagStyle tagStyle) {
		this(parent, style, new InnerTagBean(tagIndex, tagName, tagContent, tagType), tagStyle);
	}

	/**
	 * 构造一个内部标记组件。
	 * @param parent
	 *            父容器。
	 * @param style
	 *            组件样式。参见 SWT 组件样式相关内容：{@link Widget#getStyle()}、{@link SWT}。
	 * @param innerTagBean
	 *            内部标记实体
	 * @param tagStyle
	 *            标记样式。
	 */
	public InnerTag(Composite parent, int style, InnerTagBean innerTagBean, TagStyle tagStyle) {
		super(parent, style | DEFAULT_STYLE_OPTIONS);
		this.setInnerTagBean(innerTagBean);
		this.setToolTipText(resetRegularString(innerTagBean.getContent()));
		this.setBackground(parent.getBackground());
		tagRender = new InnerTagRender(this);
		init();
	}

	private String resetRegularString(String input) {
		input = input.replaceAll("&amp;", "&");
		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		input = input.replaceAll("&quot;", "\"");
		return input;

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof InnerTag)) {
			return false;
		}
		InnerTag that = (InnerTag) obj;
		return this.getInnerTagBean().equals(that.getInnerTagBean());
	}

	/**
	 * 计算标记组件尺寸。参见 Control 组件的 computeSize 方法。
	 * @param wHint
	 *            组件宽度
	 * @param hHint
	 *            组件高度
	 * @param changed
	 *            尺寸是否可变
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point p = tagRender.calculateTagSize(innerTagBean);
//		return super.computeSize(p.x, p.y, changed);
		return p;
	}

	// 初始化组件。
	private void init() {
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				tagRender.draw(e.gc, innerTagBean, 0, 0);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Control parent = getParent();
				if(parent instanceof StyledText){
					StyledText text = (StyledText) parent;
					Point size = getSize();
					Point p = getLocation();
					int offset = text.getOffsetAtLocation(p);
					int mouseX = e.x;
					if(mouseX > size.x / 2 ){
						text.setCaretOffset(offset + 1);						
					} else {
						text.setCaretOffset(offset);
					}
				}
			}
		});
	}

	public boolean isSelected() {
		return isSelected;
	};
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	/**
	 * 各种标记绘制样式效果。
	 * @param args
	 *            ;
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(800, 600);
		GridLayout gl = new GridLayout();
		shell.setLayout(gl);
		//
		// Color borderColor = new Color(display, 0, 255, 255);
		// Color textBgColor = new Color(display, 0, 205, 205);
		// Color indexBgColor = new Color(display, 0, 139, 139);
		// Color textFgColor = new Color(display, 0, 104, 139);
		// Color indexFgColor = borderColor;
		//
		// Font font = new Font(Display.getDefault(), "Arial", 8, SWT.BOLD);
		//
		// InnerTag tag1 = new InnerTag(shell, SWT.NONE, "<ph id=\"1\" />&lt;this&gt;&amp; is a ph text.</ph>", "ph",
		// 4333,
		// STANDALONE, FULL);
		// tag1.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag1.setFont(font);
		// tag1.pack();
		//
		// InnerTag tag2 = new InnerTag(shell, SWT.NONE, "<ph id=\"2\" />this is a ph text.</ph>", "ph", 2, STANDALONE,
		// SIMPLE);
		// tag2.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag2.setFont(font);
		// tag2.pack();
		//
		// InnerTag tag3 = new InnerTag(shell, SWT.NONE, "<ph id=\"3\" />this is a ph text.</ph>", "ph", 3, STANDALONE,
		// INDEX);
		// tag3.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag3.setFont(font);
		// tag3.pack();
		//
		// InnerTag tag4 = new InnerTag(shell, SWT.NONE, "<bx id=\"1\" />", "bx", 4, START, FULL);
		// tag4.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag4.setFont(font);
		// tag4.pack();
		//
		// InnerTag tag5 = new InnerTag(shell, SWT.NONE, "<bx id=\"2\" />", "bx", 5, START, SIMPLE);
		// tag5.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag5.setFont(font);
		// tag5.pack();
		//
		// InnerTag tag6 = new InnerTag(shell, SWT.NONE, "<bx id=\"3\" />", "bx", 6, START, INDEX);
		// tag6.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag6.setFont(font);
		// tag6.pack();
		//
		// InnerTag tag7 = new InnerTag(shell, SWT.NONE, "<ex id=\"3\" />", "ex", 6, END, INDEX);
		// tag7.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag7.setFont(font);
		// tag7.pack();
		//
		// InnerTag tag8 = new InnerTag(shell, SWT.NONE, "<ex id=\"2\" />", "ex", 5, END, SIMPLE);
		// tag8.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag8.setFont(font);
		// tag8.pack();
		// //
		// InnerTagControl tag9 = new InnerTagControl(shell, SWT.NONE, "", "", 4, STANDALONE, TagStyle.FULL);
		// tag9.initColor(textFgColor, textBgColor, indexFgColor, indexBgColor, borderColor);
		// tag9.setFont(font);
		// tag9.pack();
		//
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void setInnerTagBean(InnerTagBean innerTagBean) {
		this.innerTagBean = innerTagBean;
	}

	public InnerTagBean getInnerTagBean() {
		return innerTagBean;
	}
}
