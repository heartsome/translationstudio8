package net.heartsome.cat.common.innertag.factory;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;

public class DefaultPlaceHolderBuilder implements IPlaceHolderBuilder {

	public String getPlaceHolder(List<InnerTagBean> innerTagBeans, int index) {
		return "";
	}

	public int getIndex(List<InnerTagBean> innerTagBeans, String placeHolder) {
		return -1;
	}

}
