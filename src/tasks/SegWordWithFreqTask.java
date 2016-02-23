package tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import entities.WordEntity;

import nlpir.NLpir;

public class SegWordWithFreqTask implements Callable<String> {
	private String dataSegment;
	private NLpir nlpir;
	public static Map<String,WordEntity> weMap;

	public SegWordWithFreqTask(String sDataSegment,NLpir n) {
		dataSegment = sDataSegment;
		nlpir=n;
		weMap=new HashMap<String,WordEntity>();
	}

	public String getClearDataSegment() {
		return dataSegment;
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
		return nlpir.getWordFreqFromMem(dataSegment);

	}

}
