package net.heartsome.cat.ts.ui.innertag.tagstyle;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class InnerTagRule extends WordRule {

	public InnerTagRule(IWordDetector detector) {
		super(detector);
	}

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
					return token;
				}
				if (fDefaultToken.isUndefined()) {
					unreadBuffer(scanner);
				}
				return fDefaultToken;
			}
		} else {
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
