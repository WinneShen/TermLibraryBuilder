package code;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import nlpir.NLpir;
import entities.WordEntity;

public class Segment {
	private static void write2file(Map<String,WordEntity> weMap,String filePath) throws IOException{
		FileWriter fw=new FileWriter(filePath);
		for(WordEntity value:weMap.values()){
			fw.write(value.getWord()+"\t"+value.getCount()+"\n");
		}
		fw.close();
	}

	public static void main(String[] args) throws IOException {
		long t1;
		String dataSourceFilePath = "resource/test.txt";
		String wordResultFilePath = "output/clearResult.txt";
		String termLibraryFilePath="output/termLibrary.txt";
		NLpir nlpir = new NLpir();

		// 0.init nlpir
		if (nlpir.init() == 0) {
			System.err.println("init nlpir fail");
			return;
		}
//		// 1.get new words based Dic by NLPIR_AddUserWord
//		t1 = System.currentTimeMillis();
//		nlpir.addNewWordsFromFile(dataSourceFilePath);
//		System.out
//				.println("time for getting new words by NLPIR_AddUserWord is "
//						+ (System.currentTimeMillis() - t1) / 1000.0 + "s");
//
//		// 2.get new words by POS-Combination algorithm
//		t1 = System.currentTimeMillis();
//		nlpir.addNewWordsByPOSCombination(dataSourceFilePath);
//		System.out
//				.println("time for getting new words by POS-Combination algorithm is "
//						+ (System.currentTimeMillis() - t1) / 1000.0 + "s");

		// 3.segment words from sourcePath
		t1 = System.currentTimeMillis();
		Map<String,WordEntity> weMap=nlpir.segmentWordsConcurrently(dataSourceFilePath);//weMap按词频从大到小排序
		System.out.println("time for segmenting words concurrently is "
				+ (System.currentTimeMillis() - t1) / 1000.0 + "s");
		write2file(weMap, wordResultFilePath);

		// 4.filter out words whose freq < threshold
		t1 = System.currentTimeMillis();
		weMap=nlpir.wordFreqFilter(weMap);
		System.out.println("time for filtering out words whose freq < threshold is "
				+ (System.currentTimeMillis() - t1) / 1000.0 + "s");
		
		// 5.write to file
		write2file(weMap, termLibraryFilePath);
		
		// 6.nlpir exit
		nlpir.exit();
	}
}
