package net.heartsome.cat.ts.ui.tagstyle;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class InnerTagRule extends WordRule {

	public InnerTagRule(int ruleType, IWordDetector detector) {
		super(detector);
		this.ruleType = ruleType;
	}

	public static int TYPE_TAG_START_INDEX = -1;

	public static int TYPE_TAG_END_INDEX = 0;

	public static int TYPE_TAG_Content = 1;

	private int ruleType;

	private static boolean start = false;

	/** Buffer used for pattern detection. */
	private StringBuffer fBuffer = new StringBuffer();

	@SuppressWarnings("unchecked")
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
				fBuffer.setLength(0);
				do {
					fBuffer.append((char) c);
					c = scanner.read();
				} while (fDetector.isWordPart((char) c) && c != ICharacterScanner.EOF);
				scanner.unread();

				String buffer = fBuffer.toString();
				IToken token = null;
				Set<String> set = fWords.keySet();
				for (String re : set) {
					if (re != null && buffer.matches(re)) {
						token = (IToken) fWords.get(re);
						break;
					}
				}

				if (token != null) { // 匹配并得到样式
					if (ruleType == TYPE_TAG_START_INDEX || ruleType == TYPE_TAG_Content) {
						start = true;
						return token;
					} else if (ruleType == TYPE_TAG_END_INDEX && start) {
						start = false;
						return token;
					}
				}
				if (fDefaultToken.isUndefined()) {
					unreadBuffer(scanner);
				}
				return fDefaultToken;
			}
		} else {
			// 当检查到最后一个规则（此顺序和 TagStyleConfiguration.getRecipeScanner() 中的规则加载顺序一致）
			// 仍然没有匹配的时候，将 start 状态恢复为 false。
			if (ruleType == TYPE_TAG_Content) {
				start = false;
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	@Override
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i = fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}
}
