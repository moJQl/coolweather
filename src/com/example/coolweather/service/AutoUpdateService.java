package com.example.coolweather.service;

import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent,int flags,int startId){
		new Thread(new Runnable(){//开启一个子线程

			@Override
			public void run() {
				updateWeather();//调用updateWeather()方法来更新天气
			}}).start();
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);//创建定时任务
		//int anHour=1*1000;//8小时的毫秒数
		int anHour=8*60*1000;//8小时的毫秒数
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;//时间间隔设置为8小时
		Intent i=new Intent(this,AutoUpdateService.class);//8小时后执行到AutoUpdateService的onCreate()方法中
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	/*更新天气信息*/
	private void updateWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);//从SharedPreferences文件中读取天气代号
		String weatherCode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";//拼装地址
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);//将服务器返回的天气交给Uility的handleWeatherResponse()方法去处理，把最新的天气信息存储到SharedPreferences文件中
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}});
	}
	
}
