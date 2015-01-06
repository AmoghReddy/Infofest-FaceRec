package com;

import java.util.Iterator;
import java.io.File;

//for reading JSON Output
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//for reading nViso output
import com.mashape.client.http.MashapeResponse;

public class sample {

	/**
	 * @param args
	 */
	private final static String MY_APP_ID = "02dd50a2";
	private final static String MY_APP_KEY = "1a92c1524422295bdcf5b67083d4ede6";
	private final static String MY_APP_SESSION = "FidelitySession";
	private final static String MY_APP_FORMAT = "json";
	//private final static String IMAGE_PATH = "C:\\Workspace\\Pics\\Amogh.jpg";

	public static void main(String[] args) throws JSONException {
		// TODO Auto-generated method stub
		// sync_process_image_upload(s);
		
	}

	public static String sync_process_image_upload(String s) throws JSONException {
		// call the nViso API for processing the image
		nViso3DFIHttpClient client = new nViso3DFIHttpClient(MY_APP_ID,
				MY_APP_KEY);
		MashapeResponse<JSONObject> response = client.processImageByUpload(
				new File(s), MY_APP_SESSION, "0", MY_APP_FORMAT);

		// now you can do something with the response.
		return ProcessEmotions(response);
	}

	public static String ProcessEmotions(MashapeResponse<JSONObject> response)
			throws JSONException {
		JSONObject s = response.getBody();

		Iterator keys = s.keys();
		String emotionDetails = ",";
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (s.get(key) != null) {

				if (key.toString().equals("images")) {
					JSONArray imageArray = (JSONArray) s.get(key);
					JSONObject imageJSONObject = imageArray.getJSONObject(0);
					JSONArray facesArray = imageJSONObject
							.getJSONArray("faces");

					// we are processing only 1 image at a time. So taking the
					// first index is fine
					JSONObject facesObject = facesArray.getJSONObject(0);

					// get the gender value
					String gender = facesObject.getJSONObject("attribute")
							.getString("gender");
					System.out.println(gender);

					// Get the emotion values
					JSONObject emotionObject = facesObject.getJSONObject(
							"attribute").getJSONObject("emotion");
					Double disgust = emotionObject.getDouble("disgust");
					Double sadness = emotionObject.getDouble("sadness");
					Double anger = emotionObject.getDouble("anger");
					Double happiness = emotionObject.getDouble("happiness");
					Double neutral = emotionObject.getDouble("neutral");
					Double surprise = emotionObject.getDouble("surprise");
					Double fear = emotionObject.getDouble("fear");
					System.out.println("DIs:" + disgust + " --Sad" + sadness);
					//emotionDetails =  disgust+","+sadness+","+anger+","+happiness+","+neutral+","+surprise+","+fear;
					emotionDetails += "\"disgust\":\""+disgust+"\",";
					emotionDetails += "\"sadness\":\""+sadness+"\",";
					emotionDetails += "\"anger\":\""+anger+"\",";
					emotionDetails += "\"happiness\":\""+happiness+"\",";
					emotionDetails += "\"neutral\":\""+neutral+"\",";
					emotionDetails += "\"surprise\":\""+surprise+"\",";
					emotionDetails += "\"fear\":\""+fear+"\"";
				}
			}
		}
		return emotionDetails;

	}

}
