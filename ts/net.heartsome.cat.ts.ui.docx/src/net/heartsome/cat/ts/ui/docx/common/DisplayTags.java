package net.heartsome.cat.ts.ui.docx.common;

public class DisplayTags {

	private boolean show;
	private StringBuilder content = new StringBuilder();
	private String displayText;

	public String getContent() {
		return content.toString();
	}

	public void appendContent(char[] ch, long l) {
		int offset = (int) (l >> 32);
		int length = (int) l;
		for (int i = offset; i < offset + length; i++) {
			switch (ch[i]) {
			case '<':
				content.append("&lt;");
				break;
			case '>':
				content.append("&gt;");
				break;
			case '\'':
				content.append("&apos;");
				break;
			case '\"':
				content.append("&quot;");
				break;
			case '&':
				content.append("&amp;");
				break;
			default:
				content.append(ch[i]);
			}
		}
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public String getDisplayText() {
//		return "r8";
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
}
