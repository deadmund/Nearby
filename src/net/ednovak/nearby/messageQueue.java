package net.ednovak.nearby;

public class messageQueue {
		
	private String[] arr = new String[2]; 
	private int end = 0;
	public int length = end;
	
	public void push(String newMessage){
		if (end == arr.length-1){
			String[] tmp = new String[arr.length*2];
			for (int i = 0; i < arr.length; i++){
				tmp[i] = arr[i];
			}
			// end = i; // Probs don't even need this!
			arr = tmp;
		}
		arr[end] = newMessage;
		end++;
		length = end;
	}
	
	public String peek(int spot){
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
}
