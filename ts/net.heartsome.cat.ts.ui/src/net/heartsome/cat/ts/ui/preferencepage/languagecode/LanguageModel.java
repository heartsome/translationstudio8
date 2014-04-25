package net.heartsome.cat.ts.ui.preferencepage.languagecode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;


/**
 * 此类用于用户与语言首选项进行交互时，操作相关<code>Language</code>设置
 * @author cheney
 * @since JDK1.6
 */
public class LanguageModel extends Language {

	// 首选项中的语言列表
	private Set<Language> languages;
	// 首选项中语言代码与语言实体的关联 Map
	private Map<String, Language> languagesMap;

	/**
	 * 默认构造函数构建
	 */
	public LanguageModel() {
		this("root", "root", null,false);
	}

	/**
	 * 继承父类<code>Language</code>的构造函数
	 * @param code
	 * @param name
	 * @param bidi
	 */
	public LanguageModel(String code, String name, String imagePath, boolean bidi) {
		super(code, name,imagePath, bidi);
		languagesMap=LocaleService.getDefaultLanguage();
		languages=new HashSet<Language>(languagesMap.values());
		
	}

	/**
	 * 语言列表
	 * @return 语言列表;
	 */
	public Set<Language> getLanguages() {
		return languages;
	}

	/**
	 * 语言列表
	 * @param languages
	 *            语言列表;
	 */
	public void setLanguages(Set<Language> languages) {
		this.languages = languages;
	}

	/**
	 * 语言代码和语言对应的 Map
	 * @return 语言代码和语言对应的 Map;
	 */
	public Map<String, Language> getLanguagesMap() {
		return languagesMap;
	}

	/**
	 * 语言代码和语言对应的 Map
	 * @param languagesMap
	 *            语言代码和语言对应的 Map;
	 */
	public void setLanguagesMap(Map<String, Language> languagesMap) {
		this.languagesMap = languagesMap;
	}

	/**
	 * 删除特定的语言列表
	 * @param langs
	 *            牪定的语言列表;
	 */
	public void removeLanguage(List<Language> langs) {
		if (langs == null || langs.isEmpty() || this.languages == null || this.languages.isEmpty()) {
			return;
		}
		for (Language language : langs) {
			removeLanguage(language);
		}
	}

	/**
	 * 删除特定的语言列表
	 * @param lang
	 *            删除特定的语言列表;
	 */
	public void removeLanguage(Language lang) {
		if (lang == null || this.languages == null || this.languages.isEmpty() || this.languagesMap == null) {
			return;
		}
		this.languages.remove(lang);
		this.languagesMap.remove(lang.getCode());
	}

}
