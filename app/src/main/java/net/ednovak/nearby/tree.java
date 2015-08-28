package net.ednovak.nearby;


public class tree{

	public int value; // All nodes have values
	public  char[] path; // Tells the alg if it's parent is to the left or right
	public String special; // If it's alice or Bob or something special
	public tree left; // Leaves have null for left and right  // some other nodes might have \\
	public tree right; // Leave have null for left and right  // null for these values       \\
	private tree parent; // Root node has null for parent
	public String treeType;
	public int height;
	public String type;
	
	// The tree class assumes that 4003017 is our magic number but this number is only magic for
	// longitude trees.  I think this is causing a bug for latitude trees.
	private int magic;
	
	public tree(int newValue, char[] newPath, tree newLeft, tree newRight, int nHeight, String nType){
		value = newValue; 
		path = newPath;
		special = null; 
		left = newLeft; 
		right = newRight;
		height = nHeight;
		type = nType;
		
		if (type.equals("lat")){
			magic = 4003003;  // Assume longitude
		}
		else if (type.equals("lon")){
			magic = 4003017;
		}
	}
	
	public void setType(String type){
		if (type.equals("lat")){
			magic = 4003003;
		}
		else if (type.equals("lon")){
			magic = 4003017;
		}
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
	// consider a leaf anything less than 4003017.  This means there are bugs around 90 and -90 latitude
	// fortunately this is at the poles.  Also, there are bugs there anyway because the protocol does not recognize
	// that -180 and 180 are right next to each other.  So we have problems at the extremes.  Fortunately, again
	// -180 and 180 is in the middle of the pacific ocean.
	
	// Used to find wall set
	public tree rightLeaf(){
		tree cur = this;
		while (cur.right != null){
			cur = cur.right;
		}
		
		// This only holds for the old longtitude values! 
		if (cur.value > magic){
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
		if (cur.value > magic){
			return null;
			//System.out.println("This 'leaf' is not really a leaf.");
		}
		return cur;
	}
	
	public tree createParent(){
		tree parent;
		if (path.length == 0) {
			path = new char[1];
			path[0] = '0';
		}

		char[] nPath = new char[path.length - 1]; // Drop the last bit (manual copy) :(
		for (int j = 0; j < nPath.length; j++) {
			nPath[j] = path[j];
		}

		if (this.upRightward()) { // This is a branch that goes right (upward)
			int nValue = value + magic; // Max num of leaf nodes (longitude only)
			parent = new tree(nValue, nPath, this, null, height+1, type);
		}

		else { // This is a branch that goes left (upward)
			int nValue = value + (magic - (int) (Math.pow(2.0, (double) (height))));
			parent = new tree(nValue, nPath, null, this, height+1, type);
		}
		return parent;
	}
	
	public tree getParent(){
		return parent;
	}
	
	
	public void setParent(tree t){
		parent = t;
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
	
	public void setNullChild(tree t){
		if (left == null){
			left = t;
		}
		else if (right == null){
			right = t;
		}
	}
	
	public boolean upRightward(){
		 return path[path.length - 1] == '0';
	}
}
