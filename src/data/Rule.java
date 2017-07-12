package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.*;
import tuffy.util.Config;
import org.postgresql.Driver;

public class Rule {

	String predicate = null;
	String value = null;
	public String[] header = null;
	ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
	
	public Rule(){}
	
	
	public ArrayList<Integer> findIgnoredTuples(List<Tuple> rules){
		ArrayList<Integer> ignoredIDs = new ArrayList<Integer>();
		
		HashMap<String,Integer> map = new HashMap<String,Integer>(header.length);
		
		for(Tuple rule: rules){
			int i = 0;
			while(i<rule.reason.length){
				map.put(rule.reason[i++], 1);
			}
			int j=0;
			while(j<rule.result.length){
				map.put(rule.result[j++], 1);
			}
		}
		for(int i=0;i<header.length;i++){
			String predicate = header[i];
			Integer result = map.get(predicate);
			if(null == result){//find ignored tuple predicate
				ignoredIDs.add(i);
			}
		}
		System.out.print("Ignored Tuple ID:");
		for(int i: ignoredIDs){
			System.out.print(i+" ");
		}
		
		return ignoredIDs;
	}
	
	/**
	 * 鏉╂柨娲栭崡鏇氶嚋鐏炵偞锟窖冩倳閻ㄥ嫭澧嶉崷銊ュ灙閻ㄥ嫮绱崣锟�
	 * @return Attribute Index
	 * */
	public static int findAttributeIndex(String name, String[] header){
		int index = 0;
		for(;index<header.length;index++){
			if(header[index].equals(name))
				break;
		}
		return index;
	}
	
	/**
	 * 鏉╂柨娲栨径姘嚋鐏炵偞锟窖冩倳閻ㄥ嫭澧嶉崷銊ュ灙閻ㄥ嫮绱崣锟�
	 * @return Attribute Indexes
	 * */
	public static int[] findAttributeIndex(String[] name, String[] header){
		boolean[] flag = new boolean[header.length];
		int[] attributeIDs = new int[name.length];
		
		for(int i=0;i<flag.length;i++){	//閸掓繂顫愰崠鏉卨ag
			flag[i] = false;
		}
		
		for(int i=0;i<name.length;i++){
			for(int index = 0;index<header.length;index++){
				if(flag[index]==false && header[index].equals(name[i])){
					attributeIDs[i] = index;
					flag[index] = true;
					break;
				}
			}
		}
		return attributeIDs;
	}
	
	/**
	 * @param tuple IDs
	 * @param reason IDs
	 * */
	public static int[] findResultIDs(int[] tupleIDs, int[] reasonIDs){
		int[] resultIDs = new int[tupleIDs.length-reasonIDs.length];
		int index=0;
		ArrayList<Integer> list = new ArrayList<Integer>();
        //闁鍤仦鐐扮艾tupleIDs娴ｅ棔绗夌仦鐐扮艾reasonIDs閻ㄥ嫬鍘撶槐锟�, 閸楃esultIDs
        for(int i = 0; i < tupleIDs.length; ++i) {
            boolean bContained = false;
            for(int j = 0; j < reasonIDs.length; ++j) {
                if (tupleIDs[i] == reasonIDs[j]) {
                    bContained = true;
                    break;
                }
            }
            if (!bContained) {
            	resultIDs[index++] = tupleIDs[i];
            	//list.add(tupleIDs[i]);
            }
        }
         
//        int res[] = new int[list.size()];
//        for(int i = 0; i < list.size(); ++i)
//            res[i] = list.get(i);
//		
		return resultIDs;
	}
	
