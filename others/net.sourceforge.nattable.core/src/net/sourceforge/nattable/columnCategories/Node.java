package net.sourceforge.nattable.columnCategories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node of the Tree class. 
 */
public class Node implements Serializable {

	private static final long serialVersionUID = 7855L;

	public static enum Type {ROOT, COLUMN, CATEGORY, UNKNOWN};
	private Type type;
	private String data;
	private List<Node> children;
	private Node parent;

    public Node(String data) {
        this(data, Type.UNKNOWN);
    }

    public Node(String newCategoryName, Type type) {
    	setData(newCategoryName);
    	setType(type);
	}

	public Node getParent() {
    	return parent;
    }

    public void setParent(Node parent) {
		this.parent = parent;
	}
    
    public void setType(Type type) {
		this.type = type;
	}
    
    public Type getType() {
		return type;
	}

    /**
     * Return the children of Node. The Tree is represented by a single
     * root Node whose children are represented by a List<Node>. Each of
     * these Node elements in the List can have children. The getChildren()
     * method will return the children of a Node.
     * @return the children of Node
     */
    public List<Node> getChildren() {
        if (this.children == null) {
            return new ArrayList<Node>();
        }
        return this.children;
    }

    /**
     * Returns the number of immediate children of this Node.
     * @return the number of immediate children.
     */
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Adds a child to the list of children for this Node. The addition of
     * the first child will create a new List<Node>.
     * @param child a Node object to set.
     * @return Child node just added 
     */
    public Node addChild(Node child) {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        children.add(child);
        child.setParent(this);
        return child;
    }
    
	public Node addChildCategory(String categoryName) {
		return addChild(new Node(categoryName, Type.CATEGORY));
	}

	public void addChildColumnIndexes(int... columnIndexes) {
		for (int columnIndex : columnIndexes) {
			addChild(new Node(String.valueOf(columnIndex), Type.COLUMN));
		}
	}

    /**
     * Inserts a Node at the specified position in the child list. Will     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
     * @param index the position to insert at.
     * @param child the Node object to insert.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void insertChildAt(int index, Node child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
        }
    }

    /**
     * Remove the Node element at index index of the List<Node>.
     * @param index the index of the element to delete.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(type).append(",").append(getData().toString()).append(",[");
        int i = 0;
        for (Node e : getChildren()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(e.getData().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }
}
