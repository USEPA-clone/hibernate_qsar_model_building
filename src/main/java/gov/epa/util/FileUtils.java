package gov.epa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {
	/**
	 * Helper method to returns file path and name with extension omitted
	 * @param name File name 
	 * @return File name without extension
	 */
	public static String getFileNameWithoutExtension(String name) {
		int pos = name.lastIndexOf('.');
		if (pos > 0 && pos < (name.length() - 1)) {
			// there is a '.' and it's not the first, or last character.
			return name.substring(0, pos);
		}
		return name;
	}

	/**
	 * Helper method to replace file extension with another one 
	 * @param fileName - name of the file
	 * @param newExt - new extension including ".", e.g. ".pdf"
	 * @return File name with new extension 
	 */
	public static String replaceExtension(String fileName, String newExt) {
		String fileNameNoExt = getFileNameWithoutExtension(fileName);
		return fileNameNoExt + newExt;
	}
	
	public static void appendToFile(String filePath, String textToAppend) throws IOException
	{
	    Path path = Paths.get(filePath);
	    Files.write(path, textToAppend.getBytes(), StandardOpenOption.APPEND);
	}
	
	
	
	public static int copyFile(File SrcFile, File DestFile) {

		try {

//			System.out.println(SrcFile.getAbsolutePath());
//			System.out.println(DestFile.getAbsolutePath());

			DestFile.getParentFile().mkdirs();
			FileChannel in = new FileInputStream(SrcFile).getChannel();

			FileChannel out = new FileOutputStream(DestFile).getChannel();

			in.transferTo(0, (int) in.size(), out);
			in.close();
			out.close();

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}
}