	public void getHeader(String DBurl, String splitString){
		try {
			FileReader reader;
			reader = new FileReader(DBurl);
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			if((line = br.readLine())!=null) {
				header = line.split(splitString);
			}else{
				System.err.println("Error: No header!");
			}
			br.close();
	        reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	/**
	 * getting predicates
	 * @param fileURL
	 * @param splitString
	 * @return List<String[]>
	 * @throws IOException 
	 * */
	public List<Tuple> loadRules(String DBurl, String fileURL,String splitString) throws IOException{
		System.out.println(">>> Getting Predicates.......");
		
		FileReader reader;
		String[] reason_predicates = null;
		String[] result_predicates = null;
		List<Tuple> list = new ArrayList<Tuple>();
		getHeader(DBurl, splitString);
		try {
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
	        String line = null;
	        String current = "";
	        int index = 0;
	        while((line = br.readLine()) != null && line.length()!=0){  //The data has header
	        	line = line.replaceAll("1\t", "").replaceAll(" ", "");
	        	String[] line_partiton = line.split("=>");
	        	String[] reason = line_partiton[0].split(splitString);
	        	String[] result = line_partiton[1].split(splitString);
	        	int reason_length = reason.length;
	        	int result_length = result.length;
	        	reason_predicates = new String[reason_length];
	        	result_predicates = new String[result_length];
	        	
	        	for(int i=0;i<reason_length;i++){
	        		reason_predicates[i] = reason[i].replaceAll("\\(.*\\)","");
	        	}
	        	for(int i=0;i<result_length;i++){
	        		result_predicates[i] = result[i].replaceAll("\\(.*\\)","");
	        	}
	        	
	        	Tuple t = new Tuple();
	        	t.reason = reason_predicates;
	        	t.result = result_predicates;
	        	
	        	//t.setReasonAttributeIndex(findAttributeIndex(reason_predicates, header));	//娣囨繂鐡╮eason閻ㄥ垷ttribute IDs
	        	
	        	String[] combine = new String[reason_length+result_length];
	        	System.arraycopy(reason_predicates, 0, combine, 0, reason_length);
	        	System.arraycopy(result_predicates, 0, combine, reason_length, result_length);
	        	t.setAttributeNames(combine);
	        	//t.index = index++;
	        	
	        	Arrays.sort(combine);
	        	if(current.equals(Arrays.toString(combine)))continue;
	        	
	        	System.out.println(Arrays.toString(combine));
	        	
	        	current = Arrays.toString(combine);
	        	list.add(t);
	        }
	        br.close();
	        reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
		System.out.println(">>> Completed!");
		return list;
	}
	
	/**
	 * 閺嶇厧绱￠崠鏍ㄦ殶閹诡噣娉﹂敍灞煎▏閸忓墎顑侀崥鍦N閻ㄥ嫯绶崗銉﹀闂囷拷
	 * @param outFile
	 * @throws IOException 
	 */
	public void formatEvidence(String outFile) throws IOException{
		String content = "";
		//Clean all the out content in 'outFile'
        FileWriter fw;
        System.out.println(">> Write Evidence to file (evidence.db) ...");
		try {
			fw = new FileWriter(outFile);
			fw.write("");
	    	fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int index = 1;
		class IndexAndCount{
			int count;
			int index;
			int headerIndex;
			IndexAndCount(int count,int headerIndex){
				this.count = count;
				this.index = 0;
				this.headerIndex = headerIndex;
			}
			public void increase(){count++;}
			public void setIndex(int index){this.index = index;}
			public double getCount(){return (double)count;}
			public int getIndex() {return index;}
			public int getHeaderIndex() {return headerIndex;}
		}
		HashMap<String,IndexAndCount> map = new HashMap<>();
        for(int i=0; i<header.length; i++){
            for(int k=0; k<tupleList.size(); k++){
                String item = tupleList.get(k).getContext()[i];
                if (!map.containsKey(item)){
                    map.put(item,new IndexAndCount(1,i));
                }
                else {
                    map.get(item).increase();
                }
            }
        }
		for (Map.Entry<String, IndexAndCount> entry : map.entrySet()) {
            if (entry.getValue().getIndex()==0){
                entry.getValue().setIndex(index);
                index++;
            }
//            System.out.println(entry.getValue().getIndex()+' '+entry.getKey());
			double pre = entry.getValue().getCount()/tupleList.size();
			pre += 0.0001;
			if(pre > 1)pre = 1;
			DecimalFormat format = new DecimalFormat("#0.0000");
			content += format.format(pre) + " ";
			content += header[entry.getValue().getHeaderIndex()]+"(\""+entry.getKey()+"\")" + "\n";
		}
        writeToFile(content, outFile);
		System.out.println(">> Writing Completed!");

		String url = Config.db_url;
		String username = Config.db_username;
		String password = Config.db_password;
		
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			String sql = "DROP TABLE IF EXISTS temp CASCADE;";
			stmt.execute(sql);
			
			sql = "CREATE TABLE temp(";
			for(int i=0; i<header.length; i++){
				sql += i==header.length-1?header[i]+" bigint);":header[i]+" bigint,";
			}
			System.out.println(sql);
			stmt.execute(sql);
			//新建表
			sql = "CREATE INDEX temp_idx ON temp (";
			for(int i=0; i<header.length; i++){
				sql += i==header.length-1?header[i]+");":header[i]+", ";
			}
			System.out.println(sql);
			stmt.execute(sql);
			//新建索引
			for (Tuple t : tupleList){
				sql = "INSERT INTO temp VALUES(";
				for (int i = 0;i < t.getContext().length; i++){
					String item = t.getContext()[i];
					sql += i==t.getContext().length-1?map.get(item).getIndex():map.get(item).getIndex() + ",";
				}
				sql += ");";
				stmt.execute(sql);
			}
			stmt.close();
			conn.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 閺嶇厧绱￠崠鏈les, 鐏忓棗鎳℃０妯哄彆瀵繗娴嗛幑顫礋娑擄拷闂冩儼鐨崇拠宥夛拷鏄忕帆瑜般垹绱�
	 * @param fileURL
	 * @param outFile
	 */
	public void formatRules(String fileURL, String outFile){
		
		FileReader inFile;
		try {
			inFile = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(inFile);
	        String str = null;
	        String firstOrderLogic = "";
	        System.out.println(">> Write first-order-logic rules to file (prog.mln) ...");
	        
	        //Clean all the out content in 'outFile'
	        FileWriter fw =  new FileWriter(outFile);
        	fw.write("");
        	fw.close();
	        
	        while((str = br.readLine()) != null && str.length()!=0){
	        	firstOrderLogic = "1\t";//add閵嗭拷default weight 濞ｈ濮炴妯款吇閺夊啴鍣竪eight=1
	        	String[] line = str.split("=>");//閸掑棔璐熼垾妯哄斧閸ョ垘eason閳ユ瑥鎷伴垾妯肩波閺嬫笧esult閳ユ瑤琚辨稉顏堝劥閸掞拷
	        	
	        	String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	        	String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	        	
	        	//reason
	        	for(int index=0; index<reason.length; index++){
        			if(reason[index].contains("=")){
	        			String[] current = reason[index].split("=");
	        			setPredicate(current[0]);
    					setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
    					firstOrderLogic += "!"+predicate+"("+value+") v ";
	        		}else{
	        			int value_index = 0;
	        			for(int i=index;i<reason.length;i++){
	        				setPredicate(reason[i]);
	        				setValue("x"+(value_index++));
	        				firstOrderLogic += "!"+predicate+"("+value+") v ";
	        			}
		        		break;
	        		}
        		}
	        	
	        	//result
	        	for(int index=0; index<result.length; index++){
        			if(result[index].contains("=")){
	        			String[] current = result[index].split("=");
	        			setPredicate(current[0]);
    					setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
    					firstOrderLogic += predicate+"("+value+")";
	        		}else{
	        			int value_index = 0;
	        			for(int i=index;i<result.length;i++){
	        				setPredicate(result[i]);
	        				setValue("y"+(value_index++));
	        				firstOrderLogic += predicate+"("+value+")";
	        			}
		        		break;
	        		}
        		}
	        	//System.out.println(firstOrderLogic+"\n");
	        	
	        	writeToFile(firstOrderLogic, outFile);
	        }
	        System.out.println(">> Writing Completed!");
	        br.close();
	        inFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	void setPredicate(String predicate){
		this.predicate = predicate;
	}
	
	void setValue(String value){
		this.value = value;
	}
	
	public static void writeToFile(String content, String outFile){	//閹跺サontent閻ㄥ嫬鍞寸�圭鎷烽崝鐘插晸閸忋儲鏋冩禒锟�
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile, true)));
			out.write(content+"\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Init Rules
	 * */
	public void initRules(String fileURL){
		//default fileURL = "E:\\eclipse_workspace\\DiscoverRules-v2.0\\rules.txt";
		FileReader reader;
		try {
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
	        String str = null;
	        while((str = br.readLine()) != null){
	        	
	        	String[] line = str.split("=>");//閸掑棔璐熼垾妯哄斧閸ョ垘eason閳ユ瑥鎷伴垾妯肩波閺嬫笧esult閳ユ瑤琚辨稉顏堝劥閸掞拷
	        	
	        	String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	        	String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	        	
	        	//reason
	        	System.out.print("reason: ");
	        	for(int index=0; index<reason.length; index++){
        			if(reason[index].contains("=")){
	        			String[] current = reason[index].split("=");
	        			setPredicate(current[0]);
    					System.out.print("P = "+predicate+", ");
    					setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
	        			System.out.print("V = "+value+".  ");
	        		}else{
	        			for(int i=index;i<reason.length;i++){
	        				setPredicate(reason[i]);
	        				System.out.print("P = "+predicate+", ");
	        				System.out.print("V = any value.  ");
	        			}
		        		break;
	        		}
        		}
	        	System.out.println();
	        	
	        	//result
	        	System.out.print("result: ");
	        	for(int index=0; index<result.length; index++){
        			if(result[index].contains("=")){
	        			String[] current = result[index].split("=");
	        			setPredicate(current[0]);
    					System.out.print("P = "+predicate+", ");
    					setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
	        			System.out.print("V = "+value+".  ");
	        		}else{
	        			for(int i=index;i<result.length;i++){
	        				setPredicate(result[i]);
	        				System.out.print("P = "+predicate+", ");
	        				System.out.print("V = any value.  ");
	        			}
		        		break;
	        		}
        		}
	        	System.out.println();
	        }
	        br.close();
	        reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	/**
	 * Init Data
	 * */
	public void initData(String fileURL,String splitString,boolean ifHeader){//check if the data has header
		// read file content from file
        FileReader reader;
		try {
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
	        String str = null;
	        int index = 0; //tuple index
	        
	        if(ifHeader && (str = br.readLine()) != null){  //The data has header
	        	header=str.split(splitString);
	        	while((str = br.readLine()) != null) {
		        	Tuple t = new Tuple();
		        	t.init(str,splitString,index);//init the tuple,split with ","
		        	tupleList.add(t);
		        	index++;
		        }
	        }else{
	        	
	        	while((str = br.readLine()) != null) {
	        		Tuple t = new Tuple();
		        	t.init(str,splitString,index);//init the tuple,split with ","
		        	tupleList.add(t);
		        	index++;
		        }
	        	int length = tupleList.get(0).getContext().length;
	        	header = new String[length];
	        	char c = 64;
	        	for(int i=1;i<=length;i++){
	        		c +=1;
	        		header[i-1]=String.valueOf(c);
	        	}
	        }
	        
	        br.close();
	        reader.close();
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) throws SQLException, IOException {
//		
//	}
}
