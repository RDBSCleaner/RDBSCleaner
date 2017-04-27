package test;

import java.util.ArrayList;
import java.util.List;

public class Main {
	
	public static void main(String[] args){

		List<String> list = new ArrayList<String>(6);
		String str = "a";
		int i=0;
		while(i<8){
			list.add(str+(i++));
			System.out.print(list.get(i-1)+" ");
		}
		System.out.println();
		for(int j=0;j<list.size();){
			String current = list.get(j);
			if(current.equals("a3")){
				list.remove(current);
				continue;
			}
			j++;
		}
		for(String current:list){
			System.out.print(current+" ");
		}
		
	}
}
