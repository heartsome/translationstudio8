package net.heartsome.cat.convert.ui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.CommonFunctions;
import net.heartsome.cat.convert.ui.utils.FileFormatUtils;
import net.heartsome.cat.converter.util.AbstractModelObject;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 在转换 xliff 文件时，转换设置对话框中可设置参数对应的实体 bean
 * @author cheney
 * @since JDK1.6
 */
public class ConversionConfigBean extends AbstractModelObject {
	// 源文件的路径
	private String source;

	// 目标文件路径
	private String target;

	// 骨架文件路径
	private String skeleton;

	// 源文件语言
	private String srcLang;

	// 目标语言
	private String tgtLang;

	// 源文件编码
	private String srcEncoding;

	// 目标文件编码
	private String targetEncoding;

	// 是否按段茖分段
	private boolean segByElement;

	// SRX 规则文件路径
	private String initSegmenter;

	// 是否标记为不可翻译
	private boolean lockXtrans;

	// 是否为 100％ 匹配的文本进行进行标记
	private boolean lock100;

	// 是否为上下文匹配的文本进行进行标记
	private boolean lock101;

	// 是否为重复的文本段进行标记
	private boolean lockRepeated;

	// 是否按 CR/LF 分段
	private boolean breakOnCRLF;

	// 将骨架嵌入 xliff 文件
	private boolean embedSkl;

	// 如果目标文件已存在，是否覆盖
	private boolean replaceTarget;

	// 预览模式
	private boolean previewMode;

	// 文件格式
	private List<String> fileFormats;

	// 编码列表
	private List<String> pageEncoding;

	// 目标语言列表
	private List<Language> tgtLangList;

	// 语言列表
	private List<Language> languages;

	// 文件类型
	private String fileType;

	/**
	 * 文件类型
	 * @return ;
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * 文件类型
	 * @param fileType
	 *            ;
	 */
	public void setFileType(String fileType) {
		firePropertyChange("fileType", this.fileType, this.fileType = fileType);
	}

