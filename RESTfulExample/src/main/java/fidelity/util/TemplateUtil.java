package fidelity.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Luxand.FSDK;

/**
 * The Class TemplateUtil.
 * 
 * @author Amogh Reddy
 */
public class TemplateUtil {

	/** The dir path. */
	public static String dirPath = "C:\\Workspace\\Pics";
	public static String propPath = "C:\\Workspace\\facedetails.properties";

	/** The face template. */
	static FSDK.FSDK_FaceTemplate.ByReference faceTemplate;
	
	public static ArrayList<FSDK.FSDK_FaceTemplate.ByReference> templates  = new ArrayList<FSDK.FSDK_FaceTemplate.ByReference>();;
	public static HashMap<FSDK.FSDK_FaceTemplate.ByReference, String> fileNameMap = new HashMap<FSDK.FSDK_FaceTemplate.ByReference, String>();

	/**
	 * Generate templates.
	 * 
	 * @param tempArray  the temp array
	 * @param fileNameMap  the file name map
	 */
 public static void generateTemplates() {
		File files[];
		File folder = new File(dirPath);
		files = folder.listFiles();
		// FSDK.FSDK_FaceTemplate.ByReference faceTemplate;
		FSDK.HImage imageT = new FSDK.HImage();
		Arrays.sort(files);
		for (int i = 0, n = files.length; i < n; i++) {
			faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference();
			FSDK.LoadImageFromFile(imageT, files[i].toString());
			//System.out.println(imageT.toString());
			FSDK.GetFaceTemplate(imageT, faceTemplate);
			templates.add(faceTemplate);
			fileNameMap.put(faceTemplate, files[i].toString());
		}
	}
 
 public static void addTemplate(String filePath){
	 	File file = new File(filePath);
	 	FSDK.HImage imageT = new FSDK.HImage();
	 	faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference();
		FSDK.LoadImageFromFile(imageT, file.toString());
		FSDK.GetFaceTemplate(imageT, faceTemplate);
		templates.add(faceTemplate);
		fileNameMap.put(faceTemplate, file.toString());
 }
 
}
