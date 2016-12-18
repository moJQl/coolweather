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
		new Thread(new Runnable(){//����һ�����߳�

			@Override
			public void run() {
				updateWeather();//����updateWeather()��������������
			}}).start();
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);//������ʱ����
		//int anHour=1*1000;//8Сʱ�ĺ�����
		int anHour=8*60*1000;//8Сʱ�ĺ�����
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;//ʱ��������Ϊ8Сʱ
		Intent i=new Intent(this,AutoUpdateService.class);//8Сʱ��ִ�е�AutoUpdateService��onCreate()������
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	/*����������Ϣ*/
	private void updateWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);//��SharedPreferences�ļ��ж�ȡ��������
		String weatherCode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";//ƴװ��ַ
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);//�����������ص���������Uility��handleWeatherResponse()����ȥ���������µ�������Ϣ�洢��SharedPreferences�ļ���
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}});
	}
	
}
