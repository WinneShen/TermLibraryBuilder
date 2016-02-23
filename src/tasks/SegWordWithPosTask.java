package tasks;

import java.util.concurrent.Callable;

import nlpir.NLpir;

public class SegWordWithPosTask implements Callable<String> {
	private String dataSegment;
	private NLpir nlpir;

	public SegWordWithPosTask(String sDataSegment,NLpir n) {
		dataSegment = sDataSegment;
		nlpir=n;
	}


	@Override
	public String call() throws Exception {
		String[] dataSourceArray = dataSegment.replaceAll(
				"[\\s\\[\\]，\n~\"!！、:：。,./／{}《》<>（）()?？‘’“”；'`【】;=—+-]", " ")
				.split(" {1,}");
		dataSegment = "";
		for (int i = 0; i < dataSourceArray.length; i++) {
			if (!(dataSourceArray[i].matches("[a-zA-Z]*[0-9]+") && dataSourceArray[i]
					.length() > 5) && dataSourceArray[i].length() > 1) {
				dataSegment += dataSourceArray[i] + " ";
			}
		}
		return nlpir.segmentWordsFromMem(dataSegment);
	}

}
