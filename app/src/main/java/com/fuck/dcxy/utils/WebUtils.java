package com.fuck.dcxy.utils;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtils{
	
	@NonNull
	public static String inputStream2string(@NonNull InputStream inputStream){
		try{
			int len;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			while((len = inputStream.read(buffer)) != -1) outStream.write(buffer, 0, len);
			inputStream.close();
			return outStream.toString();
		}catch(IOException e){
			return "";
		}
	}
	
	@NonNull
	public static String doGet(String url, String... params) throws IOException{
		HttpURLConnection connection = get(url, params);
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			return inputStream2string(connection.getInputStream());
		}else{
			return "";
		}
	}
	
	@NonNull
	public static HttpURLConnection get(String url, String... params) throws IOException{
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		
		connection.setInstanceFollowRedirects(false);
		
		connection.setRequestMethod("GET");
		
		if(params.length != 0){
			for(int i = 0; i < params.length; i += 2){
				connection.setRequestProperty(params[i], params[i + 1]);
			}
		}
		
		connection.connect();
		
		return connection;
	}
	
	@NonNull
	public static String doPost(String url, String data, String... params) throws IOException{
		HttpURLConnection connection = post(url, data, params);
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			return inputStream2string(connection.getInputStream());
		}else{
			return "";
		}
	}
	
	@NonNull
	public static HttpURLConnection post(String url, String data, String... params) throws IOException{
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		
		connection.setRequestMethod("POST");
		
		if(params.length != 0){
			for(int i = 0; i < params.length; i += 2){
				connection.setRequestProperty(params[i], params[i + 1]);
			}
		}
		
		connection.getOutputStream().write(data.getBytes());
		
		connection.connect();
		
		return connection;
	}
	
}
