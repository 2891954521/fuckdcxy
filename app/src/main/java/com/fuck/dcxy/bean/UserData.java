package com.fuck.dcxy.bean;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserData{
	
	public String phone;
	
	public String password;
	
	public String token;
	
	@NonNull
	public static UserData getUserData(@NonNull Context context){
		SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
		
		UserData userData = new UserData();
		userData.phone = sp.getString("phone", null);
		userData.password = sp.getString("password", null);
		userData.token = sp.getString("token", null);
		return userData;
	}
	
	public static void saveUserData(@NonNull Context context, @NonNull UserData userData){
		SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
		editor.putString("phone", userData.phone);
		editor.putString("password", userData.password);
		editor.putString("token", userData.token);
		editor.apply();
	}
	
	public static String getDrinkCode(@NonNull Context context){
		return context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("code", null);
	}
	
	public static void saveDrinkCode(@NonNull Context context, @Nullable String code){
		context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("code", code).apply();
	}
	
}
