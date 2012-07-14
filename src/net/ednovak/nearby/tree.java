package net.ednovak.nearby;

public class tree{

	public int value; // All nodes have values
	public  char[] path; // Tells the alg if it's parent is to the left or right
	public String special; // If it's alice or Bob or something special
	public tree left; // Leaves have null for left and right  // some other nodes might have \\
	public tree right; // Leave have null for left and right  // null for these values       \\
	public tree parent; // Root node has null for parent
	
	public tree(int newValue, char[] newPath, tree newLeft, tree newRight){
		value = newValue; 
		path = newPath;
		special = null; 
		left = newLeft; 
		right = newRight; 
	}
	
	@Override
	public String toString(){
		String s = "--Node--\nvalue: " + value + "\nmap: " + new String(path);
		if (special != null){
			s += "\nspecial: " + special;
		}
		if (left != null){
			s += "\nleft subtree: " + left.value;
		}
		if (right != null){
			s += "\nright subtree: " + right.value;
		}
		if (parent != null){
			s += "\nparent: " + parent.value;
		}
		return s;		
	}
}