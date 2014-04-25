package net.sourceforge.nattable.columnCategories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.util.ObjectUtils;

/**
 * Represents a Tree of Objects.<br/>
 * The Tree is represented as a single rootElement which points to a List<Node> of children.<br/>
 * 
 * Adapted from public domain code at http://sujitpal.blogspot.com/. 
 */
public class Tree implements Serializable {

	private static final long serialVersionUID = 6182L;
	private Node rootElement;

    /**
     * Default ctor.
     */
    public Tree() {
        super();
    }

    /**
     * Return the root Node of the tree.
     * @return the root element.
     */
    public Node getRootElement() {
        return this.rootElement;
    }

    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(Node rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * Returns the Tree as a List of Node objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<Node>.
     */
    public List<Node> toList() {
        List<Node> list = new ArrayList<Node>();
        walk(rootElement, list);
        return list;
    }

    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    @Override
	public String toString() {
        return toList().toString();
    }

    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference
     * as it recurses down the tree.
     * @param element the starting element.
     * @param list the output of the walk.
     */
    private void walk(Node element, List<Node> list) {
        list.add(element);
        for (Node data : element.getChildren()) {
            walk(data, list);
        }
    }

	/**
	 * Find the Node in the tree containing the supplied data.
	 * Stops searching at the first match. 
	 * @return matching Node if found, NULL otherwise
	 */
	public Node find(String nodeData) {
		return find(getRootElement(), nodeData);
	}

	/**
	 * Find a Node in a tree, containing the given data
	 *
	 * @param element Node to start searching at
	 * @param nodeData to search for
	 * @return matching Node if found, NULL otherwise
	 */
	public Node find(Node element, String nodeData) {
		if (nodeData.equals(element.getData())) {
			return element;
		}
		for (Node data : element.getChildren()) {
			Node result = find(data, nodeData);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void clear() {
		rootElement = null;
	}

	/**
	 * Removes the node with the supplied node data. Deletes the first matching node.
	 * @return TRUE if a node was found and removed
	 */
	public boolean remove(String nodeData) {
		Node nodeToRemove = find(nodeData);
		if (ObjectUtils.isNotNull(nodeToRemove)) {
			nodeToRemove.getParent().getChildren().remove(nodeToRemove);
			nodeToRemove.setParent(null);
			return true;
		}
		return false;
	}
}
