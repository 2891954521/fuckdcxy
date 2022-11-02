package com.fuck.dcxy.model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.fuck.dcxy.App;
import com.fuck.dcxy.bean.UserData;

public class UserDataViewModel extends AndroidViewModel{
	
	private final MutableLiveData<UserData> userData;
	
	private final MutableLiveData<String> drinkCode;
	
	private final MutableLiveData<Boolean> loginSuccess;
	
	public static UserDataViewModel getInstance(@NonNull Context context){
		return ((App)context.getApplicationContext()).userDataViewModel;
	}
	
	public UserDataViewModel(@NonNull Application application){
		super(application);
		
		userData = new MutableLiveData<>();
		userData.setValue(UserData.getUserData(application));
		
		drinkCode = new MutableLiveData<>();
		drinkCode.setValue(UserData.getDrinkCode(application));
		
		loginSuccess = new MutableLiveData<>();
	}
	
	public MutableLiveData<UserData> getUserLiveData(){
		return userData;
	}
	
	public MutableLiveData<String> getDrinkCodeLiveData(){
		return drinkCode;
	}
	
	public MutableLiveData<Boolean> getLoginSuccessLiveData(){
		return loginSuccess;
	}
	
	public UserData getUserData(){
		return userData.getValue();
	}
	
	public String getDrinkCode(){
		return drinkCode.getValue();
	}
	
	public void updateUserData(UserData data){
		userData.postValue(data);
		UserData.saveUserData(getApplication(), data);
	}
	
	public void updateDrinkCode(String code){
		drinkCode.postValue(code);
		UserData.saveDrinkCode(getApplication(), code);
	}
	
	public void login(UserData data){
		userData.postValue(data);
		loginSuccess.postValue(true);
		UserData.saveUserData(getApplication(), data);
	}
	

}
