package com.example.coolweather.activity;

import com.example.coolweather.R;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {
	private LinearLayout weatherInfoLayout;
	/*用于显示城市名*/
	private TextView cityNameText;
	/*用于显示发布时间*/
	private TextView publishText;
	/*用于显示天气描述信息*/
	private TextView weatherDespText;
	/*用于显示气温1*/
	private TextView temp1Text;
	/*用于显示气温2*/
	private TextView temp2Text;
	/*用于显示当前日期*/
	private TextView currentDateText;
	/*切换城市按钮*/
	private Button switchCity;
	/*更改天气按钮*/
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText=(TextView) findViewById(R.id.city_name);
		publishText=(TextView) findViewById(R.id.publish_text);
		weatherDespText=(TextView) findViewById(R.id.weather_desp);
		temp1Text=(TextView) findViewById(R.id.temp1);
		temp2Text=(TextView) findViewById(R.id.temp2);
		currentDateText=(TextView) findViewById(R.id.current_date);
		switchCity=(Button) findViewById(R.id.switch_city);
		refreshWeather=(Button) findViewById(R.id.refresh_weather);
		String countyCode=getIntent().getStringExtra("county_code");//尝试从Intent中取出县级代号
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号就去查询天气
			publishText.setText("同步中 . . .");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			//没有县级代号时就直接显示本地天气
			showWeather();
		}
		/*调用setOnClickListener()方法注册点击事件*/
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	/*查询县级代号所对应的天气代号*/
	private void queryWeatherCode(String countyCode) {
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";//拼装地址
		queryFromServer(address,"countyCode");//查询县级代号所对应的天气代号
	}
	/*查询天气代号所对应的天气*/
	private void queryWeatherInfo(String weatherCode) {
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";//拼装地址
		queryFromServer(address,"weatherCode");//查询天气代号所对应的天气信息
	}
	/*根据传入的地址和类型去向服务器查询天气代号或者天气信息*/
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(final String response) {//对服务器返回的数据进行解析
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据中解析出天气代号
						String[] array=response.split("\\|");
						if(array!=null&&array.length==2){
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);//传入天气代号
						}
					}
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);//使用JSONObject将数据全部解析出来
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							showWeather();//显示信息
						}});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("同步失败");
					}});
			}});
	}
	/*从SharedPreferences文件中读取存储的天气信息，并显示到界面上*/
	private void showWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);//启动AutoUpdateService服务(一旦选中了某个城市并成功跟新天气之后，AutoUpdateService就会一直在后台运行，并保证每8个小时更新一次天气)
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city://点击切换城市按钮
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);//目前已经选择过一个城市，为了防止直接跳转回来，在Intent中加一个from_weather_activity标志位
			startActivity(intent);//跳转到ChooseAreaActivity
			finish();
			break;
		case R.id.refresh_weather://点击更新天气按钮
			publishText.setText("同步中 . . .");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);//从SharedPreferences文件中读取天气代号
			String weatherCode=prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);//调用queryWeatherInfo()方法去更新天气
			}
			break;
		default:
			break;
		}
	}
	
}
