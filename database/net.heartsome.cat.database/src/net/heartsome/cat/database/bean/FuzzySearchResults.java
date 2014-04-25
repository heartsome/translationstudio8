/**
 * SearchResults.java
 *
 * Version information :
 *
 * Date:2012-11-27
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.DateUtils;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class FuzzySearchResults {

	private int maxTuNumber;

	private List<FuzzySearchResult> results;

	private int sortStrategy;

	private String deffaultTm;

	public FuzzySearchResults(String defaultTm, int sortStrategy, int maxTuNumber) {
		this.maxTuNumber = maxTuNumber;
		this.sortStrategy = sortStrategy;
		this.deffaultTm = defaultTm;
		results = new ArrayList<FuzzySearchResult>();
	}

	/**
	 * First sort by similarity,then sort by TM sort Strategy,{@link #sortStrategy} initialize by constructor
	 * @param sortStrategry
	 *            ;
	 */
	public void sort() {
		Collections.sort(this.results, sortComparator);
	}

	public void add(FuzzySearchResult tu) {
		this.results.add(tu);
	}

	public List<FuzzySearchResult> getSearchResult(){
		return this.results;
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

	/**
	 * Detecting current result set contains frs or not
	 * Compare source and target is difference
	 * @param tu
	 * @return ;
	 */
	public boolean contains(FuzzySearchResult frs) {
		TmxTU tu = frs.getTu();
		for (FuzzySearchResult _frs : results) {
			TmxTU _tu = _frs.getTu();
			if (_tu.getSource().getPureText().trim().equals(tu.getSource().getPureText().trim())
					&& (_tu.getTarget().getPureText().trim().equals(tu.getTarget().getPureText().trim()))) {
				return true;
			}
		}
		return false;
	}

	private Comparator<FuzzySearchResult> sortComparator = new Comparator<FuzzySearchResult>() {

		public int compare(FuzzySearchResult a, FuzzySearchResult b) {
			int a1 = a.getSimilarity();
			int b1 = b.getSimilarity();

			if (a1 < b1) {
				return 1;
			} else if (a1 == b1) {
				if (sortStrategy == TMPreferenceConstants.DEFAULT_DB_PRECEDENCE) {
					// sort by default database
					String dbName = b.getDbName();
					if (dbName.equals(deffaultTm)) {
						return -1;
					}
				} else if (sortStrategy == TMPreferenceConstants.DATE_REVERSE_PRECEDENCE) {
					// sort by update date
					String t1Str = a.getTu().getChangeDate();
					String t2Str = b.getTu().getChangeDate();
					if (t1Str.length() != 0 && t2Str.length() != 0) {
						Timestamp t1 = DateUtils.getTimestampFromUTC(t1Str);
						Timestamp t2 = DateUtils.getTimestampFromUTC(t2Str);
						return t2.compareTo(t1);

					} else if (t1Str.length() == 0 && t2Str.length() != 0) {
						return -1;
					}
				}
				return 0;
			} else {
				return -1;
			}
		}
	};
}
