/**
 * PrefrenceParameters.java
 *
 * Version information :
 *
 * Date:2012-5-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation.bean;

import net.heartsome.cat.ts.machinetranslation.Activator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class PrefrenceParameters implements IPropertyChangeListener {

	/**
	 * goolge key
	 */
	private String goolgeKey ;
	/**
	 * goolge state
	 */
	private boolean googleState;

	private boolean bingState;

	private String bingId;

	private String bingKey;

	private boolean isAlwaysAccess;

	private boolean isManualAccess;

	private IPreferenceStore ps;

	private static PrefrenceParameters instance;

	public static PrefrenceParameters getInstance() {
		if (instance == null) {
			instance = new PrefrenceParameters();
		}
		return instance;
	}

	private PrefrenceParameters() {
		ps = Activator.getDefault().getPreferenceStore();
		ps.addPropertyChangeListener(this);
		loadPrefrence();
	}

	private void loadPrefrence() {
		if (ps != null) {
			googleState = ps.getBoolean(IPreferenceConstant.GOOGLE_STATE);
			goolgeKey = ps.getString(IPreferenceConstant.GOOGLE_KEY);

			bingId = ps.getString(IPreferenceConstant.BING_ID);
			bingKey = ps.getString(IPreferenceConstant.BING_KEY);
			bingState = ps.getBoolean(IPreferenceConstant.BING_STATE);

			isAlwaysAccess = ps.getBoolean(IPreferenceConstant.ALWAYS_ACCESS);
			isManualAccess = ps.getBoolean(IPreferenceConstant.MANUAL_ACCESS);

		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		loadPrefrence();
	}

	/**
	 * 返回访问API的策略, 没有指定访问策略则返回一个空串，如果有策略则返回对的首选项参数名称
	 * @return
	 */
	public String getAccessStrategy() {
		if (isAlwaysAccess) {
			return IPreferenceConstant.ALWAYS_ACCESS;
		}
		if (isManualAccess) {
			return IPreferenceConstant.MANUAL_ACCESS;
		}
		return "";
	}

	/**
	 * 是否总是访问API
	 * @return the isAlwaysAccess
	 */
	public boolean isAlwaysAccess() {
		return isAlwaysAccess;
	}

	/**
	 * 是否从不访问API
	 * @return the isNeverAccess
	 */
	public boolean isManualAccess() {
		return isManualAccess;
	}

	/** @return the goolgeKey */
	public String getGoolgeKey() {
		return goolgeKey;
	}

	/** @return the googleState */
	public boolean isGoogleState() {
		return googleState;
	}

	/** @return the bingState */
	public boolean isBingState() {
		return bingState;
	}

	/** @return the bingId */
	public String getBingId() {
		return bingId;
	}

	/** @return the bingKey */
	public String getBingKey() {
		return bingKey;
	}
	
	
}
