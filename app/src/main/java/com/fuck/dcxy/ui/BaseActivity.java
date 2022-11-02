package com.fuck.dcxy.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fuck.dcxy.R;
import com.gyf.immersionbar.ImmersionBar;

public class BaseActivity extends AppCompatActivity{
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		initStatusBar();
	}
	
	protected void initStatusBar(){
		ImmersionBar immersionBar = ImmersionBar.with(this).transparentBar();
		int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		if(mode == Configuration.UI_MODE_NIGHT_YES){
			// 夜间模式
			immersionBar.statusBarDarkFont(false);
			immersionBar.navigationBarDarkIcon(false);
		} else if(mode == Configuration.UI_MODE_NIGHT_NO){
			// 日间模式
			immersionBar.statusBarDarkFont(true);
			immersionBar.navigationBarDarkIcon(true);
		}
		immersionBar.init();
	}
	
	public final void toast(String msg){
		Toast toast = new Toast(this);
		toast.setText(msg);
		toast.setGravity(Gravity.CENTER, 0 ,0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public final void toast(@DrawableRes int icon, String message){
		FrameLayout layout = (FrameLayout)LayoutInflater.from(this).inflate(R.layout.layout_tips, null);
		((ImageView)layout.findViewById(R.id.tips_icon)).setImageResource(icon);
		((TextView)layout.findViewById(R.id.tips_message)).setText(message);
		Toast toast = new Toast(this);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER, 0 ,0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}
}
