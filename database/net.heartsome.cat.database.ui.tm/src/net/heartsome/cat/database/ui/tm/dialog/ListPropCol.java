package net.heartsome.cat.database.ui.tm.dialog;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.PropCol;

/**
 * 相关搜索的列需要用到的类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ListPropCol extends PropCol {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ListPropCol.class.getName());

	private int index;

	public ListPropCol(String id, String label, String prop, int index) {
		super(id, label, prop);
		this.index = index;
	}

	@SuppressWarnings("rawtypes")
	public Object getValue(IRow row) {
		if (row != null) {
			try {
				Object base = row;
				for (int i = 0; i < _propPath.length; i++) {
					String propName = _propPath[i];
					Method getter = base.getClass().getMethod("get" + propName, new Class[] {});
					base = getter.invoke(base, new Object[] {});
					if (index >= 0 && base instanceof List) {
						List list = (List) base;
						base = list.get(index);
					}
				}
				if (_accessor == null) {
					return base;
				} else {
					return _accessor.getValue(base);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 处理添加/删除标记以及保存修改后的值（只保存在表格中）
	 * @see de.jaret.util.ui.table.model.PropCol#setValue(de.jaret.util.ui.table.model.IRow, java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public void setValue(IRow row, Object value) {
		Object oldValue = getValue(row);
		if (isRealModification(oldValue, value)) {
			try {
				Object base = row;
				for (int i = 0; i < _propPath.length - 1; i++) {
					String propName = _propPath[i];
					Method getter = base.getClass().getMethod("get" + propName, new Class[] {});
					base = getter.invoke(base, new Object[] {});

				}
				if (_accessor == null) {
					Class<?> clazz;
					if (value == null) {
						clazz = getContentClass(row);
					} else {
						if (value instanceof Enum) {
							clazz = ((Enum) value).getDeclaringClass();
						} else {
							clazz = value.getClass();
						}
						if (clazz.equals(Boolean.class)) {
							clazz = Boolean.TYPE;
						} else if (clazz.equals(Integer.class)) {
							clazz = Integer.TYPE;
						} else if (clazz.equals(Double.class)) {
							clazz = Double.TYPE;
						}
					}
					if (index >= 0 && base instanceof XPropRow) {
						XPropRow propRow = (XPropRow) base;
						List<String> list = propRow.getLstTarget();
						list.set(index, (String) value);
						Method setter = base.getClass().getMethod("set" + _propPath[_propPath.length - 1],
								new Class[] { list.getClass() });
						setter.invoke(base, new Object[] { list });
					} else {
//						添加/删除标记
						if (index < 0 && base instanceof XPropRow) {
							XPropRow propRow = (XPropRow) base;
							HashMap<String, String> map = (HashMap<String, String>) propRow.getDataMap();
							boolean blnIsAddTag = (Boolean)value;
							MetaData metaData = (MetaData) propRow.getData("metaData");
							DBOperator dbop = DatabaseService.getDBOperator(metaData);
							try {
								dbop.start();
								dbop.addOrRemoveFlag(blnIsAddTag, map.get("id"));
								dbop.commit();
							} catch (SQLException e1) {
								try {
									dbop.rollBack();
								} catch (SQLException e) {
									String text = blnIsAddTag ? Messages.getString("dialog.ListPropCol.logger1") : Messages.getString("dialog.ListPropCol.logger2");
									LOGGER.error(text + Messages.getString("dialog.ListPropCol.logger3"), e);
								}
							} catch (ClassNotFoundException e1) {
								try {
									dbop.rollBack();
								} catch (SQLException e) {
									LOGGER.error(Messages.getString("dialog.ListPropCol.logger4"), e);
								}
							} finally{
								try {
									if (dbop != null) {
										dbop.end();
									}
								} catch (SQLException e) {
									LOGGER.error("",e);
								}
							}
						}
						Method setter = base.getClass().getMethod("set" + _propPath[_propPath.length - 1],
								new Class[] { clazz });
						setter.invoke(base, new Object[] { value });
					}
				} else {
					_accessor.setValue(base, value);
				}
				fireValueChanged(row, this, oldValue, value);
			} catch (Exception e) {
				LOGGER.error("", e);
				e.printStackTrace();
				throw new RuntimeException("Could not set value " + e.getLocalizedMessage());
			}
		}
	}
}
