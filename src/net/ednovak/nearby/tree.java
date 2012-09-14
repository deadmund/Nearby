package net.ednovak.nearby;

public class tree{

	public int value; // All nodes have values
	public  char[] path; // Tells the alg if it's parent is to the left or right
	public String special; // If it's alice or Bob or something special
	public tree left; // Leaves have null for left and right  // some other nodes might have \\
	public tree right; // Leave have null for left and right  // null for these values       \\
	public tree parent; // Root node has null for parent
	public String treeType;
	
	public tree(int newValue, char[] newPath, tree newLeft, tree newRight){
		value = newValue; 
		path = newPath;
		special = null; 
		left = newLeft; 
		right = newRight; 
		//treeType = nTreeType;
	}
	
	
	// For rightLeaf and leftLeaf the follow table holds true
	// -----------------------------
	// | type | largest | smallest |
	// -----------------------------
	// | lon  | 4003017 | 0        |
	// | lat  | 4003003 | 0		   |
	// -----------------------------
	// The slight difference is a rounding error.  If you look at longitudeToLeaf and latitudeToLeaf
	// You'll see they use slightly different constants and that the constants are not exactly 2:1 
	// yet the Magnitude of degrees is exactly 2:1.  As a result, and to simply programming, I'm choosing to
	// consider a leaf anything more than 4003017.  This means there are bugs around 90 and -90 latitude
	// fortunately this is at the poles.  Also, there are bugs there anyway because the protocol does not recognize
	// that -180 and 180 are right next to each other.  So we have problems at the extremes.  Fortunatley, again
	// -180 and 180 is in the middle of the pacific ocean.
	// Used to find Alice's rep set
	public tree rightLeaf(){
		tree cur = this;
		while (cur.right != null){
			cur = cur.right;
		}
		
		// This only holds for the old longtitude values! 
		if (cur.value > 4003017){
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
		// Same problem here!!
		if (cur.value > 4003017){
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
	
	public treeQueue findCoverSet(tree leaf){
		treeQueue answer = new treeQueue();
		tree cur = leaf;
		while (cur != null){
			answer.push(cur);
			cur = cur.parent;
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
	
	public int count(){
		// Setup
		int sum = 0;
		treeQueue top = new treeQueue();
		treeQueue bottom = new treeQueue();
		top.push(this);
		
		// Breadth first traversal through entire tree
		while (top.length != 0){
			sum = sum + top.length;
			for(int i = 0; i < top.length; i++){
				tree cur = top.peek(i).left;
				if (cur != null){
					bottom.push(cur);
				}
				cur = top.peek(i).right;
				if (cur != null){
					bottom.push(cur);
				}
			}
			top = bottom;
			bottom = new treeQueue();
		}
		
		return sum;
	}
}