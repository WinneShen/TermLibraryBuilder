package code;

import java.io.IOException;

import nlpir.NLpir;

public class Segment {

	public static void main(String[] args) throws IOException {
		long t1;
		String dataSourceFilePath = "resource/test.txt";
		String wordResultFilePath = "resource/clearResult.txt";
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
		nlpir.segmentWordsConcurrently(dataSourceFilePath, wordResultFilePath);
		System.out.println("time for segmenting words concurrently is "
				+ (System.currentTimeMillis() - t1) / 1000.0 + "s");

		// 4.nlpir exit
		nlpir.exit();
	}
}
