package net.heartsome.cat.converter.mif.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TextRect {
	private String id;
	private String vPosition;
	
	public boolean validate(){
		if(id == null || id.equals("")){
			return false;
		}
		if(vPosition == null || vPosition.equals("")){
			return false;
		}
		return true;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getvPosition() {
		return vPosition;
	}
	public void setvPosition(String vPosition) {
		this.vPosition = vPosition;
	}
	
	public static void main(String[] args){
//		List<String> test = new ArrayList<String>();
//		test.add("0.58527");
//		test.add("1.31806");
//		test.add("0.42088");
//		
//		Collections.sort(test);
//		
//		System.out.println(test);
		
		List<TextRect> textRects = new ArrayList<TextRect>();
		TextRect t1 = new TextRect();
		t1.setId("1");
		t1.setvPosition("0.58527");
		textRects.add(t1);
		t1 = new TextRect();
		t1.setId("2");
		t1.setvPosition("1.31806");
		textRects.add(t1);
		t1 = new TextRect();
		t1.setId("3");
		t1.setvPosition("0.42088");
		textRects.add(t1);
		
		Collections.sort(textRects, new Comparator<TextRect>() {

			public int compare(TextRect o1, TextRect o2) {
				String p1 = o1.getvPosition();
				String p2 = o2.getvPosition();
				return p1.compareTo(p2);
			}
		});
		
		for(TextRect t : textRects){
			System.out.print(t.getId());
			System.out.println(t.getvPosition());
		}
		
	}
	
}
