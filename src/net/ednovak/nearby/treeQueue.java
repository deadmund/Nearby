package net.ednovak.nearby;

import android.util.Log;

public class treeQueue {
		
	private tree[] arr = new tree[2]; 
	private int end = 0;
	public int length = end;
	
	public treeQueue union(treeQueue other){
		treeQueue tmp = new treeQueue();
		for (int i=0; i < arr.length; i++){
			tmp.push(arr[i]);
		}
		for (int i=0; i < other.length; i++){
			tmp.push(other.peek(i));
		}
		return tmp;
	}
	
	public void push(tree newTree){
		if (end == arr.length-1){
			tree[] tmp = new tree[arr.length*2];
			for (int i = 0; i < arr.length; i++){
				tmp[i] = arr[i];
			}
			// end = i; // Probs don't even need this!
			arr = tmp;
		}
		arr[end] = newTree;
		end++;
		length = end;
	}
	
	public tree peek(int spot){
		if (spot >= end){
			System.out.println("queue out of bounds, tried to access spot: " + spot);
			System.exit(401);
		}
		
		if (spot == -1){
			return arr[end-1]; // Last spot of queue (has something in it definitely)
		}
		return arr[spot];
	}
	
	public int length(){
		return end;
	}
	
	public tree findUserLeaf(){
		tree user = null;
		for (tree t : arr){
			if (t.special != null){
				if (user != null){
					Log.d("Err", "Found two nodes with special set in one leafQueue");
				}
				user = t;
			}
		}
		return user;
	}
	
	// Only pushes the node if it isn't already in the queue
	// Returns 0 on success, -1 on failure
	public int uniquePush(tree nTree){
		for (int i = 0; i < length; i++){
			if (arr[i].value == nTree.value){
				return -1;
			}
		}
		Log.d("tree", "Adding this node to the queue: " + nTree.value);
		this.push(nTree);
		return 0;
	}
	
	
	public tree find(int value){
		for(int i = 0; i < length; i++){
			if (arr[i].value == value){
				return arr[i];
			}
		}
		return new tree(0, new char[0], null, null, 0);
	}
}