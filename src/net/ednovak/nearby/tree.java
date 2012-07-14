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
	
	// Used to find Alice's rep set
	public tree rightLeaf(){
		tree cur = this;
		while (cur.right != null){
			cur = cur.right;
		}
		if (cur.value > 2117648){
			return null;
			//System.out.println("This 'leaf' is not really a leaf.");
		}
		return cur;
	}
	
	public tree leftLeaf(){
		tree cur = this;
		while (cur.left != null){
			cur = cur.left;
		}
		if (cur.value > 2117648){
			return null;
			//System.out.println("This 'leaf' is not really a leaf.");
		}
		return cur;
	}
	
	// Starts at the root in the top queue.  For each node in top queue, if it has 
	public treeQueue findRepSet(tree leftEnd, tree rightEnd, tree root){
		treeQueue answer = new treeQueue();
		treeQueue bottom = new treeQueue();
		treeQueue top = new treeQueue();
		top.push(root);
		while (top.length != 0){
			for (int i = 0; i < top.length; i++){
				tree cur = top.peek(i);
				// If a leaf is outside the span that it isn't in my tree and we'll see null
				if (cur.leftLeaf() == null || cur.rightLeaf() == null){
					if (cur.left != null){
						bottom.push(cur.left);
					}
					if (cur.right != null){
						bottom.push(cur.right);
					}
				}
				else{ answer.push(cur); } // The left and right leaf was within the bounds
			}
			top = bottom;
			bottom = new treeQueue();
		}
		return answer;
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