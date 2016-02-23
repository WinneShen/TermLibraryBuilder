package entities;

public class WordEntity {
	private String word;
	private String pos;
	private int count;
	public WordEntity(String w,String p,int c){
		word=w;
		pos=p;
		count=c;
	}
	public int getCount(){
		return count;
	}
	public String getWord(){
		return word;
	}
	public void addCount(int c){
		count+=c;
	}
}
