package com.fuck.dcxy.ui;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fuck.dcxy.R;
import com.fuck.dcxy.bean.UserData;
import com.fuck.dcxy.model.UserDataViewModel;
import com.fuck.dcxy.utils.WebUtils;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends BaseActivity{
	
	private boolean isRunning;
	
	private MaterialDialog dialog;
	
	private TextInputLayout phone;
	
	private TextInputLayout password;
	
	private UserDataViewModel userDataViewModel;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		
		phone = findViewById(R.id.login_phone);
		password = findViewById(R.id.login_password);
		
		findViewById(R.id.login_login).setOnClickListener(v -> login());
		
		userDataViewModel = UserDataViewModel.getInstance(this);
		userDataViewModel.getUserLiveData().observe(this, userData -> {
			phone.getEditText().setText(userData.phone);
			password.getEditText().setText(userData.password);
		});
		
		dialog = new MaterialDialog.Builder(this)
				.progress(true,0)
				.content("请稍候")
				.canceledOnTouchOutside(false)
				.build();
	}
	
	private void login(){
		
		if(isRunning) return;

		if(TextUtils.isEmpty(phone.getEditText().getText())){
			phone.setError("请输入手机号码");
			return;
		}else{
			phone.setError(null);
		}
		
		if(TextUtils.isEmpty(password.getEditText().getText())){
			password.setError("请输入密码");
			return;
		}else{
			password.setError(null);
		}
		
		dialog.show();
		
		new Thread(){
			@Override
			public void run(){
				isRunning = true;
				try{
					UserData data = userDataViewModel.getUserData();
					data.phone = phone.getEditText().getText().toString();
					data.password = password.getEditText().getText().toString();
					
					JSONObject js = new JSONObject(WebUtils.doPost(
							"https://dcxy-customer-app.dcrym.com/app/customer/login",
							new JSONObject().put("loginAccount", data.phone).put("password", data.password).toString(),
							"clientsource", "{}", "Content-Type", "application/json"));
					
					if(js.getInt("code") == 1000){
						data.token = js.getJSONObject("data").getString("token");
						userDataViewModel.login(data);
						runOnUiThread(() -> {
							toast(R.drawable.tips_finish, "登录成功");
							dialog.dismiss();
							finish();
						});
					}else{
						String msg = js.getString("msg");
						runOnUiThread(() -> {
							toast(R.drawable.tips_error, "登陆失败: " + msg);
							dialog.dismiss();
						});
					}
					
				}catch(JSONException | IOException e){
					runOnUiThread(() -> {
						toast(R.drawable.tips_error, "登陆失败");
						dialog.dismiss();
					});
				}
				isRunning = false;
			}
		}.start();
	}
}
