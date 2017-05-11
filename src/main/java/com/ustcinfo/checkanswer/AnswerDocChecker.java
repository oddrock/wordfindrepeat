package com.ustcinfo.checkanswer;

import java.io.IOException;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class AnswerDocChecker {

	public static void main(String[] args) throws Exception {
		String file1Path = "C:/_Download/附件1-中国移动ERP系统集中化一期工程-集成平台-需求说明书-点对点应答.docx";
		OPCPackage opcPackage = POIXMLDocument.openPackage(file1Path);
		POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
		String content = extractor.getText();
		String[] contentArr = content.split("\n");
		System.out.println(contentArr.length);
		for (int i=0; i<contentArr.length; i++){
			System.out.println(i+": " + contentArr[i]);
		}
	}

}
