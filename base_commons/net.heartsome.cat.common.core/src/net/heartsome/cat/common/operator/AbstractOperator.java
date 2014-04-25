package net.heartsome.cat.common.operator;

import java.util.Map;

public abstract class AbstractOperator {
	public abstract void undo();
	
	public abstract void redo();
	
	public abstract void exit();
	
	public abstract void setDocumentProperties(Map<String,Object> props);
	
	public abstract Map<String,Object> getDocumentProperties();
	
	public abstract void cut();
	
	public abstract void copy();
	
	public abstract void paste();
	
	
	

}
