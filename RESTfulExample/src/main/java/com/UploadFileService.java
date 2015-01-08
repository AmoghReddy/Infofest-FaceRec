package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONException;

import Luxand.FSDK;
import fidelity.util.Constants;
import fidelity.util.TemplateUtil;

@Path("/face")
public class UploadFileService {

	private final String UPLOADED_FILE_PATH = "";
	//private final ArrayList<FSDK.FSDK_FaceTemplate.ByReference> templates  = new ArrayList<FSDK.FSDK_FaceTemplate.ByReference>();;
	//private final HashMap<FSDK.FSDK_FaceTemplate.ByReference, String> fileNameMap = new HashMap<FSDK.FSDK_FaceTemplate.ByReference, String>();
	
	private void initializeSystem(){
		try {
			int r = FSDK
					.ActivateLibrary("sAfTXr/EfgPVngjjgFIB8RZ7rsVQeQxk74A4Fa1HSlALu+V2YLlQERdFVhPug9GLXxGWJHpOmWVpfSRdjatlvkXuYQC8Cc2+t7oBCQ+DJ9Nkmmi3a/cHMo88fY0rfY16L8g1TgApVu46tVQsTUSdD9j2Q+GjdKTVCjVLRg2qvSg=");
			if (r != FSDK.FSDKE_OK) {
				System.out.println("Activation key expired");
				//System.exit(r);
			}
		} catch (java.lang.UnsatisfiedLinkError e) {
			System.out.println("Something wrong with activation");
			//System.exit(1);
		}
		FSDK.Initialize();
		
		TemplateUtil.generateTemplates();
	}
	
