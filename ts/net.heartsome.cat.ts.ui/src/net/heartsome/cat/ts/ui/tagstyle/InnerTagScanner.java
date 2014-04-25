package net.heartsome.cat.ts.ui.tagstyle;

import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;

public class InnerTagScanner extends RuleBasedScanner {

	private Token tagIndexToken;
	private Token tagContentToken;
	private IPreferenceStore store;
	private boolean isIndex;
	
	public InnerTagScanner(IPreferenceStore store, String foregroundKey, String backgroundKey) {
		this.store = store;
		initialize(foregroundKey, backgroundKey);
		isIndex = false;
	}
	
	public InnerTagScanner(IPreferenceStore store, String foregroundKey, String backgroundKey, boolean isIndex) {
		this(store, foregroundKey, backgroundKey);
		this.isIndex = isIndex;
	}

	private void initialize(String foregroundKey, String backgroundKey) {
		updateToken(foregroundKey, backgroundKey);

		IRule[] rules = new IRule[3];
		rules[0] = createTagStartIndexRule(); // 创建标记开始索引规则
		rules[1] = createTagEndIndexRule(); // 创建标记结束索引规则
		rules[2] = createTagContentRule(); // 创建标记正文规则

		setRules(rules);
	}

	protected void updateToken(String foregroundKey, String backgroundKey) {
		if (tagContentToken == null) {
			tagContentToken = new Token(null);
		}
		if (tagIndexToken == null) {
			tagIndexToken = new Token(null);
		}
		tagIndexToken.setData(createTextStyle(foregroundKey, backgroundKey));
		tagContentToken.setData(createTextStyle(backgroundKey, foregroundKey));
	}

	private Color getColor(String colorKey) {
		String tagfg = store.getString(colorKey);
		return GUIHelper.getColor(StringConverter.asRGB(tagfg));
	}

	private TextStyle createTextStyle(String foregroundKey, String backgroundKey) {
		Color foreground = getColor(foregroundKey);
		Color background = getColor(backgroundKey);
		return new TextStyle(JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT), foreground, background);
	}

	/**
	 * 创建标记开始索引规则
	 * @return ;
	 */
	private IRule createTagStartIndexRule() {
		InnerTagRule wordRule = new InnerTagRule(InnerTagRule.TYPE_TAG_START_INDEX, new TagStartIndexDetector());
		wordRule.addWord("^((" + InnerTagUtil.INVISIBLE_CHAR + "\\d+)|(\\d+" + InnerTagUtil.INVISIBLE_CHAR + "))$", tagIndexToken);

		return wordRule;
	}

	/**
	 * 创建标记结束索引规则
	 * @return ;
	 */
	private IRule createTagEndIndexRule() {
		InnerTagRule wordRule = new InnerTagRule(InnerTagRule.TYPE_TAG_END_INDEX, new TagEndIndexDetector());
		wordRule.addWord("^\\d+" + InnerTagUtil.INVISIBLE_CHAR + "$", tagIndexToken);

		return wordRule;
	}

	/**
	 * 创建标记正文规则
	 * @return ;
	 */
	private IRule createTagContentRule() {
		InnerTagRule wordRule = new InnerTagRule(InnerTagRule.TYPE_TAG_Content, new TagContentDetector());
		// "^_(x|bx|ex|ph|g|bpt|ept|ph|it|mrk|sub)_$" 匹配简单标记和完整标记
		wordRule.addWord("^" + InnerTagUtil.INVISIBLE_CHAR + "((x|bx|ex|ph|g|bpt|ept|ph|it|mrk|sub)"+
				"|(<(x|bx|ex|ph|bpt|ept|ph|it|mrk|sub)\\s*(\\w*\\s*=\\s*('|\")(.|\n)*('|\"))*>?(.|\n)*<?/(x|bx|ex|ph|bpt|ept|ph|it|mrk|sub)?>)"+
				"|(<g(\\s*\\w*\\s*=\\s*('|\")(.|\n)*('|\"))*>)|</g>)"
				+ InnerTagUtil.INVISIBLE_CHAR + "$", tagContentToken);

		return wordRule;
	}
	
	/**
	 * 内部标记开始索引部分的探测器
	 * @author weachy
	 * @version
	 * @since JDK1.6
	 */
	class TagStartIndexDetector implements IWordDetector {

		public boolean isWordStart(char c) {
			if(isIndex){
				return c == InnerTagUtil.INVISIBLE_CHAR || Character.isDigit(c);
			}
			if (TagStyle.curStyle == TagStyle.INDEX) {
				return c == InnerTagUtil.INVISIBLE_CHAR || Character.isDigit(c);
			} else {
				return c == InnerTagUtil.INVISIBLE_CHAR;
			}
		}

		public boolean isWordPart(char c) {
			if(isIndex){
				return c == InnerTagUtil.INVISIBLE_CHAR || Character.isDigit(c);
			}
			if (TagStyle.curStyle == TagStyle.INDEX) {
				return c == InnerTagUtil.INVISIBLE_CHAR || Character.isDigit(c);
			} else {
				return Character.isDigit(c);
			}
		}
	}

	/**
	 * 内部标记结束索引部分的探测器
	 * @author weachy
	 * @version
	 * @since JDK1.6
	 */
	class TagEndIndexDetector implements IWordDetector {

		private boolean end = false;

		public boolean isWordStart(char c) {
			return Character.isDigit(c);
		}

		public boolean isWordPart(char c) {
			if (end) {
				end = false;
				return false;
			}
			if (c == InnerTagUtil.INVISIBLE_CHAR) {
				end = true;
			}
			return Character.isDigit(c) || c == InnerTagUtil.INVISIBLE_CHAR;
		}
	}

	/**
	 * 内部标记标记内容部分的探测器
	 * @author weachy
	 * @version
	 * @since JDK1.6
	 */
	class TagContentDetector implements IWordDetector {

		private boolean end = false;

		public boolean isWordStart(char c) {
			return c == InnerTagUtil.INVISIBLE_CHAR;
		}

		public boolean isWordPart(char c) {
			if (end) {
				end = false;
				return false;
			}
			if (c == InnerTagUtil.INVISIBLE_CHAR) {
				end = true;
				return true;
			}
//			return Character.isLetter(c);
//			当显示完整标记时，可能标记中会含有空格等字符，因此返回 true
			return true;
		}
	}
}
