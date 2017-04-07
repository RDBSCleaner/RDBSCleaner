package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Domain {
	
	public HashMap<Integer,String[]> dataSet = new HashMap<Integer,String[]>();
	//public List<String[]> dataSet = new ArrayList<String[]>();
	public List<HashMap<Integer, Tuple>> domains = null;
	public List<HashMap<Integer, Tuple>> groups;
	public List<List<HashMap<Integer, Tuple>>> Domain_to_Groups = null;	//List<groups> ���group by key��Domain�а�����group�ı��
	
	//�����У�������ݼ���û�и���������һ�������У�Attr1,Attr2,Attr3,...,AttrN
	private String[] header = null; 
	
	public Domain(){}
	
	/**
	 * ��rules�����ݼ��������򻮷� Partition DataSet into Domains
	 * @param fileURL
	 * @param splitString
	 * @param ifHeader
	 * @param rules
	 * */
	public void init(String fileURL,String splitString,boolean ifHeader, List<Tuple> rules){
		// read file content from file ��ȡ�ļ�����
        FileReader reader;
        int rules_size = rules.size();
        domains = new ArrayList<HashMap<Integer, Tuple>>(rules_size);
        
        for(int i=0;i<rules_size;i++){
        	domains.add(new HashMap<Integer, Tuple>());
        }
        
		try {
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
	        String str = null;
	        
	        int key = 0; //tuple index
	        
	        if(ifHeader && (str = br.readLine()) != null){  //The data has header
	        	header=str.split(splitString);
	        	while((str = br.readLine()) != null) {
	        		//dataSet.add(str.split(splitString));
	        		String[] tuple = str.split(splitString);
	        		
	        		dataSet.put(key, tuple);
	        		
		        	for(int i=0;i<rules_size;i++){	//Ϊÿһ��rule�������ݼ�����Di
		        		Tuple curr_rule = rules.get(i);
		        		String[] attributeNames = curr_rule.getAttributeNames();

		        		int[] IDs = Rule.findAttributeIndex(attributeNames, header);
		        		
		        		String[] reason = curr_rule.reason;
		        		int[] reasonIDs = Rule.findAttributeIndex(reason, header);
		        		
		        		String[] reasonContent = new String[reasonIDs.length];
		        		String[] tupleContext = new String[IDs.length];
		        		int reason_index = 0;
		        		for(int j=0; j<IDs.length; j++){
		        			tupleContext[j] = tuple[IDs[j]];//�������ڸ������tuple����
		        			if(ifReason(IDs[j],reasonIDs)){	//�����reason,�����reasonContent
		        				reasonContent[reason_index++] = tuple[IDs[j]];
		        			}
		        		}
		        		Tuple t = new Tuple();
		        		t.setContext(tupleContext);
		        		t.setReason(reasonContent);
		        		//����Di�������,����hashMap
		        		domains.get(i%rules_size).put(key,t); //<K,V>  K = tuple ID , from 0 to n
		        	}
		        	key++;
		        }
	        }else{
	        	int length = 0;
	        	boolean flag = false;
	        	while((str = br.readLine()) != null) {
	        		//dataSet.add(str.split(splitString));
	        		String[] tuple = str.split(splitString);
	        		
	        		dataSet.put(key, tuple);
	        		
	        		if(!flag){
	        			length = tuple.length;
			        	header = new String[length];
			        	for(int i=1;i<=length;i++){
			        		header[i-1]="Attr"+i;
			        	}
			        	flag = true;
	        		}
		        	
	        		for(int i=0;i<rules_size;i++){	//Ϊÿһ��rule�������ݼ�����Di
		        		Tuple curr_rule = rules.get(i);
		        		String[] attributeNames = curr_rule.getAttributeNames();

		        		int[] IDs = Rule.findAttributeIndex(attributeNames, header);
		        		
		        		String[] reason = curr_rule.reason;
		        		int[] reasonIDs = Rule.findAttributeIndex(reason, header);
		        		
		        		String[] reasonContent = new String[reasonIDs.length];
		        		String[] tupleContext = new String[IDs.length];
		        		int reason_index = 0;
		        		for(int j=0; j<IDs.length; j++){
		        			tupleContext[j] = tuple[IDs[j]];//�������ڸ������tuple����
		        			if(ifReason(IDs[j],reasonIDs)){	//�����reason,�����reasonContent
		        				reasonContent[reason_index++] = tuple[IDs[j]];
		        			}
		        		}
		        		Tuple t = new Tuple();
		        		t.setContext(tupleContext);
		        		t.setReason(reasonContent);
		        		//����Di�������,����hashMap
		        		domains.get(i%rules_size).put(key,t); //<K,V>  K = tuple ID , from 0 to n
		        	}
		        	key++;
		        }
	        	System.out.println("header length = "+length);
	        }
	        br.close();
	        reader.close();
	        printDomainContent(domains);
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	public static boolean ifReason(int index, int[] reasonIDs){
		boolean result = false;
		int[] copy_reasonIDs = Arrays.copyOfRange(reasonIDs, 0, reasonIDs.length);
		Arrays.sort(copy_reasonIDs);
		for(int i=0;i<copy_reasonIDs.length;i++){
			if(index == copy_reasonIDs[i]){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * ���λ��֣�����reason predicates ��������
	 * @param List<HashMap<Integer,List>> domains
	 * @param List<Tuple> rules
	 * */
	public void groupByKey(List<HashMap<Integer,Tuple>> domains, List<Tuple> rules){
		HashMap<Integer, Tuple> domain = null;
		int size = domains.size();
		
		Domain_to_Groups = new ArrayList<List<HashMap<Integer, Tuple>>>(size);
		
		for(int i=0;i<size;i++){
			domain = domains.get(i);
			int domain_size = domain.size();
			boolean[] flags = new boolean[domain_size];
			groups = new ArrayList<HashMap<Integer, Tuple>>();
			for(int k=0;k<domain_size;k++){
				Tuple tuple1 = domain.get(k);
				HashMap<Integer, Tuple> group = new HashMap<Integer, Tuple>();
				group.put(k, tuple1);	//add Ti to group(i), now G(i)={Ti}
				String[] reason1 = tuple1.reason;
				//int index=0;
				if(!flags[k]){
					for(int m=k+1;m<domain_size;m++){
						if(flags[m])continue;
						Tuple tuple2 = domain.get(m);
						String[] reason2 = tuple2.reason;
						if(Arrays.equals(reason1,reason2)){
							group.put(m, tuple2);
							flags[m] = true;
						}
						//index++;
					}
					if(group.size()>1){
						groups.add(group);
					}
				}
			}
			Domain_to_Groups.add(groups);
		}
		int d_index=0;
		for(List<HashMap<Integer, Tuple>> d: Domain_to_Groups){
			System.out.println("\n*******Domain "+(++d_index)+"*******");
			printGroup(d);
		}
		//printGroup(groups);
	}
	
	/**
	 * ɾ���ظ�����
	 * @param List<List<Integer>> keyList_list
	 * @param HashMap<Integer,String[]> dataSet
	 * */
	public void deleteDuplicate(List<List<Integer>> keyList_list, HashMap<Integer,String[]> dataSet){
		for(List<Integer> keyList: keyList_list){
			for(int i=0;i<keyList.size()-1;i++){
				int key = keyList.get(i);
				dataSet.remove(key);
			}
		}
	}
	
	/**
	 * ����ظ����ݣ���ִ�кϲ�����
	 * */
	public List<List<Integer>> checkDuplicate(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups){
		
		List<List<Integer>> keyList_list = new ArrayList<List<Integer>>();
		int round = 0;
		
		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			if(round == 0){	//��һ��ѭ��,��ǵ�һ��Domain��groups�е��ظ�����
				for(HashMap<Integer, Tuple> group: groups){
					List<Integer> keyList = new ArrayList<Integer>();
					for (int key: group.keySet()) {
						//save keys
						keyList.add(key);
					}
					keyList_list.add(keyList); 	//��һ��Domain�У��ж��ٸ�group ���ж��ٸ�keyList_list
					round++;
				}
			}else{
				int index=0;
				for(List<Integer> list: keyList_list){
					for(; index<groups.size(); index++){
						HashMap<Integer, Tuple> group = groups.get(index);
						boolean result = false;
						for(int i=0;i<list.size();i++){
							int key = list.get(i);
							Iterator iter = group.entrySet().iterator();
							Entry entry = (Entry) iter.next();
							int currentKey = (int)entry.getKey();
						    if(currentKey > key)break;
							result = group.containsKey(key);
							if(!result){	//��������ڣ���ɾ��keyList�е����key
								list.remove(i);
							}
						}
					}
					if(list.size()<=1){	//��group�в������ظ�����
						keyList_list.remove(list);
						return null;
					}
				}
				
			}
		}
		//�ϲ�����������
		return keyList_list;
	}
	
	
	/**
	 * ���������еı��ID�͵�ǰ��������Tuple����ö�Ӧ������ֵvalues
	 * @param Tuple tuple
	 * @param String[] attributeIDs
	 * @return values
	 * */
	public static String[] getValues(Tuple t, int[] attributeIDs){
		int length = attributeIDs.length;
		String[] values = new String[length];
		for(int i=0;i<length;i++){
			values[i] = t.reason[attributeIDs[i]];
		}
		return values;
	}
	
	/**
	 * ��ӡ���ֺ������domain
	 * @param domains
	 * */
	public void printDomainContent(List<HashMap<Integer, Tuple>> domains){
		HashMap<Integer, Tuple> domain = null; 
		for(int i=0;i<domains.size();i++){
			domain = domains.get(i);
			Iterator iter = domain.entrySet().iterator();
			System.out.println("\n---------Domain "+(i+1)+"---------");
			
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				Object key = entry.getKey();
				Tuple value = (Tuple)entry.getValue();
				System.out.println("key = "+key+" value = "+Arrays.toString(value.getContext()));
			}
		}
		//System.out.println("\nDomain.size = "+domains.size()+"\n");
		System.out.println("\n>>> Completed!");
	}
	
	/**
	 * ��ӡ���ֺ������Group
	 * @param List<HashMap<Integer, List>> groups
	 * */
	public void printGroup(List<HashMap<Integer, Tuple>> groups){
		HashMap<Integer, Tuple> group = null; 
		for(int i=0;i<groups.size();i++){
			group = groups.get(i);
			Iterator iter = group.entrySet().iterator();
			System.out.println("\n---------Group "+(i+1)+"---------");
			
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				Object key = entry.getKey();
				Tuple value = (Tuple)entry.getValue();
				System.out.println("key = "+key+" value = "+Arrays.toString(value.getContext()));
			}
		}
//		System.out.println("\nGroup.size = "+groups.size()+"\n");
		System.out.println(">>> Completed!");
	}
	
	
	/**
	 * ��ӡ�������ݼ�������
	 * @param HashMap<Integer, String[]> dataSet
	 * */
	public void printDataSet(HashMap<Integer, String[]> dataSet){
		System.out.println("==========DataSet Content==========");
		
		Iterator iter = dataSet.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			Object key = entry.getKey();
			String[] value = (String[])entry.getValue();
			System.out.println("key = "+key+" value = "+Arrays.toString(value));
		}
		System.out.println();
	}
}
