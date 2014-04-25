package net.heartsome.cat.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TM DB Translation memory encapsulation
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmxTU {

	/** MTU PK */
	private int tmId;

	private String tuId;
	/*** TU属性值 */
	private List<TmxProp> props;
	private List<TmxNote> notes;
	private String creationUser;
	private String creationDate;
	private String changeDate;
	private String changeUser;
	private String creationTool;
	private String creationToolVersion;

	/** TU XMLElement Attributes */
	private Map<String, String> attributes;

	private TmxSegement source;
	private TmxSegement target;
	private List<TmxSegement> segments;
	private TmxContexts contexts;

	public TmxTU() {
	}

	public TmxTU(TmxSegement source, TmxSegement target) {
		this.source = source;
		this.target = target;
	}

	public TmxTU(String creationUser, String creationDate, String changeDate, String changeUser, TmxSegement source,
			TmxSegement target) {
		this.creationUser = creationUser;
		this.creationDate = creationDate;
		this.changeUser = changeUser;
		this.changeDate = changeDate;
		this.source = source;
		this.target = target;
	}

	/**
	 * Set the value of tmId MTU PK
	 * @param newVar
	 *            the new value of tmId
	 */
	public void setTmId(int newVar) {
		tmId = newVar;
	}

	/**
	 * Get the value of tmId MTU PK
	 * @return the value of tmId
	 */
	public int getTmId() {
		return tmId;
	}

	/**
	 * Set the value of attributeValues TU属性值(prop node)
	 * @param newVar
	 *            the new value of attributeValues
	 */
	public void setProps(List<TmxProp> newVar) {
		props = newVar;
	}

	/**
	 * check whether contain the Obj
	 * @param obj
	 * @return ;
	 */
	public boolean isContainsProp(TmxProp obj) {
		if (props == null || props.size() == 0) {
			return false;
		}
		if (props.contains(obj)) {
			return true;
		}
		return false;
	}

	/**
	 * Append a new Prop node
	 * @param newProp
	 *            ;
	 */
	public void appendProp(TmxProp newProp) {
		if (props == null) {
			props = new ArrayList<TmxProp>();
		}
		if (!props.contains(newProp)) {
			props.add(newProp);
		}
	}

	/**
	 * Get the value of attributeValues TU属性值
	 * @return the value of attributeValues
	 */
	public List<TmxProp> getProps() {
		return props;
	}

	/**
	 * Get the prop by prop node attribute value
	 * @param type
	 *            Node Attribute value
	 * @return did not find return null;
	 */
	public TmxProp getPropByType(String type) {
		if (props == null || props.size() == 0) {
			return null;
		}
		for (TmxProp prop : props) {
			if (prop.getName().equals(type)) {
				return prop;
			}
		}
		return null;
	}

	/** @return the notes */
	public List<TmxNote> getNotes() {
		return notes;
	}

	/**
	 * Append a new Note node
	 * @param note
	 *            ;
	 */
	public void appendNote(TmxNote note) {
		if (notes == null) {
			notes = new ArrayList<TmxNote>();
		}
		if (!notes.contains(note)) {
			notes.add(note);
		}
	}

	/**
	 * @param notes
	 *            the notes to set
	 */
	public void setNotes(List<TmxNote> notes) {
		this.notes = notes;
	}

	/**
	 * Set the value of creationUser
	 * @param newVar
	 *            the new value of creationUser
	 */
	public void setCreationUser(String newVar) {
		creationUser = newVar;
	}

	/**
	 * Get the value of creationUser
	 * @return the value of creationUser
	 */
	public String getCreationUser() {
		return creationUser;
	}

	/**
	 * Set the value of creationDate
	 * @param newVar
	 *            the new value of creationDate
	 */
	public void setCreationDate(String newVar) {
		creationDate = newVar;
	}

	/**
	 * Get the value of creationDate
	 * @return the value of creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * Set the value of changeDate
	 * @param newVar
	 *            the new value of changeDate
	 */
	public void setChangeDate(String newVar) {
		changeDate = newVar;
	}

	/**
	 * Get the value of changeDate
	 * @return the value of changeDate
	 */
	public String getChangeDate() {
		return changeDate;
	}

	/**
	 * Set the value of changeUser
	 * @param newVar
	 *            the new value of changeUser
	 */
	public void setChangeUser(String newVar) {
		changeUser = newVar;
	}

	/**
	 * Get the value of changeUser
	 * @return the value of changeUser
	 */
	public String getChangeUser() {
		return changeUser;
	}

	/**
	 * Set the value of source
	 * @param newVar
	 *            the new value of source
	 */
	public void setSource(TmxSegement newVar) {
		source = newVar;
	}

	/**
	 * Get the value of source
	 * @return the value of source
	 */
	public TmxSegement getSource() {
		return source;
	}

	/**
	 * Set the value of target
	 * @param newVar
	 *            the new value of target
	 */
	public void setTarget(TmxSegement newVar) {
		target = newVar;
	}

	/**
	 * Get the value of target
	 * @return the value of target
	 */
	public TmxSegement getTarget() {
		return target;
	}

	public List<TmxSegement> getSegments() {
		return segments;
	}

	public void setSegments(List<TmxSegement> segments) {
		this.segments = segments;
	}

	public void appendSegement(TmxSegement segment) {
		if (segments == null) {
			segments = new ArrayList<TmxSegement>();
		}
		segments.add(segment);
	}

	/** @return the tuId */
	public String getTuId() {
		return tuId;
	}

	/**
	 * @param tuId
	 *            the tuId to set
	 */
	public void setTuId(String tuId) {
		this.tuId = tuId;
	}

	/** @return the createtionTool */
	public String getCreationTool() {
		return creationTool;
	}

	/**
	 * @param createtionTool
	 *            the createtionTool to set
	 */
	public void setCreationTool(String createtionTool) {
		this.creationTool = createtionTool;
	}

	/** @return the creationToolVersion */
	public String getCreationToolVersion() {
		return creationToolVersion;
	}

	/**
	 * @param creationToolVersion
	 *            the creationToolVersion to set
	 */
	public void setCreationToolVersion(String creationToolVersion) {
		this.creationToolVersion = creationToolVersion;
	}

	/** @return the attributes */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void appendAttribute(String name, String value) {
		if (this.attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attributes.put(name, value);
	}

	/** @return the contexts */
	public TmxContexts getContexts() {
		return contexts;
	}

	/**
	 * @param contexts
	 *            the contexts to set
	 */
	public void setContexts(TmxContexts contexts) {
		this.contexts = contexts;
	}

	/**
	 * @param contextName
	 *            @see {@link TmxContexts#PRE_CONTEXT_NAME} {@link TmxContexts#NEXT_CONTEXT_NAME}
	 * @param contextVal
	 *            ;
	 */
	public void appendContext(String contextName, String contextVal) {
		if (contextVal == null || contextVal.length() == 0) {
			return;
		}
		if (this.contexts == null) {
			contexts = new TmxContexts();
		}
		if (contextName.equals(TmxContexts.PRE_CONTEXT_NAME)) {
			contexts.appendPreContext(contextVal);
		} else if (contextName.equals(TmxContexts.NEXT_CONTEXT_NAME)) {
			contexts.appendNextContext(contextVal);
		}

	}
}
