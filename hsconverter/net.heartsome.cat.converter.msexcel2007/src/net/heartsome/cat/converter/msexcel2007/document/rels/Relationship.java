/**
 * ReferenceRelationship.java
 *
 * Version information :
 *
 * Date:2012-8-1
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document.rels;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Relationship {
	private String id;
	private String type;
	private String target;

	public Relationship(String id, String type, String target) {
		this.id = id;
		this.type = type;
		this.target = target;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Relationship) {
			Relationship tempObj = (Relationship) obj;
			if (this.id.equals(tempObj.id) && this.type.equals(tempObj.getType())
					&& this.target.equals(tempObj.getTarget())) {
				return true;
			}
		} else {
			return super.equals(obj);
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
