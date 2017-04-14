package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import data.Domain;
import data.Rule;
import data.Tuple;
import tuffy.main.MLNmain;

public class Main {
	
	public static void main(String[] args) throws SQLException, IOException {
		
		Rule rule = new Rule();
		String currentDIR = System.getProperty("user.dir");//获得当前工程路径
		String rulesFile = currentDIR + "\\rules.txt";
		String rule_outFile = currentDIR + "\\prog.mln";
		String evidence_outFile = currentDIR + "\\evidence.db";
		//String dataURL = currentDIR + "\\dataSet\\"+ "car evaluation\\car.data";
		String dataURL = currentDIR + "\\dataSet\\"+ "test-city.data";
		//String dataURL = currentDIR + "\\dataSet\\"+ "car evaluation\\car-nodoor.csv";
		
		String splitString = ",";
		boolean ifHeader = true;
		
		List<Tuple> rules = rule.loadRules(dataURL, rulesFile, splitString);
		Domain domain = new Domain();
		
		rule.initData(dataURL, splitString, ifHeader);
		
		//rule.formatRules(rulesFile, rule_outFile);	//格式化Rules, 将命题公式转换为一阶谓词逻辑形式 (不需要执行，删掉)
		
		rule.formatEvidence(evidence_outFile);
		
		
		//调用MLN相关的命令参数
		ArrayList<String> list = new ArrayList<String>();
		String marginal_args = "-marginal";
		list.add(marginal_args);
		
		String learnwt_args = "-learnwt";
		//list.add(learnwt_args);
		
		String nopart_args = "-nopart";
		//list.add(nopart_args);
		
		String mln_args = "-i";
		list.add(mln_args);
		
		String mlnFileURL = "prog.mln";//prog.mln
		list.add(mlnFileURL);
		
		String evidence_args = "-e";
		list.add(evidence_args);
		
		String evidenceFileURL = "evidence.db"; //samples/smoke/
		list.add(evidenceFileURL);
		
		String queryFile_args = "-queryFile";
		list.add(queryFile_args);
		
		String queryFileURL = "query.db";
		list.add(queryFileURL);
		
		String outFile_args = "-r";
		list.add(outFile_args);
		
		String weightFileURL = "out.txt";
		list.add(weightFileURL);
		
		String noDropDB = "-keepData";
		list.add(noDropDB);

		
		String[] learnwt = list.toArray(new String[list.size()]);
		
		HashMap<String, Double> attributes = MLNmain.main(learnwt);	//入口：参数学习 weight learning——using 'Diagonal Newton discriminative learning'

		//打印MLN marginal计算得到的概率
		Iterator<Entry<String, Double>> iter = attributes.entrySet().iterator(); 
        while(iter.hasNext()){ 
            Entry<String, Double> me = iter.next() ; 
            System.out.println(me.getKey() + " --> " + me.getValue()) ; 
        }
        
        //区域划分 形成Domains
        domain.init(dataURL, splitString, ifHeader, rules);
        //对每个Domain执行group by key操作
        domain.groupByKey(domain.domains, rules);
      		
        //根据MLN的概率修正错误数据
        domain.correctByMLN(domain.Domain_to_Groups, attributes, domain.header);
        
        System.out.println(">>> Find Duplicate Values...");
        
        List<List<Integer>> keysList = domain.combineDomain(domain.Domain_to_Groups); 	//返回所有重复数组的tupleID
        
        System.out.println("\n\tDuplicate keys: ");
        int c=0;
        if(keysList.isEmpty())System.out.println("\tNo duplicate exists.");
        for(List<Integer> keyList: keysList){
        	System.out.print("\tGroup "+(++c)+": ");
        	for(int key: keyList){
        		System.out.print(key+" ");
        	}
      		System.out.println();
      	}
      	System.out.println();
      	
      	System.out.println(">>> Delete duplicate tuples");
      	
      	//执行去重操作
      	domain.deleteDuplicate(keysList, domain.dataSet);
      	
      	System.out.println(">>> completed!");

      	//打印删除‘后’的数据集内容
      	domain.printDataSet(domain.dataSet);
      	
//        domain.printDomainContent(domain.domains);
        //标记重复数据
//        List<List<Integer>> keyList_list = domain.checkDuplicate(domain.Domain_to_Groups);
      	
      		
	}
	
}
