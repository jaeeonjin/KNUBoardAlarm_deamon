package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpConnectionUtils {

	private String BASE_URL;
	
	public HttpConnectionUtils(String url) {
		BASE_URL = url;
	}
	
	private URL getURL(String url) {
		URL urlObj = null;
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return urlObj;
	}

	private HttpURLConnection getConnection(String url, String method) {
		HttpURLConnection conn = null;
		
		try {
			conn = (HttpURLConnection) getURL(url).openConnection();
			conn.setRequestMethod(method.toUpperCase());
			return conn;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	public JSONArray receiveListDataForJson(String url, String method) {
		BufferedReader in = null; 
		JSONArray jsonArr = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(getConnection(url, method).getInputStream(), "UTF-8"));
			String line = in.readLine();
			
			JSONParser parser = new JSONParser();
			Object result = null;
			
			try {
				result = parser.parse(line);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			jsonArr = (JSONArray) result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally { 
			if(in != null) try { in.close(); } catch(Exception e) { e.printStackTrace(); } 
		}
		
		return jsonArr;
	}
	
	public JSONArray callAPIForGET(String apiName) {
		return receiveListDataForJson(BASE_URL+apiName, "GET");
	}
	
	public JSONArray callAPIForPOST(String apiName) {
		return receiveListDataForJson(BASE_URL+apiName, "POST");
	}

}
