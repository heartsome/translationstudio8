package net.sourceforge.nattable.columnCategories;

import static net.sourceforge.nattable.util.ObjectUtils.isNotNull;

import java.io.Serializable;

import net.sourceforge.nattable.columnCategories.Node.Type;

public class ColumnCategoriesModel implements Serializable {

	private static final long serialVersionUID = 4550L;

	/** Tree of the column category names */
	private final Tree tree = new Tree();

	public Node addRootCategory(String rootCategoryName) {
		if(isNotNull(tree.getRootElement())){
			throw new IllegalStateException("Root has been set already. Clear using (clear()) to reset.");
		}
		Node root = new Node(rootCategoryName, Type.ROOT);
		tree.setRootElement(root);
		return root;
	}

	public Node addCategory(Node parentCategory, String newCategoryName){
		if(tree.getRootElement() == null){
			throw new IllegalStateException("Root node must be set (using addRootNode()) before children can be added");
		}
		Node newNode = new Node(newCategoryName, Node.Type.CATEGORY);
		parentCategory.addChild(newNode);
		return newNode;
	}

	public void addColumnsToCategory(Node parentCategory, int... columnIndexes){
		if(parentCategory.getType() != Type.CATEGORY){
			throw new IllegalStateException("Columns can be added to a category node only.");
		}
		
		for (Integer columnIndex : columnIndexes) {
			parentCategory.addChild(new Node(String.valueOf(columnIndex), Type.COLUMN));
		}
	}

	public void removeColumnIndex(Integer hiddenColumnIndex) {
		tree.remove(String.valueOf(hiddenColumnIndex));
	}
	
	public Node getRootCategory() {
		return tree.getRootElement();
	}

	@Override
	public String toString() {
		return tree.toString();
	}

	public void dispose() {
		tree.clear();
	}

	public void clear() {
		tree.clear();
	}
	
}
