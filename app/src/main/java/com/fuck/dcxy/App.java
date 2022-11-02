package com.fuck.dcxy;

import android.app.Application;

import com.fuck.dcxy.model.UserDataViewModel;

public class App extends Application{
	
	public UserDataViewModel userDataViewModel;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		userDataViewModel = new UserDataViewModel(this);
		
	}
}

