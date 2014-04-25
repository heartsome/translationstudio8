package net.heartsome.cat.ts.ui.plugin;

/**
 * TBXMaker 插件中列属性的 Bean 类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ColProperties {
	String colName;
	public String level;
	String language;
	String propName;
	public String propType;

	public static String noteName = "note";  //$NON-NLS-1$
	public static String descripName = "descrip";  //$NON-NLS-1$
	public static String termNoteName = "termNote";  //$NON-NLS-1$
	public static String termName = "term";  //$NON-NLS-1$
	
	public static String conceptLevel = "Concept";  //$NON-NLS-1$
	public static String langLevel = "Term";  //$NON-NLS-1$
		
	public ColProperties(String pColName){
		colName = pColName;
		level = conceptLevel;
		propName = descripName;
		propType = ""; //$NON-NLS-1$
		language = ""; //$NON-NLS-1$		
	}
	
	public void setColumnType(String plevel, String pLang, String pName, String pType){
		level = plevel;		
		language = pLang;
		propName = pName;
		propType = pType;
		colName = pName + " ("+pLang+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getLanguage() {
		return language;
	}

	public String getLevel() {
		return level;
	}
	
	public String getPropName() {
		return propName;
	}

	public String getPropType() {
		return propType;
	}
	
	public String getColName() {
		return colName;
	}
	
}