	/**
	 * 语言列表
	 * @return ;
	 */
	public List<Language> getLanguages() {
		if (languages == null) {
			languages = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
			Collections.sort(languages, new Comparator<Language>() {
				public int compare(Language o1, Language o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
		}
		return languages;
	}

	/**
	 * 语言列表
	 * @param languages
	 *            ;
	 */
	public void setLanguages(List<Language> languages) {
		this.languages = languages;
	}

	/**
	 * 编码列表
	 * @return ;
	 */
	public List<String> getPageEncoding() {
		if (pageEncoding == null) {
			String[] codeArray = LocaleService.getPageCodes();
			pageEncoding = Arrays.asList(codeArray);
		}
		return pageEncoding;
	}

	/**
	 * 编码列表
	 * @param pageEncoding
	 *            ;
	 */
	public void setPageEncoding(List<String> pageEncoding) {
		this.pageEncoding = pageEncoding;
	}

	/**
	 * 将骨架嵌入 xliff 文件
	 * @return ;
	 */
	public boolean isEmbedSkl() {
		return embedSkl;
	}

	/**
	 * 将骨架嵌入 xliff 文件
	 * @param embedSkl
	 *            ;
	 */
	public void setEmbedSkl(boolean embedSkl) {
		this.embedSkl = embedSkl;
	}

	/**
	 * 源文件的路径
	 * @return ;
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 源文件的路径
	 * @param source
	 *            ;
	 */
	public void setSource(String source) {
		firePropertyChange("source", this.source, this.source = source);
	}

	/**
	 * 目标文件路径
	 * @return ;
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * 目标文件路径
	 * @param target
	 *            ;
	 */
	public void setTarget(String target) {
		firePropertyChange("target", this.target, this.target = target);
	}

	/**
	 * 骨架文件路径
	 * @return ;
	 */
	public String getSkeleton() {
		return skeleton;
	}

	/**
	 * 骨架文件路径
	 * @param skeleton
	 *            ;
	 */
	public void setSkeleton(String skeleton) {
		this.skeleton = skeleton;
	}

	/**
	 * 源文件语言
	 * @return ;
	 */
	public String getSrcLang() {
		return srcLang;
	}

	/**
	 * 源文件语言
	 * @param srcLang
	 *            ;
	 */
	public void setSrcLang(String srcLang) {
		firePropertyChange("srcLang", this.srcLang, this.srcLang = srcLang);
	}

	/**
	 * 目标语言
	 * @return the tgtLang
	 */
	public String getTgtLang() {
		if(tgtLang == null){
			tgtLang = "";
		}
		return tgtLang;
	}

	/**
	 * 目标语言
	 * @param tgtLang
	 *            the tgtLang to set
	 */
	public void setTgtLang(String tgtLang) {
		firePropertyChange("tgtLang", this.tgtLang, this.tgtLang = tgtLang);
	}

	/** @return 目标语言 */
	public List<Language> getTgtLangList() {
		return tgtLangList;
	}

	/**
	 * 目标语言
	 * @param tgtLang
	 *            the tgtLang to set
	 */
	public void setTgtLangList(List<Language> tgtLangList) {
		this.tgtLangList = tgtLangList;
	}

	/**
	 * 源文件编码
	 * @return ;
	 */
	public String getSrcEncoding() {
		return srcEncoding;
	}

	/**
	 * 源文件编码
	 * @param srcEncoding
	 *            ;
	 */
	public void setSrcEncoding(String srcEncoding) {
		firePropertyChange("srcEncoding", this.srcEncoding, this.srcEncoding = srcEncoding);
	}

	/**
	 * 是否按段茖分段
	 * @return ;
	 */
	public boolean isSegByElement() {
		return segByElement;
	}

	/**
	 * 是否按段茖分段
	 * @param segByElement
	 *            ;
	 */
	public void setSegByElement(boolean segByElement) {
		this.segByElement = segByElement;
	}

	/**
	 * SRX 规则文件路径
	 * @return ;
	 */
	public String getInitSegmenter() {
		return initSegmenter;
	}

	/**
	 * SRX 规则文件路径
	 * @param initSegmenter
	 *            ;
	 */
	public void setInitSegmenter(String initSegmenter) {
		firePropertyChange("initSegmenter", this.initSegmenter, this.initSegmenter = initSegmenter);
	}

	/**
	 * 是否标记为不可翻译
	 * @return ;
	 */
	public boolean isLockXtrans() {
		return lockXtrans;
	}

	/**
	 * 是否标记为不可翻译
	 * @param lockXtrans
	 *            ;
	 */
	public void setLockXtrans(boolean lockXtrans) {
		this.lockXtrans = lockXtrans;
	}

	/**
	 * 是否为 100％ 匹配的文本进行进行标记
	 * @return ;
	 */
	public boolean isLock100() {
		return lock100;
	}

	/**
	 * 是否为 100％ 匹配的文本进行进行标记
	 * @param lock100
	 *            ;
	 */
	public void setLock100(boolean lock100) {
		this.lock100 = lock100;
	}

	/**
	 * 是否为上下文匹配的文本进行进行标记
	 * @return ;
	 */
	public boolean isLock101() {
		return lock101;
	}

	/**
	 * 是否为上下文匹配的文本进行进行标记
	 * @param lock101
	 *            ;
	 */
	public void setLock101(boolean lock101) {
		this.lock101 = lock101;
	}

	/**
	 * 是否为上下文匹配的文本进行进行标记
	 * @return ;
	 */
	public boolean isLockRepeated() {
		return lockRepeated;
	}

	/**
	 * 是否为上下文匹配的文本进行进行标记
	 * @param lockRepeated
	 *            ;
	 */
	public void setLockRepeated(boolean lockRepeated) {
		this.lockRepeated = lockRepeated;
	}

	/**
	 * 是否按 CR/LF 分段
	 * @return ;
	 */
	public boolean isBreakOnCRLF() {
		return breakOnCRLF;
	}

	/**
	 * 是否按 CR/LF 分段
	 * @param breakOnCRLF
	 *            ;
	 */
	public void setBreakOnCRLF(boolean breakOnCRLF) {
		this.breakOnCRLF = breakOnCRLF;
	}

	/**
	 * 目标文件编码
	 * @param targetEncoding
	 *            ;
	 */
	public void setTargetEncoding(String targetEncoding) {
		firePropertyChange("targetEncoding", this.targetEncoding, this.targetEncoding = targetEncoding);
	}

	/**
	 * 目标文件编码
	 * @return ;
	 */
	public String getTargetEncoding() {
		return targetEncoding;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("configuration:--------------------\n");
		buffer.append("source:" + getSource() + "\n");
		buffer.append("target:" + getTarget() + "\n");
		buffer.append("skeleton:" + getSkeleton() + "\n");
		buffer.append("srclang:" + getSrcLang() + "\n");
		buffer.append("tgtlang:" + getTgtLang() + "\n");
		buffer.append("srcEncoding:" + getSrcEncoding() + "\n");
		buffer.append("targetEncoding:" + getTargetEncoding() + "\n");
		buffer.append("segByElement:" + isSegByElement() + "\n");
		buffer.append("initSegmenter:" + getInitSegmenter() + "\n");
		buffer.append("lockXtrans:" + isLockXtrans() + "\n");
		buffer.append("lock100:" + isLock100() + "\n");
		buffer.append("lock101:" + isLock101() + "\n");
		buffer.append("lockRepeated:" + isLockRepeated() + "\n");
		buffer.append("breakOnCRLF:" + isBreakOnCRLF() + "\n");
		buffer.append("embedSkl:" + isEmbedSkl() + "\n");
		buffer.append("--------------------------");
		return buffer.toString();
	}

	/**
	 * 正向转换验证
	 * @return ;
	 */
	public IStatus validateConversion() {
		if (source == null || source.trim().equals("")) {
			return ValidationStatus.error("请选择源文件。");
		} else {
			String localSource = ConverterUtil.toLocalPath(source);
			File file = new File(localSource);
			if (!file.exists()) {
				return ValidationStatus.error("源文件不存在。");
			}
		}
		if (target == null || target.trim().equals("")) {
			return ValidationStatus.error("请选择目标文件。");
		} else {
			if (!replaceTarget) { // 如果未选择覆盖，判断并提示目标文件是否存在
				String localTarget = ConverterUtil.toLocalPath(target);
				File file = new File(localTarget);
				if (file.exists()) {
					return ValidationStatus.error("目标文件已存在。");
				}
			}
		}
		if (srcLang == null || srcLang.trim().equals("")) {
			return ValidationStatus.error("请选择源文件的语言");
		}
		if (srcEncoding == null || srcEncoding.trim().equals("")) {
			return ValidationStatus.error("请选择源文件的编码。");
		}
		if (initSegmenter == null || initSegmenter.trim().equals("")) {
			return ValidationStatus.error("请选择分段规则文件。");
		} else {
			File file = new File(initSegmenter);
			if (!file.exists()) {
				return ValidationStatus.error("当前选择的分段规则文件不存在。");
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * 逆向转换验证
	 * @return ;
	 */
	public IStatus validateReverseConversion() {
		if (target == null || target.trim().equals("")) {
			return ValidationStatus.error("请选择已转换的文件。");
		} else {
			if (!replaceTarget) { // 如果未选择覆盖，判断并提示目标文件是否存在
				String localTarget = ConverterUtil.toLocalPath(target);
				File file = new File(localTarget);
				if (file.exists()) {
					return ValidationStatus.error("已转换的文件已存在。");
				}
			}
		}
		if (targetEncoding == null || targetEncoding.trim().equals("")) {
			return ValidationStatus.error("请选择已转换文件的编码。");
		}
		return Status.OK_STATUS;
	}

	public void setReplaceTarget(boolean replaceTarget) {
		this.replaceTarget = replaceTarget;
	}

	public boolean isReplaceTarget() {
		return replaceTarget;
	}

	public void setFileFormats(List<String> fileFormats) {
		this.fileFormats = fileFormats;
	}

	public List<String> getFileFormats() {
		if (fileFormats == null) {
			fileFormats = CommonFunctions.array2List(FileFormatUtils.getFileFormats());
		}
		return fileFormats;
	}

	public boolean isPreviewMode() {
		return previewMode;
	}

	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}
}
