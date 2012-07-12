package net.ednovak.nearby;

public class treeNode{

	public int levelIndex;
	public double value;
	public boolean isLeaf;
	public String special;
	
	public treeNode(int newLevelIndex, double newValue, boolean newIsLeaf){
		levelIndex = newLevelIndex;
		value = newValue;
		isLeaf = newIsLeaf;
		special = null;
	}
	
	@Override
	public String toString(){
		String s = "Leaf: " + isLeaf + "   value: " + value + "   levelIndex: " + levelIndex;
		if (special != null){
			s += "   special: " + special;
		}
		return s;
		
	}
}