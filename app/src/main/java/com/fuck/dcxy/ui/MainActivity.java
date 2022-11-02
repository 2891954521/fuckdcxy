package com.fuck.dcxy.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fuck.dcxy.R;
import com.fuck.dcxy.bean.UserData;
import com.fuck.dcxy.model.UserDataViewModel;
import com.fuck.dcxy.utils.ParamUtils;
import com.fuck.dcxy.utils.WebUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends BaseActivity{
	
	private boolean isRunning;
	
	private boolean isShowingBigCode;
	
	private Toolbar toolbar;
	
	private ImageView arrow;
	
	private ImageView image;
	
	private LinearLayout bottom;
	
	private SeekBar brightnessSeekBar;
	
	private MaterialDialog dialog;
	
	private SharedPreferences sp;
	
	private UserDataViewModel userDataViewModel;
	
	private WindowManager.LayoutParams layoutParams;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sp = getSharedPreferences("data", Context.MODE_PRIVATE);
		
		initViews();
		
		initBrightness();
		
		userDataViewModel = UserDataViewModel.getInstance(this);
		userDataViewModel.getDrinkCodeLiveData().observe(this , code -> createCode());
		userDataViewModel.getLoginSuccessLiveData().observe(this, loginSuccess -> refreshCode());

		if(userDataViewModel.getUserData().token == null){
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
		}
	}
	
	private void initViews(){
		image = findViewById(R.id.code);
		bottom = findViewById(R.id.bottom);
		toolbar = findViewById(R.id.toolbar);
		arrow = findViewById(R.id.fill_screen_icon);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("请稍候").canceledOnTouchOutside(false).build();
		
		toolbar.setOnMenuItemClickListener(item -> {
			if(item.getItemId() == R.id.main_login){
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
			}
			return true;
		});
		findViewById(R.id.refresh_code).setOnClickListener(v -> refreshCode());
		
		findViewById(R.id.fill_screen).setOnClickListener(v -> {
			if(isShowingBigCode){
				hideCode();
			}else{
				showCode();
			}
		});
		
		brightnessSeekBar = findViewById(R.id.main_brightness);
		brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				setBrightness(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar){ }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){
				sp.edit().putInt("drinkBrightness", seekBar.getProgress()).apply();
			}
		});
	}
	
	private void initBrightness(){
		layoutParams = getWindow().getAttributes();
		
		int brightness = sp.getInt("drinkBrightness", (int)layoutParams.screenBrightness);
		
		setBrightness(brightness);
		brightnessSeekBar.setProgress(brightness);
	}
	
	private void setBrightness(int paramInt){
		layoutParams.screenBrightness = paramInt / 255f;
		getWindow().setAttributes(layoutParams);
	}
	
	/**
	 * 联网刷新条形码
	 */
	private void refreshCode(){
		
		if(isRunning) return;
		
		if(userDataViewModel.getUserData().token == null){
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			return;
		}
		
		dialog.show();
		new Thread(){
			@Override
			public void run(){
				isRunning = true;
				UserData userData = userDataViewModel.getUserData();
				try{
					
					// 验证token有效性
					JSONObject js = new JSONObject(WebUtils.doPost(
							"https://dcxy-customer-app.dcrym.com/app/customer/login",
							new JSONObject().put("loginTime", String.valueOf(System.currentTimeMillis())).toString(),
							"clientsource", "{}", "token", userData.token, "Content-Type", "application/json"));
					
					if(js.getInt("code") != 1000){
						userData.token = null;
						userDataViewModel.updateUserData(userData);
						runOnUiThread(() -> {
							toast(R.drawable.tips_warning, "登陆状态过期，请重新登陆");
							startActivity(new Intent(MainActivity.this, LoginActivity.class));
							dialog.dismiss();
						});
					}else{
						js = new JSONObject(WebUtils.doGet(
								"https://dcxy-customer-app.dcrym.com/app/customer/flush/idbar",
								"clientsource", "{}", "token", userData.token));
						
						String data = js.getString("data");
						String code = data.substring(0, data.length() - 1) + "3";
						userDataViewModel.updateDrinkCode(code);
						
						runOnUiThread(() -> dialog.dismiss());
					}
				}catch(JSONException | IOException e){
					runOnUiThread(() -> {
						toast(R.drawable.tips_error, "获取饮水码失败:" + e.getMessage());
						dialog.dismiss();
					});
				}
				isRunning = false;
			}
		}.start();
	}
	
	/**
	 * 创建条形码
	 */
	private void createCode(){
		String code = userDataViewModel.getDrinkCode();
		if(code == null) return;
		try{
			BitMatrix bitMatrix = new MultiFormatWriter().encode(code, BarcodeFormat.CODE_128, 1000, 200);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			int[] pixels = new int[width * height];
			for(int y = 0; y < height; y++){
				int offset = y * width;
				for(int x = 0; x < width; x++){
					pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			image.setImageBitmap(bitmap);
		}catch(WriterException ignored){ }
	}
	
	private void hideCode(){
		isShowingBigCode = false;
		toolbar.setVisibility(View.VISIBLE);
		Animation toolbarAnim = new TranslateAnimation(0, 0, - ParamUtils.dp2px(this, 56), 0);
		toolbarAnim.setDuration(getResources().getInteger(R.integer.anim));
		
		Animation imageScale = new ScaleAnimation(1f, 1f, 1f, 0.4717f, 0f, ParamUtils.dp2px(this, 106));
		imageScale.setDuration(getResources().getInteger(R.integer.anim));
		
		Animation animation2 = new TranslateAnimation(0, 0, 0, - ParamUtils.dp2px(MainActivity.this, 56));
		animation2.setDuration(getResources().getInteger(R.integer.anim));
		
		Animation arrowAnim = new RotateAnimation(0f, 180f, arrow.getWidth() / 2f, arrow.getHeight() / 2f);
		arrowAnim.setDuration(getResources().getInteger(R.integer.anim));
		
		imageScale.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				arrow.setRotation(-90f);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ParamUtils.dp2px(MainActivity.this, 100));
				layoutParams.setMargins(0, ParamUtils.dp2px(MainActivity.this, 56), 0, 0);
				image.setLayoutParams(layoutParams);
				bottom.setPadding(0, ParamUtils.dp2px(MainActivity.this, 156), 0, 0);
				
				arrow.clearAnimation();
				image.clearAnimation();
				bottom.clearAnimation();
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
		toolbar.startAnimation(toolbarAnim);
		image.startAnimation(imageScale);
		arrow.startAnimation(arrowAnim);
		bottom.startAnimation(animation2);
	}
	
	private void showCode(){
		isShowingBigCode = true;
		
		Animation toolbarAnim = new TranslateAnimation(0, 0, 0, - ParamUtils.dp2px(this, 56));
		toolbarAnim.setDuration(getResources().getInteger(R.integer.anim));
		
		Animation imageScale = new ScaleAnimation(1f, 1f, 1f, 2.12f, 0f, ParamUtils.dp2px(this, 50));
		imageScale.setDuration(getResources().getInteger(R.integer.anim));

		Animation bottomAnim = new TranslateAnimation(0, 0, 0, ParamUtils.dp2px(this, 56));
		bottomAnim.setDuration(getResources().getInteger(R.integer.anim));
		
		Animation arrowAnim = new RotateAnimation(0f, 180f, arrow.getWidth() / 2f, arrow.getHeight() / 2f);
		arrowAnim.setDuration(getResources().getInteger(R.integer.anim));
		
		imageScale.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				toolbar.setVisibility(View.GONE);
				arrow.setRotation(90f);
				
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ParamUtils.dp2px(MainActivity.this, 212));
				layoutParams.setMargins(0, 0, 0, 0);
				image.setLayoutParams(layoutParams);
				bottom.setPadding(0, ParamUtils.dp2px(MainActivity.this, 212), 0, 0);
				
				arrow.clearAnimation();
				image.clearAnimation();
				bottom.clearAnimation();
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
		
		toolbar.startAnimation(toolbarAnim);
		image.startAnimation(imageScale);
		arrow.startAnimation(arrowAnim);
		bottom.startAnimation(bottomAnim);
	}
}