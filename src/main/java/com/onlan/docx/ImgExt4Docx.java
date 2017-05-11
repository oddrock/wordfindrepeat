package com.onlan.docx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ImgExt4Docx {
	static final int BUFFER = 2048;
	
	/**
	 * 从Docx文件中解压缩出图片到指定目录下
	 * @param docxFilePath
	 * @param destDirPath
	 * @return
	 */
	public String parseImgFromDocx(String docxFilePath, String destDirPath) {
		try {
			ZipFile zipFile = new ZipFile(docxFilePath);
			Enumeration enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				if (zipEntry.isDirectory()) {
					new File(destDirPath + zipEntry.getName()).mkdirs();
					continue;
				}
				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
				File file = new File(destDirPath + zipEntry.getName());
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
				int count;
				byte[] array = new byte[BUFFER];
				while ((count = bis.read(array, 0, BUFFER)) != -1) {
					bos.write(array, 0, BUFFER);
				}

				bos.flush();
				bos.close();
				bis.close();
			}
			return destDirPath + "word/media";

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String inputFilename = "c:/技术部分-上海才扬.docx";
		String unZipPathname = "c:/_Temp/";
		ImgExt4Docx ied = new ImgExt4Docx();
		System.out.println(ied.parseImgFromDocx(inputFilename, unZipPathname));

	}
}
