package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rule {

	String predicate = null;
	String value = null;
	String[] header = null;
	ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
	
	public Rule(){}
	
	
	/**
	 * 返回单个属性名的所在列的编号
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
	 * 返回多个属性名的所在列的编号
	 * @return Attribute Indexes
	 * */
	public static int[] findAttributeIndex(String[] name, String[] header){
		boolean[] flag = new boolean[header.length];
		int[] attributeIDs = new int[name.length];
		
		for(int i=0;i<flag.length;i++){	//初始化flag
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
	 * 已知tuple IDs和reason IDs
	 * 返回结果result值所在列的编号
	 * @param tuple IDs
	 * @param reason IDs
	 * */
	public static int[] findResultIDs(int[] tupleIDs, int[] reasonIDs){
		int[] resultIDs = new int[tupleIDs.length-reasonIDs.length];
		int index=0;
		ArrayList<Integer> list = new ArrayList<Integer>();
        //选出属于tupleIDs但不属于reasonIDs的元素, 即resultIDs
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
	 * 从rules中提取predicates
	 * @param fileURL
	 * @param splitString
	 * @return List<String[]>
	 * */
	public List<Tuple> loadRules(String DBurl, String fileURL,String splitString){
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
	        	
	        	//t.setReasonAttributeIndex(findAttributeIndex(reason_predicates, header));	//保存reason的attribute IDs
	        	
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
	 * 格式化数据集，使其符合MLN的输入所需
	 * @param outFile
	 */
	public void formatEvidence(String outFile){
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
        for(int i=0; i<header.length; i++){
            HashMap<String,Integer> map = new HashMap<>();
            for(int k=0; k<tupleList.size(); k++){
                String item = tupleList.get(k).getContext()[i];
                if (!map.containsKey(item)){
                    map.put(item,1);
                }
                else {
                    map.put(item,map.get(item)+1);
                }
                // 用HashMap存储每一个item出现的次数
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                double pre = (double)entry.getValue()/tupleList.size();
                DecimalFormat format = new DecimalFormat("#0.00");
                content += format.format(pre) + " ";
                content += header[i]+"("+entry.getKey()+")" + "\n";
            }
        }
        writeToFile(content, outFile);
		System.out.println(">> Writing Completed!");
	}
	
	/**
	 * 格式化Rules, 将命题公式转换为一阶谓词逻辑形式
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
	        	firstOrderLogic = "1\t";//add　default weight 添加默认权重weight=1
	        	String[] line = str.split("=>");//分为‘原因reason’和‘结果result’两个部分
	        	
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
	
	public static void writeToFile(String content, String outFile){	//把content的内容追加写入文件
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
	        	
	        	String[] line = str.split("=>");//分为‘原因reason’和‘结果result’两个部分
	        	
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
		// read file content from file 读取文件内容
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
