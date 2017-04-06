package data;

public class Tuple {
	
	public String[] reason = null;	//记录Tuple的reason predicates
	public String[] result = null;	//记录Tuple的result predicates
	public int index = -1; //index from 0 to dataset.size;
	
	private int[] resultAttributeIndex = null;
	private int[] reasonAttributeIndex = null;
	private int[] AttributeIndex = null;
	private String[] AttributeNames = null; 
	private String[] TupleContext = null;
	
	
	public Tuple(){}
	
	public void setContext(String[] TupleContext){
		this.TupleContext = TupleContext;
	}
	
	public void setReason(String[] reason){
		this.reason = new String[reason.length];
		for(int i=0;i<reason.length;i++){
			this.reason[i] = reason[i];
		}
	}
	
	public void init(String tupleLine,String splitString,int index){//Init the tuple
		
		this.TupleContext = tupleLine.split(splitString);
		this.index = index;
		//System.out.println("Tuple.length = "+Tuple.length);
	}
	
	public String[] getAttributeNames(){
		if(null!=AttributeNames){
			return AttributeNames;
		}else
			System.out.println("Error: Attribute Names is empty.");
			return null;
	}
	
	public void setAttributeNames(String[] attributeNames){
		int length = attributeNames.length;
		this.AttributeNames = new String[length];
		for(int i=0;i<length;i++){
			this.AttributeNames[i] = attributeNames[i];
		}
	}
	
	public String[] getContext(){
		if(null!=TupleContext){
			return TupleContext;
		}else
			System.out.println("Error: tuple context is empty.");
			return null;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int[] getReasonAttributeIndex() {
		return reasonAttributeIndex;
	}

	public void setReasonAttributeIndex(int[] attributeIndex) {
		int length = attributeIndex.length;
		reasonAttributeIndex = new int[length];
		for(int i=0;i<length;i++){
			reasonAttributeIndex[i] = attributeIndex[i];
		}
	}
	
	public int[] getResultAttributeIndex() {
		return resultAttributeIndex;
	}

	public void setResultAttributeIndex(int[] attributeIndex) {
		int length = attributeIndex.length;
		resultAttributeIndex = new int[length];
		for(int i=0;i<length;i++){
			resultAttributeIndex[i] = attributeIndex[i];
		}
	}

	public int[] getAttributeIndex() {
		return AttributeIndex;
	}

	public void setAttributeIndex(int[] attributeIndex) {
		AttributeIndex = attributeIndex;
	}
}