	@GET
	@Path("/init")
	public Response init(){
		
		initializeSystem();
		return Response.status(200).entity("The Templates are generated.").build();
	}
	
		
	@POST
	@Path("/register")
	@Consumes("multipart/form-data")
	public Response register(final MultipartFormDataInput input) {

		String fileName = Constants.STRING_EMPTY;
		String personName = Constants.STRING_EMPTY;
		String corpID = Constants.STRING_EMPTY;
		final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		
		if (TemplateUtil.templates == null || TemplateUtil.templates.isEmpty()){
			System.out.println("Calling initializeSystem to generate templates");
			initializeSystem();
		}
		
		try {
			if (uploadForm != null) {
				if (uploadForm.get("name") != null) {
					personName = uploadForm.get("name").get(0).getBodyAsString();
				}
				if (uploadForm.get("corpid") != null) {
					corpID = uploadForm.get("corpid").get(0).getBodyAsString();
				}
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		
		List<InputPart> nameParts = uploadForm.get("name");
		for (InputPart namePart : nameParts) {
			MultivaluedMap<String, String> header = namePart.getHeaders();
			
		}
		List<InputPart> inputParts = uploadForm.get("face");
		String result = "";
		for (InputPart inputPart : inputParts) {

			try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = corpID+".jpg";//getFileName(header);
				
				//convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class,null);

				byte [] bytes = IOUtils.toByteArray(inputStream);
				
				//constructs upload file path
				fileName = UPLOADED_FILE_PATH + fileName;
				writeFile(bytes,fileName);
				//writeFile(bytes,"C:\\test.png");
				writeFile(bytes,TemplateUtil.dirPath+"\\"+corpID+".jpg");
				TemplateUtil.addTemplate(fileName);
				writePropertiesToFile(personName,corpID);
				result += "Successfuly added. Size = "+TemplateUtil.templates.size();
				//System.out.println("Done");

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return Response.status(200).entity(result).build();

	}
	
	@POST
	@Path("/recognize")
	@Consumes("multipart/form-data")
	public Response recognize(MultipartFormDataInput input) {

		String fileName = "";
		if (TemplateUtil.templates == null || TemplateUtil.templates.isEmpty()){
			System.out.println("Calling initializeSystem to generate templates");
			initializeSystem();
		}
		else {
			System.out.println("Templates cache size = "+TemplateUtil.templates.size()); 
		}
//		if (templates == null || templates.isEmpty()){
//			System.out.println("Calling Initialize");
//			initializeSystem();
//		}
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("face");
		String result = "{";
		for (InputPart inputPart : inputParts) {

			try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				
				//convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class,null);

				byte [] bytes = IOUtils.toByteArray(inputStream);
				
				//constructs upload file path
				fileName = UPLOADED_FILE_PATH + fileName;
				writeFile(bytes,fileName);
				//writeFile(bytes,"C:\\test.png");
				
				result += templateMatcher(fileName);
				result += getEmotionDetails(fileName);
				//System.out.println("Done");

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		result += "}";
		System.out.println("Final Result : "+result);
		return Response.status(200).entity(result).build();

	}
	
	private String getEmotionDetails(String fileName){
		String result = "";
		
		sample samp = new sample();
		try {
			result = samp.sync_process_image_upload(fileName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	private String templateMatcher(String fileName) {
		
		FSDK.FSDK_FaceTemplate.ByReference faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference();
		FSDK.HImage imageT = new FSDK.HImage();
		File file = new File(fileName);
		
		FSDK.LoadImageFromFile(imageT, file.toString());
		FSDK.GetFaceTemplate(imageT, faceTemplate);
		
		float Similarity[];
		Similarity = new float[1];
		Float tempSimilarity = 0.00f;
		String bestTemplate = "";
		String result = "";

		FSDK.FSDK_FaceTemplate.ByReference faceTemp = null;
		for (int i = 0, n = TemplateUtil.templates.size(); i < n; i++) {
			faceTemp = TemplateUtil.templates.get(i);
			FSDK.MatchFaces(faceTemplate, faceTemp, Similarity);
			//System.out.println(faceTemp.toString());
			//System.out.println((i+1)+" similarity = "+ Similarity[0]);
			if (tempSimilarity < Similarity[0]) {
				tempSimilarity = Similarity[0];
				if (TemplateUtil.fileNameMap.containsKey(TemplateUtil.templates.get(i))) {
					bestTemplate = TemplateUtil.fileNameMap.get(TemplateUtil.templates.get(i)).toString();
				}
			}
		}
		result = geCorpId(bestTemplate);
		//System.out.println("Result : "+result);
		return result;
	}
	
	private String geCorpId(final String image) {
		Properties prop = new Properties();
		FileInputStream in;
		String imageName[];
		String faceData[];
		// String corpId = null;
		String faceDetails = "";
		try {
			//prop.load(getClass().getResourceAsStream(TemplateUtil.propPath));
			in = new FileInputStream(TemplateUtil.propPath);
			prop.load(in);
			in.close();
			System.out.println("image = "+image);
			String imageNameExt = image.substring(image.lastIndexOf('\\') + 1);
			System.out.println("imageNameExt = "+imageNameExt);
			imageName = imageNameExt.split("\\.");
			System.out.println("imageName length = "+imageName.length);
			faceData = prop.getProperty(imageName[0]).split(",");
			if (faceData != null && faceData.length > 0) {
				// corpId = faceData[0] + ","+ faceData[1];
				faceDetails += "\"name\":\""+faceData[0]+"\",";
				faceDetails += "\"corpid\":\""+faceData[1]+"\"";
				//faceDetails += "image:"+image+",";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return faceDetails;
	}

	/**
	 * header sample
	 * {
	 * 		Content-Type=[image/png], 
	 * 		Content-Disposition=[form-data; name="file"; filename="filename.extension"]
	 * }
	 **/
	//get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
		
		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");
				
				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	//save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fop = new FileOutputStream(file);

		fop.write(content);
		fop.flush();
		fop.close();

	}
	
	private void writePropertiesToFile(final String name, final String corpID) {
		FileInputStream in = null;
		final Properties props = new Properties();
		try {
			in = new FileInputStream(TemplateUtil.propPath);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				props.load(in);
				in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(TemplateUtil.propPath);
			props.setProperty(corpID, name+","+corpID);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				props.store(out, null);
				out.close();
				//System.out.println(""+props.getProperty(corpID));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	   
	   
}