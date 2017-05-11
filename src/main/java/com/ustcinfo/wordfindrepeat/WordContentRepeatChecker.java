package com.ustcinfo.wordfindrepeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class WordContentRepeatChecker {
	public static HashSet<String> notRepeatSentences = new  HashSet<String>();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		/*-- 读取文件，分割为句子 --*/
		System.out.println("正在导入文件...");
		String file1Path = "c:\\技术部分-上海才扬.docx";
		String file2Path = "c:\\技术部分-信通+盛世.docx";
		String currentDirPath = new File("").getAbsolutePath();
		// 输出目录
		String exportDirPath = currentDirPath;
		// 目录下的excel文件会被读入学习，被认为不是重复的句子就不会被判重。
		String excludeDirPath = currentDirPath;
		if(args.length>=2){
			file1Path = args[0];
			file2Path = args[1];
		}
		if(args.length>=3){
			exportDirPath = args[2];
		}
		if(args.length>=4){
			excludeDirPath = args[3];
		}
		String file1Name = parseFileName(file1Path);
		String file2Name = parseFileName(file2Path);
		String[] sa1 = parseWord2SentenceArray(file1Path);
		String[] sa2 = parseWord2SentenceArray(file2Path);
		
		/*-- 从指定目录的excel中学习所有可以被排除在外不算重复的句子 --*/
		if(excludeDirPath.trim().length()>0){
			for (String excludeFilePath : getExcelPathFromDir(excludeDirPath)){
				learnExcludeSentences(excludeFilePath);
			}
		}
		
		
		/*-- 数据准备、excel文件准备 --*/
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("sheet1");
		sheet.createFreezePane(0, 1, 0, 1);		// 冻结excel首行
		sheet.setColumnWidth(0, 1000);
	    sheet.setColumnWidth(1, 18500);
	    sheet.setColumnWidth(2, 19000);
	    sheet.setColumnWidth(4, 2000);
	    DecimalFormat df=new DecimalFormat("######0.00");
		HSSFCellStyle style = wb.createCellStyle();
		style.setWrapText(true);// 自动换行
		HSSFRow row;
		HSSFCell cell;
		int count = 0;
		row = sheet.createRow(count++);
		int col = 0;
		cell = row.createCell(col++);
		cell.setCellStyle(style);
		cell.setCellValue("序号");
		cell = row.createCell(col++);
		cell.setCellStyle(style);
		cell.setCellValue(file1Name);
		cell = row.createCell(col++);
		cell.setCellStyle(style);
		cell.setCellValue(file2Name);
		cell = row.createCell(col++);
		cell.setCellStyle(style);
		cell.setCellValue("疑似重复度");
		cell = row.createCell(col++);
		cell.setCellStyle(style);
		cell.setCellValue("确认是否重复");
		double sim ;
		String s1;
		String s2;
		
		
		/*-- 开始判重 --*/
		System.out.println("正在判重...");
		for (int i=0;i<sa1.length; i++){
			s1 = sa1[i];
			if(s1.length()<15){
				continue;
			}
			// 如果句子已经在例外名单里，就不再判重
			if(notRepeatSentences.contains(s1)){
				continue;
			}
			for (int j=0; j<sa2.length; j++){
				s2 = sa2[j];
				if(s2.length()<10){
					continue;
				}
				// 如果句子已经在例外名单里，就不再判重
				if(notRepeatSentences.contains(s2)){
					continue;
				}
				// 判断句子的重复度
				sim = SimFeatureUtil.sim(s1, s2);
				// >0.5说明疑似重复
				if(sim>0.5){
					row = sheet.createRow(count++);
					col = 0;
					cell = row.createCell(col++);
					cell.setCellStyle(style);
					cell.setCellValue(count-1);
					cell = row.createCell(col++);
					cell.setCellStyle(style);
					cell.setCellValue(s1);
					cell = row.createCell(col++);
					cell.setCellStyle(style);
					cell.setCellValue(s2);
					cell = row.createCell(col++);
					cell.setCellStyle(style);
					cell.setCellValue(df.format(sim));
					break;
					
					/*System.out.println("第"+count+"条疑似重复，重复度："+sim);
					System.out.println(file1Name+"："+sa1[i]);
					System.out.println(file2Name+"："+sa2[j]);
					System.out.println("------------------------------------------");*/
				}
			}
		}
		
		/*-- 输出疑似重复excel文件 --*/
		String exportfilePath = exportDirPath+"/疑似重复_"+file1Name+"_"+file2Name+"_"+getTimeStr()+".xls";
		FileOutputStream os = new FileOutputStream(exportfilePath);
		wb.write(os);
		os.close();
		System.out.println("已完成，输出文件地址："+exportfilePath);
	}
	
	/**
	 * 将word内容拆分成句子
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String[] parseWord2SentenceArray(String filePath) throws Exception{
		OPCPackage opcPackage = POIXMLDocument.openPackage(filePath);
		POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
		String content = extractor.getText();
		content = replace(content);
		return content.split("。");
	}
	
	/**
	 * 将句子中多余字符替换掉
	 * @param content
	 * @return
	 */
	public static String replace(String content){
		//content = content.replaceAll(" ", "");
		content = content.replaceAll("\\r\\n", "。");
		content = content.replaceAll("\\n", "。");
		content = content.replaceAll("。。", "。");
		//content = content.replaceAll("\\t", "");
		return content;
	}
	
	/**
	 * 从文件路径中解析得到文件名
	 * @param filePath
	 * @return
	 */
	public static String parseFileName(String filePath){
		File file = new File(filePath);
		String fileName = file.getName();
		fileName = fileName.replace(".docx", "");
		fileName = fileName.replace(".doc", "");
		return fileName;
	}
	
	/**
	 * 学习哪些句子不算重复
	 * @param filePath
	 */
	@SuppressWarnings("resource")
	public static void learnExcludeSentences(String filePath) throws Exception{
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filePath));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		int rowNum = sheet.getLastRowNum();
		HSSFRow row;
		HSSFCell cell;
		String cellVal;
		for (int i=0; i<rowNum; i++){
			row = sheet.getRow(i);
			cell = row.getCell(4);
			if(cell==null){
				continue;
			}
			cellVal = cell.getStringCellValue();
			if(cellVal==null){
				continue;
			}
			cellVal = cellVal.trim();
			if(cellVal.equals("否") || cellVal.equalsIgnoreCase("no") || cellVal.equals("不是")){
				cell = row.getCell(1);
				cellVal = cell.getStringCellValue().trim();
				if (cellVal.length()>0){
					notRepeatSentences.add(cellVal);
				}
				cell = row.getCell(2);
				cellVal = cell.getStringCellValue().trim();
				if (cellVal.length()>0){
					notRepeatSentences.add(cellVal);
				}
			}
		}
	}
	
	/**
	 * 获得时间字符串，形如MMddHHmm
	 * @return
	 */
	public static String getTimeStr(){
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmm");
		String timeStr = format.format(new Date());
		return timeStr;
	}
	
	/**
	 * 从目录下获得所有Excel文件的绝对路径
	 * @param path
	 * @return
	 */
	public static List<String> getExcelPathFromDir(String path){
        File file = new File(path);
        File [] files = file.listFiles();
        List<String> nameList = new ArrayList<String>();
        String fileName;
        int index;
        for(File a:files){
        	if(a.isFile() ){
        		fileName = a.getName();
        		index = fileName.indexOf(".xls");
        		if(index>0 && (index+4)==fileName.length()){
        			nameList.add(a.getAbsolutePath());
        		}
        		
        	}
        }
        return nameList;
    }
}
