package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		
		//区域划分 形成Domains
		domain.init(dataURL, splitString, ifHeader, rules);
		//对每个Domain执行group by key操作
		domain.groupByKey(domain.domains, rules);
		List<List<Integer>> keyList_list = domain.checkDuplicate(domain.Domain_to_Groups);
		
		
		System.out.println("\n\tDuplicate keys: ");
		int c=0;
		for(List<Integer> keyList: keyList_list){
			System.out.print("\tGroup "+(++c)+": ");
			for(int key: keyList){
				System.out.print(key+" ");
			}
			System.out.println();
		}
		System.out.println();
		
		
		
		domain.printDataSet(domain.dataSet);	//打印删除‘前’的数据集内容
		
		domain.deleteDuplicate(keyList_list, domain.dataSet);
		
		domain.printDataSet(domain.dataSet);	//打印删除‘后’的数据集内容
		
		//rule.initData(dataURL, splitString, ifHeader);
		//rule.formatRules(rulesFile, rule_outFile);	//格式化Rules, 将命题公式转换为一阶谓词逻辑形式
		//rule.formatEvidence(evidence_outFile);
		
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
		
		String[] learnwt = list.toArray(new String[list.size()]);
		
		//MLNmain.main(learnwt);//入口：参数学习 weight learning——using 'Diagonal Newton discriminative learning'
	}
	
}
