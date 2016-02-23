package code;

import java.io.FileWriter;
import java.io.IOException;

import nlpir.NLpir;

public class test {
	public static void main(String[] args) throws IOException {
		NLpir nlpir = new NLpir();

		// 0.init nlpir
		if (nlpir.init() == 0) {
			System.err.println("init nlpir fail");
			return;
		}
		long t1=System.currentTimeMillis();
		String s=nlpir.getWordFreqFromFile("resource/tt.txt");
		System.out.println("time is "+(System.currentTimeMillis()-t1)/1000.0+"s");
		FileWriter fw=new FileWriter("output/dirtResult.txt");
		fw.write(s);//("我想要订购199的套餐，谢谢"));
		fw.close();
	}
}
