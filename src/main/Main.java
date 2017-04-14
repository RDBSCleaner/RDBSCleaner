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
		String currentDIR = System.getProperty("user.dir");//��õ�ǰ����·��
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
		
		//rule.formatRules(rulesFile, rule_outFile);	//��ʽ��Rules, �����⹫ʽת��Ϊһ��ν���߼���ʽ (����Ҫִ�У�ɾ��)
		
		rule.formatEvidence(evidence_outFile);
		
		
		//����MLN��ص��������
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
		
		HashMap<String, Double> attributes = MLNmain.main(learnwt);	//��ڣ�����ѧϰ weight learning����using 'Diagonal Newton discriminative learning'

		//��ӡMLN marginal����õ��ĸ���
		Iterator<Entry<String, Double>> iter = attributes.entrySet().iterator(); 
        while(iter.hasNext()){ 
            Entry<String, Double> me = iter.next() ; 
            System.out.println(me.getKey() + " --> " + me.getValue()) ; 
        }
        
        //���򻮷� �γ�Domains
        domain.init(dataURL, splitString, ifHeader, rules);
        //��ÿ��Domainִ��group by key����
        domain.groupByKey(domain.domains, rules);
      		
        //����MLN�ĸ���������������
        domain.correctByMLN(domain.Domain_to_Groups, attributes, domain.header);
        
        System.out.println(">>> Find Duplicate Values...");
        
        List<List<Integer>> keysList = domain.combineDomain(domain.Domain_to_Groups); 	//���������ظ������tupleID
        
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
      	
      	//ִ��ȥ�ز���
      	domain.deleteDuplicate(keysList, domain.dataSet);
      	
      	System.out.println(">>> completed!");

      	//��ӡɾ�����󡯵����ݼ�����
      	domain.printDataSet(domain.dataSet);
      	
//        domain.printDomainContent(domain.domains);
        //����ظ�����
//        List<List<Integer>> keyList_list = domain.checkDuplicate(domain.Domain_to_Groups);
      	
      		
	}
	
}
