package net.heartsome.cat.converter.mif.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MifParseBuffer {
	/** key:id,value:Frame */
	private Map<String, Frame> fms;
	/** key:id,value:Table */
	private Map<String, Table> tbls;
	private List<Page> pages;
	/** key:reference id,value:TextFlow */
	private Map<String, TextFlow> tfs;

	private List<Marker> markers;
	
	public MifParseBuffer() {
		fms = new HashMap<String, Frame>();
		tbls = new HashMap<String, Table>();
		pages = new ArrayList<Page>();
		tfs = new HashMap<String, TextFlow>();
		markers = new ArrayList<Marker>();
	}

	public void appendFrame(Frame fm) {
		fms.put(fm.getId(), fm);
	}

	public void appendTbale(Table tbl) {
		tbls.put(tbl.getId(), tbl);
	}

	public void appendPage(Page page) {
		pages.add(page);
	}

	public void appendTextFlow(TextFlow tf) {
		tfs.put(tf.getTextRectId(), tf);
	}
	
	public void appendMarker(Marker m){
		markers.add(m);
	}

	public Frame getFrame(String id) {
		return fms.get(id);
	}

	public Table getTable(String id) {
		return tbls.get(id);
	}

	public List<Page> getPages() {
		return pages;
	}

	public TextFlow getTextFlow(String id) {
		return tfs.get(id);
	}
	
	public List<Marker> getMarkers(){
		return markers;
	}
}
