package nlpir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tasks.SegWordWithFreqTask;
import tasks.SegWordWithPosTask;
import entities.WordEntity;

public class NLpir {

	private String stopWordsPath = "conf/stopWords.txt";

	/**
	 * 初始化
	 * 
	 * @return init_flag
	 */
	public int init() {
		// init NLPIR
		String argu = "";
		String system_charset = "UTF-8";
		int charset_type = 1;
		int init_flag = 0;
		try {
			init_flag = CLibrary.Instance.NLPIR_Init(
					argu.getBytes(system_charset), charset_type,
					"0".getBytes(system_charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return init_flag;
	}

	/**
	 * 读取文件，找出新词，并添加到用户词库
	 * 
	 * @param sFilePath
	 * @return userWordsNum
	 */
	public int addNewWordsFromFile(String sFilePath) {
		String sNewWords = null;
		int userWordsNum = 0;
		int wordsNumOneFind = 0;
		do {
			sNewWords = CLibrary.Instance.NLPIR_GetFileNewWords(sFilePath,
					1000, true);
			String[] userWords = null;
			System.out.println("NewWords:" + sNewWords);
			userWords = sNewWords.split("#");
			wordsNumOneFind = userWords.length;
			userWordsNum += wordsNumOneFind;
			for (int i = 0; i < wordsNumOneFind; i++) {
				userWords[i] = userWords[i].split("/")[0];
				CLibrary.Instance.NLPIR_AddUserWord(userWords[i]);
			}
		} while (wordsNumOneFind > 2);
		CLibrary.Instance.NLPIR_SaveTheUsrDic();
		return userWordsNum;
	}

	/**
	 * 添加用户词汇
	 * 
	 * @param userWord
	 */
	public void addUserWord(String userWord) {
		CLibrary.Instance.NLPIR_AddUserWord(userWord);
	}

	/**
	 * 添加完所有用户词汇后执行，保存用户字典
	 */
	public void saveTheUserDic() {
		CLibrary.Instance.NLPIR_SaveTheUsrDic();
	}

	/**
	 * 删除用户词典中sWord
	 * 
	 * @param sWord
	 * @return sWord不存在则返回-1，else the handle
	 */
	public int deleteANewWord(String sWord) {
		int result = CLibrary.Instance.NLPIR_DelUsrWord(sWord);
		return result;
	}

	/**
	 * 对文件中的文本信息进行分词
	 * 
	 * @param sourcePath
	 * @param resultPath
	 */
	public void segmentWordsFromFile(String sourcePath, String resultPath) {
		CLibrary.Instance.NLPIR_FileProcess(sourcePath, resultPath, 1);
	}

	/**
	 * 对内存中的字符串进行分词
	 * 
	 * @param sSrc
	 */
	public String segmentWordsFromMem(String sSrc) {
		return CLibrary.Instance.NLPIR_ParagraphProcess(sSrc, 1);
	}

	/**
	 * 从源文件文本中取出关键词
	 * 
	 * @param sourcePath
	 */
	public String getKeyWordsFromFile(String sourcePath) {
		return CLibrary.Instance.NLPIR_GetFileKeyWords(sourcePath, 50, false);
	}

	/**
	 * 从内存中的字符串取关键词
	 * 
	 * @param sLine
	 * @return
	 */
	public String getKeyWordsFromMem(String sLine) {
		return CLibrary.Instance.NLPIR_GetKeyWords(sLine, 50, false);
	}

	/**
	 * 从输入的字符串中输出词频
	 * 
	 * @param sText
	 * @return
	 */
	public String getWordFreqFromMem(String sText) {
		return CLibrary.Instance.NLPIR_WordFreqStat(sText);
	}

	/**
	 * 从文件中读取字符串，输出每个词的词频
	 * 
	 * @param sFilename
	 * @return
	 */
	public String getWordFreqFromFile(String sFilename) {
		return CLibrary.Instance.NLPIR_FileWordFreqStat(sFilename);
	}

	/**
	 * 退出
	 */
	public void exit() {
		CLibrary.Instance.NLPIR_Exit();
	}

	// POS-Combination
	private final static int MIN_FREQ = 2;
	private final static int MAX_DATASOURCE_LENGTH = 1000;

	private String getWordsWithPos(String dataSource, NLpir nlpir,
			ExecutorService pool) {
		int beginIndex = 0, len = dataSource.length(), endIndex;
		CompletionService<String> completionService = new ExecutorCompletionService<String>(
				pool);
		// Future[] futures = new Future[(int) Math.ceil((double) len
		// / MAX_DATASOURCE_LENGTH)];
		int futureId = 0;
		while (MAX_DATASOURCE_LENGTH + beginIndex <= len) {
			endIndex = dataSource.indexOf(" ", beginIndex + MAX_DATASOURCE_LENGTH);
			if(endIndex<0){
				endIndex=len;
			}
			Callable<String> task = new SegWordWithPosTask(
					dataSource.substring(beginIndex, endIndex), nlpir);
			// futures[futureId] = pool.submit(task);
			completionService.submit(task);
			futureId++;
			beginIndex = endIndex;
		}
		endIndex = len;
		Callable<String> task = new SegWordWithPosTask(dataSource.substring(
				beginIndex, endIndex), nlpir);
		// futures[futureId] = pool.submit(task);
		completionService.submit(task);
		futureId++;
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < futureId; i++) {
			try {
				result.append(completionService.take().get().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();
		return result.toString();
	}

	/**
	 * 词性组合添加新词
	 * 
	 * @param sourcePath
	 * @param nlpir
	 * @throws IOException
	 */
	public void addNewWordsByPOSCombination(String sourcePath)
			throws IOException {

		// 1.init all var
		String dataSource = null;
		Set<String> stopWordsSet = getStopWordsFromFile(stopWordsPath);

		String[] wordsNoFreq = null;

		// 2.get dataset and clean it
		System.out.println("start to get words wirh POS in pools...");
		dataSource = getStringFromFile(sourcePath);
		ExecutorService executorService = Executors.newCachedThreadPool();// 并发
		String out = getWordsWithPos(dataSource, this, executorService);

		// 3.get words with POS,and regard a combination with first word whose
		// POS is m and second word whose POS is n or q as a new word
		System.out.println("start to get user words...");
		wordsNoFreq = out.split("( |\n)( *\n*)*");
		String[] we;
		String previousPOS = "";
		String[] previousWe = new String[2];
		List<String> stopList = new ArrayList<String>();
		FileWriter userDicFw = new FileWriter("output/userDic.txt");
		String tempWord = "";
		for (int i = 0; i < wordsNoFreq.length; i++) {
			we = wordsNoFreq[i].split("/");
			if (isStopWords(we[0], stopWordsSet)) {
				stopList.add(we[0]);
				continue;
			}
			previousPOS = previousWe[1];
			if (previousPOS != null && we.length > 1 && previousPOS.equals("m")
					&& previousWe[0].length() < 6
					&& (we[1].equals("q") || we[1].equals("n"))) {
				tempWord += previousWe[0];
				for (int j = 0; j < stopList.size(); j++) {
					tempWord += stopList.get(j);
				}
				tempWord += we[0];
				this.addUserWord(tempWord);
				userDicFw.write(tempWord + "\n");
			}
			if (we.length > 1)
				previousWe = we;
			tempWord = "";
			stopList.removeAll(stopList);
		}
		this.saveTheUserDic();
		userDicFw.close();

	}

	// 并发分词
	private final static int MAX_DATASOURCE_CLEANING_LENGTH = 1000;

	private Map<String, WordEntity> getWordsWithFreq(String dataSource,
			NLpir nlpir, ExecutorService pool) {
		int beginIndex = 0, len = dataSource.length(), endIndex;
		CompletionService<String> completionService = new ExecutorCompletionService<String>(
				pool);
		// Future[] futures = new Future[(int) Math.ceil((double) len
		// / MAX_DATASOURCE_CLEANING_LENGTH)];
		int futureId = 0;
		while (MAX_DATASOURCE_CLEANING_LENGTH + beginIndex <= len) {
			endIndex = dataSource.indexOf(" ", beginIndex + MAX_DATASOURCE_LENGTH);
			if(endIndex<0){
				endIndex=len;
			}
			Callable<String> task = new SegWordWithFreqTask(
					dataSource.substring(beginIndex, endIndex), nlpir);
			// futures[futureId] = completionService.submit(task);
			completionService.submit(task);
			futureId++;
			beginIndex = endIndex;
		}
		endIndex = len;
		Callable<String> task = new SegWordWithFreqTask(dataSource.substring(
				beginIndex, endIndex), nlpir);
		// futures[futureId] = completionService.submit(task);
		completionService.submit(task);
		futureId++;
		String wordsWithFreq = "";
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < futureId; i++) {
			try {
				result.append(completionService.take().get().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		wordsWithFreq = result.toString();
		String[] wordsWithFreqArray = wordsWithFreq.split("(#|\n){1,}");
		Map<String, WordEntity> weMap = new HashMap<String, WordEntity>();
		String[] we;
		for (int i = 0; i < wordsWithFreqArray.length; i++) {
			we = wordsWithFreqArray[i].split("/");
			if (we.length < 3)
				continue;
			if (weMap.containsKey(we[0])) {
				weMap.get(we[0]).addCount(Integer.parseInt(we[2]));
			} else {
				weMap.put(we[0],
						new WordEntity(we[0], we[1], Integer.parseInt(we[2])));
			}
		}

		pool.shutdown();
		return weMap;
	}

	private String getStringFromFile(String sourcePath) throws IOException {
		String str = "";
		File file = new File(sourcePath);

		FileInputStream in = new FileInputStream(file);
		// size 为字串的长度 ，这里一次性读完
		int size = in.available();
		byte[] buffer = new byte[size];
		in.read(buffer);
		in.close();
		str = new String(buffer, "UTF-8");
		return str;
	}

	private Set<String> getStopWordsFromFile(String filePath) {
		String txt = null;
		String[] arr;
		Set<String> result = null;
		try {
			txt = getStringFromFile(filePath);
			arr = txt.split("\n");
			for (int i = 0; i < arr.length; i++) {
				arr[i] = arr[i].trim();
			}
			result = new HashSet<String>(Arrays.asList(arr));
		} catch (Exception e) {
			result = new HashSet<String>();
			System.out.println("get stopwords fail.");
		}
		return result;
	}

	private boolean isStopWords(String word, Set<String> stopWordsSet) {
		if (stopWordsSet.contains(word)) {
			return true;
		}
		return false;
	}

	private List<Map.Entry<String, WordEntity>> getSortedEntryList(
			Map<String, WordEntity> oriMap) {
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, WordEntity>> entryList = new ArrayList<Map.Entry<String, WordEntity>>(
					oriMap.entrySet());
			Collections.sort(entryList,
					new Comparator<Map.Entry<String, WordEntity>>() {
						public int compare(Entry<String, WordEntity> entry1,
								Entry<String, WordEntity> entry2) {
							int value1 = 0, value2 = 0;
							try {
								value1 = entry1.getValue().getCount();
								value2 = entry2.getValue().getCount();
							} catch (NumberFormatException e) {
								value1 = 0;
								value2 = 0;
							}
							return value2 - value1;
						}
					});
			return entryList;

		}
		return null;
	}

	/**
	 * 并发分词，并过滤
	 * @param sourcePath
	 * @param resultPath
	 * @return map of word-wordentiy
	 * @throws IOException
	 */
	public Map<String, WordEntity> segmentWordsConcurrently(String sourcePath)
			throws IOException {
		// 1.init all var
		FileWriter fw = null;
		String dataSource = null;
		Set<String> stopWordsSet = getStopWordsFromFile(stopWordsPath);

		// 2.get datamap and clean it
		System.out.println("start to getting words with freq in pools...");
		dataSource = getStringFromFile(sourcePath);
		ExecutorService executorService = Executors.newCachedThreadPool();// 并发
		Map<String, WordEntity> weMap = getWordsWithFreq(dataSource, this,
				executorService);

		System.out.println("start to clean words...");
		List<Map.Entry<String, WordEntity>> entryList = getSortedEntryList(weMap);
		Iterator<Map.Entry<String, WordEntity>> iter = entryList.iterator();
		Map.Entry<String, WordEntity> tmpEntry = null;
		Map<String, WordEntity> sortedMap = new LinkedHashMap<String, WordEntity>();// 按频次排序后并经过过滤的词汇map
		int wordlength = 0;
		// 过滤
		while (iter.hasNext()) {
			tmpEntry = iter.next();
			if (isStopWords(tmpEntry.getKey(), stopWordsSet))
				continue;
			// if (tmpEntry.getValue().getCount() < MIN_FREQ)
			// break;
			wordlength = tmpEntry.getKey().length();
			// 词长范围为2-5和词长在1-5之间的编码词汇被保留，其余被过滤
			if (wordlength > 1
					&& wordlength < 6
					&& !(tmpEntry.getKey().matches("[a-zA-Z]*[0-9]+") && wordlength > 5))
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
		}
//		// 3.write to file and save
//		System.out.println("start to write file...");
//		fw = new FileWriter(resultPath);
//		for (String word : sortedMap.keySet()) {
//			fw.write(word + "\t" + sortedMap.get(word).getCount() + "\n");
//		}
//		fw.close();
		return sortedMap;



	}
	/**
	 * 过滤掉词语出现频次小于阈值的词
	 * @param weMap 传入的weMap必须是按词频从大到小排好序的
	 * @return
	 */
	public Map<String,WordEntity> wordFreqFilter(Map<String,WordEntity> weMap){
		Map<String,WordEntity> resultMap=new HashMap<String,WordEntity>();
		for (WordEntity value : weMap.values()) {  
			if(value.getCount()>MIN_FREQ){
				resultMap.put(value.getWord(), value);
			}else{
				break;
			}
		}
		return resultMap;
	}

}
