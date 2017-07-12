package test;

import java.util.ArrayList;
import java.util.List;

public class Main {
	
	public static void main(String[] args){

		List<Integer> list = new ArrayList<Integer>(6);
		int i=0;
		while(i<3){
			list.add((++i)*2);
//			System.out.print(list.get(i-1)+" ");
		}
		System.out.println();
		for(int j=0;j<list.size();){
			list.remove(j);
		}
		
		
	}
}
