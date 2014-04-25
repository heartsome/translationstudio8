/**
 * TranslationUnitAnalysResults.java
 *
 * Version information :
 *
 * Date:2012-12-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TranslationUnitAnalysisResults {

	private int maxTuNumber;

	private List<TranslationUnitAnalysisResult> results;

	private int sortStrategy;

	private String deffaultTm;

	public TranslationUnitAnalysisResults(String defaultTm, int sortStrategy, int maxTuNumber) {
		this.maxTuNumber = maxTuNumber;
		this.sortStrategy = sortStrategy;
		this.deffaultTm = defaultTm;
		results = new ArrayList<TranslationUnitAnalysisResult>();
	}

	/**
	 * Get the analysis results
	 * @return ;
	 */
	public List<TranslationUnitAnalysisResult> getAnaylysisResults(){
		return this.results;
	}
	/**
	 * add result
	 * @param tu
	 *            ;
	 */
	public void add(TranslationUnitAnalysisResult tu) {
		this.results.add(tu);
	}

	/**
	 * First sort by similarity,then sort by TM sort Strategy,{@link #sortStrategy} initialize by constructor
	 * @param sortStrategry
	 *            ;
	 */
	public void sort() {
		Collections.sort(this.results, sortComparator);
	}

	/**
	 * Clear current results, remove by max match size<br>
	 * Note: before execute this method, need sort first ;
	 */
	public void clearResults() {
		int index = results.size();
		while (index > maxTuNumber) {
			index--;
			results.remove(index);
		}
	}

	private Comparator<TranslationUnitAnalysisResult> sortComparator = new Comparator<TranslationUnitAnalysisResult>() {

		public int compare(TranslationUnitAnalysisResult a, TranslationUnitAnalysisResult b) {
			int a1 = a.getSimilarity();
			int b1 = b.getSimilarity();

			if (a1 < b1) {
				return 1;
			}
			return 0;
		}
	};
}
