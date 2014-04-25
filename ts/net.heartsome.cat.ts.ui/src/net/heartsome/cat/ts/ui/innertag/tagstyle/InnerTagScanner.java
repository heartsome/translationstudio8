package net.heartsome.cat.ts.ui.innertag.tagstyle;

import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.TextStyle;

public class InnerTagScanner extends RuleBasedScanner {

	private Token tagContentToken;

	public InnerTagScanner() {
		initialize();
	}

	private void initialize() {
		updateToken();

		IRule[] rules = new IRule[1];
		rules[0] = createInnerTagRule(); // 创建内部标记规则

		setRules(rules);
	}

	protected void updateToken() {
		if (tagContentToken == null) {
			tagContentToken = new Token(null);
		}
		tagContentToken.setData(createTextStyle());
	}

	private TextStyle createTextStyle() {
		TextStyle style = new TextStyle();
		style.metrics = new GlyphMetrics(0, 0, 0);
		return style;
	}

	/**
	 * 创建标记正文规则
	 * @return ;
	 */
	private IRule createInnerTagRule() {
		InnerTagRule wordRule = new InnerTagRule(new InnerTagDetector());
		wordRule.addWord("[" + PlaceHolderEditModeBuilder.MIN + "-" + PlaceHolderEditModeBuilder.MAX + "]", tagContentToken);

		return wordRule;
	}

	/**
	 * 内部标记标记内容部分的探测器
	 * @author weachy
	 * @version
	 * @since JDK1.6
	 */
	class InnerTagDetector implements IWordDetector {

		public boolean isWordStart(char c) {
			return c >= PlaceHolderEditModeBuilder.MIN && c <= PlaceHolderEditModeBuilder.MAX;
		}

		public boolean isWordPart(char c) {
			return false;
		}
	}
}
