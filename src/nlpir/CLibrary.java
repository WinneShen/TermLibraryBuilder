package nlpir;

import com.sun.jna.Library;
import com.sun.jna.Native;

// 定义接口CLibrary，继承自com.sun.jna.Library
public interface CLibrary extends Library {
	// 定义并初始化接口的静态变量
	CLibrary Instance = (CLibrary) Native
			.loadLibrary(
					"E:\\Workspaces\\NLPIR2014copy\\sample\\Java\\JNA\\JnaTest_NLPIR\\lib\\win64\\NLPIR",
					CLibrary.class);

	public int NLPIR_Init(byte[] sDataPath, int encoding, byte[] sLicenceCode);

	public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

	public void NLPIR_Exit();

	public void NLPIR_NWI_Start();// New Word Indentification Start

	public int NLPIR_NWI_AddFile(String cs);// 往新词识别系统中添加待识别新词的文本文件

	public boolean NLPIR_NWI_Complete();// 新词识别添加内容结束

	public String NLPIR_NWI_GetResult(boolean bWeightOut);// 获取新词识别的结果

	public int NLPIR_NWI_Result2UserDict();// 新词识别结果转为用户词典,返回 新词结果数目

	public String NLPIR_GetNewWords(String sSentence);

	public String NLPIR_GetFileNewWords(String string,int nMaxKeyLimit,boolean bWeightOut);//default nMaxKeyLimit is 50,bWeightOut is false

	public Double NLPIR_FileProcess(String sSourceFilename,
			String sResultFilename, int bPOStagged);// Process a text file

	public int NLPIR_ImportUserDict(String sFilename, boolean bOverwrite);

	public int NLPIR_SaveTheUsrDic();

	public int NLPIR_AddUserWord(String sWord);
	
	public int NLPIR_DelUsrWord(String sWord);
	
	public String NLPIR_GetKeyWords(String sLine,int nMaxKeyLimit,boolean bWeightOut);//default nMaxKeyLimit is 50,and default bWeightOut is false

	public  String NLPIR_GetFileKeyWords(String sTextFile,int nMaxKeyLimit,boolean bWeightOut);//default nMaxKeyLimit is 50,and default bWeightOut is false

	public String NLPIR_WordFreqStat(String sText);
	
	public String  NLPIR_FileWordFreqStat(String sFilename);


}
