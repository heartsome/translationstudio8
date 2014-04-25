/**
 * ImportTmxTemp.java
 *
 * Version information :
 *
 * Date:2013-1-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ImportTmx {
	public static final Logger logger = LoggerFactory.getLogger(ImportTmx.class);
	/** 导入策略 */
	private int importStrategy;

	private DBOperator db;

	private TmxReader reader;

	private List<TmxTU> tmxTuCache;

	private final int cacheSize = 200;

	private IProgressMonitor monitor;

	public ImportTmx(DBOperator db, int importStrategy, IProgressMonitor monitor) {
		if (monitor == null) {
			this.monitor = new NullProgressMonitor();
		} else {
			this.monitor = monitor;
		}
		this.db = db;
		this.importStrategy = importStrategy;
		tmxTuCache = new ArrayList<TmxTU>(cacheSize);
	}

	public void importTmxContent(String content) throws ImportException {
		try {
			reader = new TmxReader(content);
			String srcLang = reader.getTmxHeader().getSrclang();
			if (srcLang == null || srcLang.equals("*all*") || srcLang.equals("")) {
				// 从TMX中读取不到源语言,此时只能执行增加
				importStrategy = Constants.IMPORT_MODEL_ALWAYSADD;
			}
		} catch (TmxReadException e) {
			throw new ImportException(e.getMessage());
		}
		doImport();
	}

	public void importTmxFile(File tmxFile) throws ImportException {
		try {
			reader = new TmxReader(tmxFile);
			reader.tryToClearTags(true);
			String srcLang = reader.getTmxHeader().getSrclang();
			if (srcLang == null || srcLang.equals("*all*") || srcLang.equals("")) {
				// 从TMX中读取不到源语言,此时只能执行增加
				importStrategy = Constants.IMPORT_MODEL_ALWAYSADD;
			}
		} catch (TmxReadException e) {
			throw new ImportException(e.getMessage());
		}
		doImport();
	}

	private void doImport() throws ImportException {
		int totalTu = reader.getTotalTu();
		if (totalTu == 0) {
			throw new ImportException(Messages.getString("document.TmxReader.readTuMsg"));
		}
		String monitorMsg = Messages.getString("document.ImportTmx.task1");
		monitor.beginTask(monitorMsg, totalTu * 2);
		TmxReaderEvent event = null;
		while ((event = reader.read()) != null) {
			if (event.getState() == TmxReaderEvent.END_FILE) {
				// end file
				break;
			} else if (event.getState() == TmxReaderEvent.ERROR_TU) {
				// error TU
				continue;
			} else if (event.getState() == TmxReaderEvent.READ_EXCEPTION) {
				// read TU throw a exception
				continue;
			} else if (event.getState() == TmxReaderEvent.NORMAL_READ) {
				if (monitor != null && monitor.isCanceled()) {
					monitor.setTaskName(Messages.getString("document.ImportTmx.msg1"));
					throw new ImportException(Messages.getString("document.ImportTmx.canceledImport"));
				}
				// normal TU returned
				if (cacheSize == tmxTuCache.size()) {
					try {
						flush();
					} catch (SQLException e) {
						throw new ImportException(Messages.getString("document.ImportTmx.dbOpError") + e.getMessage());
					}
				}
				tmxTuCache.add(event.getTu());
				monitor.worked(1);
				if (monitor != null && monitor.isCanceled()) {
					monitor.setTaskName(Messages.getString("document.ImportTmx.msg1"));
					throw new ImportException(Messages.getString("document.ImportTmx.canceledImport"));
				}
			}
		}
		if (monitor != null && monitor.isCanceled()) {
			monitor.setTaskName(Messages.getString("document.ImportTmx.msg1"));
			throw new ImportException(Messages.getString("document.ImportTmx.canceledImport"));
		}
		try {
			flush();
		} catch (SQLException e) {
			throw new ImportException(Messages.getString("document.ImportTmx.dbOpError") + e.getMessage());
		}
		monitor.done();
	}

	private void flush() throws SQLException {
		if (importStrategy == Constants.IMPORT_MODEL_ALWAYSADD) { // 始终增加
			// transaction control
			// long l = System.currentTimeMillis();
			db.beginTransaction();
			try {
				for (TmxTU tu : tmxTuCache) {
					addTu(tu);
					monitor.worked(1);
					if (monitor != null && monitor.isCanceled()) {
						break;
					}
				}
			} catch (SQLException e) {
				db.rollBack();
				throw e;
			}
			db.commit();
			tmxTuCache.clear();
			// System.out.println((System.currentTimeMillis() - l));
		} else if (importStrategy == Constants.IMPORT_MODEL_OVERWRITE) {
			Map<TmxTU, List<TmxTU>> duplicateTuCache = new HashMap<TmxTU, List<TmxTU>>();
			// Map<TmxTU, List<TmxSegement>> duplicateTuvPkCache = new HashMap<TmxTU, List<TmxSegement>>();
			// long l2 = System.currentTimeMillis();
			List<TmxTU> filterSrcSameTu = filterSrcSameTu(tmxTuCache,Constants.IMPORT_MODEL_OVERWRITE);			
			for (TmxTU tu : filterSrcSameTu) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}
				// long l1 = System.currentTimeMillis();
				List<TmxTU> dbTus = checkDuplicate(tu);
				if (dbTus == null) { // error TU
					continue;
				}
				// cached the DB exist TU
				duplicateTuCache.put(tu, dbTus);
				if (dbTus.size() != 0) {
					// long l = System.currentTimeMillis();
					for (TmxTU dbTu : dbTus) {
						List<TmxSegement> tuvs = tu.getSegments();
						List<TmxSegement> dbTuvs = new ArrayList<TmxSegement>();
						for (TmxSegement tuv : tuvs) {
							dbTuvs.addAll(db.getTextDataIdByGroupIdLang(dbTu.getTmId(), "M", tuv.getLangCode()));
						}
						dbTu.setSegments(dbTuvs);
						List<TmxProp> dbProps = db.getTuMprops(dbTu.getTmId(), "TU");
						dbTu.setProps(dbProps);
						List<TmxNote> dbNotes = db.getTuMNote(dbTu.getTmId(), "TU");
						dbTu.setNotes(dbNotes);
						// duplicateTuvPkCache.put(dbTu, dbTuvs);
					}
					// System.out.println("getTextDataidByGroup："+ (System.currentTimeMillis() - l));
				}
				// System.out.println("tu check with db："+ (System.currentTimeMillis() - l1));
			}
			// System.out.println("200 tu check with db："+ (System.currentTimeMillis() - l2));
			tmxTuCache.clear();
			filterSrcSameTu.clear();
			// Transaction control
			db.beginTransaction();
			try {
				Iterator<TmxTU> tuIt = duplicateTuCache.keySet().iterator();
				while (tuIt.hasNext()) {
					if (monitor != null && monitor.isCanceled()) {
						break;
					}
					TmxTU tu = tuIt.next();
					List<TmxTU> dbTus = duplicateTuCache.get(tu);
					if (dbTus.size() == 0) {
						addTu(tu);
					} else {
						for (TmxTU dbTu : dbTus) {
							String changeDate = tu.getChangeDate() == null ? "" : tu.getChangeDate();
							String changeId = tu.getChangeUser() == null ? "" : tu.getChangeUser();

							String dbChangeDate = dbTu.getChangeDate() == null ? "" : dbTu.getChangeDate();
							String dbChangeId = dbTu.getChangeUser() == null ? "" : dbTu.getChangeUser();

							// Update TU Attribute (XmlElement Attribute)
							if (!changeDate.equals(dbChangeDate) || !changeId.equals(dbChangeId)) {
								db.updateTuChangeInfo(dbTu.getTmId(), tu.getTuId(), changeId, changeDate);
							}

							// Update Prop
							List<TmxProp> props = tu.getProps();
							List<TmxProp> dbProps = dbTu.getProps();
							if (dbProps.size() == 0 && props != null) {
								for (TmxProp _prop : props) {
									db.insertTMXProp(dbTu.getTmId(), "TU", _prop.getName(), null, null,
											_prop.getValue());
								}
							} else if (props != null) {
								for (TmxProp prop : props) {
									boolean exist = false;
									for (TmxProp dbProp : dbProps) {
										if (prop.equals(dbProp)) {
											exist = true;
											break;
										}
									}
									if (!exist) {
										db.deleteMprop("TU", dbTu.getTmId() + "");
										for (TmxProp _prop : props) {
											db.insertTMXProp(dbTu.getTmId(), "TU", _prop.getName(), null, null,
													_prop.getValue());
										}
										break;
									}
								}
							}

							// Update Note
							List<TmxNote> notes = tu.getNotes();
							List<TmxNote> dbNotes = dbTu.getNotes();
							if (dbNotes.size() == 0 && notes != null) {
								for (TmxNote note : notes) {
									db.insertTMXNote(dbTu.getTmId(), "TU", note.getContent(), null, null, null, null,
											note.getEncoding(), note.getXmlLang());
								}
							} else if (notes != null) {
								for (TmxNote note : notes) {
									boolean exist = false;
									for (TmxNote dbNote : dbNotes) {
										if (note.equals(dbNote)) {
											exist = true;
											break;
										}
									}
									if (!exist) {
										db.deleteMNote("TU", dbTu.getTmId() + "");
										for (TmxNote _note : notes) {
											db.insertTMXNote(dbTu.getTmId(), "TU", _note.getContent(), null, null,
													null, null, _note.getEncoding(), _note.getXmlLang());
										}
										break;
									}
								}
							}

							// Update TUVS except source TUV
							List<TmxSegement> tuvs = tu.getSegments();
							if (tuvs != null) {
								for (TmxSegement tuv : tuvs) {
									List<TmxSegement> dbTuvs = dbTu.getSegments();
									String lang = tuv.getLangCode();
									String content = tuv.getFullText();
									if (content == null) {
										continue;
									}
									if (dbTuvs.size() != 0) {
										boolean flg = false;
										boolean isDuplicate = false;
										for (TmxSegement dbTuv : dbTuvs) {
											if (dbTuv.getLangCode().equalsIgnoreCase(lang)) {
												String dbContent = dbTuv.getFullText();
												if (!dbContent.equals(content)) {
													db.deleteAllTuvRelations(
															Arrays.asList(new Integer[] { dbTuv.getDbPk() }), lang);
													flg = true;
												}
												isDuplicate = true;
											}
										}
										if (flg == true || !isDuplicate) {
											addTuv(dbTu.getTmId(), tuv, null, null);
										}
									} else {
										addTuv(dbTu.getTmId(), tuv, null, null);
									}
								}
							}

							// Update context
							TmxContexts dbContexts = dbTu.getContexts();
							if (tu.getContexts() != null) {
								String preContext = tu.getContexts().getPreContext();
								String nextContext = tu.getContexts().getNextContext();
								if (dbContexts == null) {
									db.updateTuvContext(dbTu.getTmId(), tu.getSource().getLangCode(), preContext,
											nextContext);
								} else {
									String dbPreContext = dbContexts.getPreContext();
									String dbNextContext = dbContexts.getNextContext();

									if (!dbPreContext.equals(preContext) || !dbNextContext.equals(nextContext)) {
										db.updateTuvContext(dbTu.getTmId(), tu.getSource().getLangCode(), preContext,
												nextContext);
									}
								}
							}
						}
					}
					monitor.worked(1);
				}
			} catch (SQLException e) {
				db.rollBack();
				throw e;
			}
			db.commit();
			duplicateTuCache.clear();
		} else if (importStrategy == Constants.IMPORT_MODEL_IGNORE) {
			List<TmxTU> needAddTus = new ArrayList<TmxTU>();
			List<TmxTU> filterSrcSameTu = filterSrcSameTu(tmxTuCache, Constants.IMPORT_MODEL_IGNORE);
			for (TmxTU tu : filterSrcSameTu) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}
				List<TmxTU> dbTus = checkDuplicate(tu);
				if (dbTus == null) { // error TU
					continue;
				}
				// cached the DB exist TU
				if (dbTus.size() == 0) {
					needAddTus.add(tu);
				}
			}
			tmxTuCache.clear();
			filterSrcSameTu.clear();
			// Transaction control
			db.beginTransaction();
			try {
				for (TmxTU tu : needAddTus) {
					if (monitor != null && monitor.isCanceled()) {
						break;
					}
					addTu(tu);
					monitor.worked(1);
				}
			} catch (SQLException e) {
				db.rollBack();
				throw e;
			}
			db.commit();
			needAddTus.clear();
		}
	}

	private List<TmxTU> checkDuplicate(TmxTU tu) throws SQLException {
		TmxSegement srcTuv = tu.getSource();
		List<TmxSegement> tuvs = tu.getSegments();
		if (srcTuv == null || tuvs == null || tu.getSegments().size() == 0) {
			return null;
		}
		String pureText = srcTuv.getPureText();
		if (pureText == null) {
			return null;
		}
		int hash = pureText.hashCode();
		String tuId = tu.getTuId();
		if (tuId == null || tuId.equals("")) {
			tuId = generateTuId();
			tu.setTuId(tuId);
		}
		// long l = System.currentTimeMillis();
		List<TmxTU> dbTus = db.getTUInfoByTuvInfo(hash, Utils.convertLangCode(srcTuv.getLangCode()), tuId);
		// System.out.println("checkDuplicate tu: "+ (System.currentTimeMillis() - l));
		return dbTus;
	}

	private boolean addTu(TmxTU tu) throws SQLException {
		TmxSegement srcTuv = tu.getSource();
		List<TmxSegement> tuvs = tu.getSegments();
		if ((srcTuv == null && tuvs == null) || (srcTuv == null && tuvs.size() < 2)) {
			// check TU
			return false;
		}
		String tuId = tu.getTuId();
		if (tuId == null || tuId.equals("")) {
			tuId = generateTuId();
		}
		int tuPk = db.insertTU(0, tuId, tu.getCreationUser(), tu.getCreationDate(), tu.getChangeUser(),
				tu.getChangeDate(), tu.getCreationTool(), tu.getCreationToolVersion(), null, null, null);
		List<TmxNote> notes = tu.getNotes();
		if (notes != null && notes.size() != 0) {
			for (TmxNote note : notes) {
				db.insertTMXNote(tuPk, "TU", note.getContent(), null, null, null, null, note.getEncoding(),
						note.getXmlLang());
			}
		}
		List<TmxProp> props = tu.getProps();
		if (props != null && props.size() != 0) {
			for (TmxProp prop : props) {
				db.insertTMXProp(tuPk, "TU", prop.getName(), null, null, prop.getValue());
			}
		}

		String preContext = null;
		String nextContext = null;
		if (tu.getContexts() != null) {
			preContext = tu.getContexts().getPreContext();
			nextContext = tu.getContexts().getNextContext();
		}
		if (srcTuv != null) {
			addTuv(tuPk, srcTuv, preContext, nextContext);
			preContext = null;
			nextContext = null;
		}
		if (tuvs != null) {
			for (TmxSegement tuv : tuvs) {
				addTuv(tuPk, tuv, preContext, nextContext);
			}
		}
		return true;
	}

	private void addTuv(int tuPk, TmxSegement tuv, String preContext, String nextContext) throws SQLException {
		String pureText = tuv.getPureText();
		String hash = pureText == null ? null : pureText.hashCode() + "";
		try {
			db.insertTextData("M", tuPk, hash, pureText, tuv.getFullText(), Utils.convertLangCode(tuv.getLangCode()),
					preContext == null ? "" : preContext, nextContext == null ? "" : nextContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 按“时间戳与随机五位数”规则生成TUID
	 * @return ;
	 */
	private String generateTuId() {
		return System.currentTimeMillis() + Utils.validateCode(5);
	}

	/**
	 * @param cache
	 * @param importStrategy
	 * @return ;
	 */
	private List<TmxTU> filterSrcSameTu(List<TmxTU> cache, int importStrategy) {
		if (null == cache || cache.isEmpty()) {
			return cache;
		}
		Map<String, List<TmxTU>> map = new HashMap<String, List<TmxTU>>();
		String srcText = "";
		for (TmxTU tu : cache) {
			srcText = tu.getSource().getPureText();
			if (map.get(srcText) != null) {
				map.get(srcText).add(tu);
			} else {
				List<TmxTU> temp = new ArrayList<TmxTU>();
				map.put(srcText, temp);
				temp.add(tu);
			}
		}
		List<TmxTU> rs = new ArrayList<TmxTU>();
		Iterator<List<TmxTU>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			List<TmxTU> next = iterator.next();			
			if(importStrategy == Constants.IMPORT_MODEL_IGNORE){				
				rs.add(next.get(0));
			}else{
				rs.add(next.get(next.size()-1));
			}
		}
		return rs;

	}
}
