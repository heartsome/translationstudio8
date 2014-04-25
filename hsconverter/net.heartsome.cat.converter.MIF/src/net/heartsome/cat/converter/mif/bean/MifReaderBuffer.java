package net.heartsome.cat.converter.mif.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MifReaderBuffer {

	private List<Object[]> cache;

	public MifReaderBuffer() {
		cache = new ArrayList<Object[]>();
	}

	/**
	 * 
	 * @param obj
	 *            obj[0] must contains the index。
	 */
	public void addBuffer(Object[] obj) {
		cache.add(obj);
	}

	public List<Object[]> getBuffer(Comparator<Object[]> cp) {
		Collections.sort(cache, cp);
		return cache;
	}
}
