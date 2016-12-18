package com.example.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

/*服务器返回的省市县数据都是“代号|城市，代号|城市”这种格式的，所以我们需要提供一个工具来解析和处理这种数据*/
public class Utility {//提供一个工具来解析和处理数据
	/*解析和处理服务器返回的省级数据*/
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){//解析规则：
			String[] allProvinces=response.split(",");//1.先按‘，’分隔
					if(allProvinces!=null&&allProvinces.length>0){
						for(String p : allProvinces){
							String[] array=p.split("\\|");//2.再按单竖线分隔
							Province province=new Province();
							province.setProvinceCode(array[0]);//3.将解析出来的数据设置到实体类中
							province.setProvinceName(array[1]);
							//将解析出来的数据存储到Province表中
							coolWeatherDB.saveProvince(province);//4.调用CoolWeatherDB中的save()方法将数据存储到相应的表中。
						}
						return true;
					}
		}
		return false;
	}
	/*解析和处理服务器返回的城市数据*/
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
					if(allCities!=null&&allCities.length>0){
						for(String c : allCities){
							String[] array=c.split("\\|");
							City city=new City();
							city.setCityCode(array[0]);
							city.setCityName(array[1]);
							city.setProvinceId(provinceId);
							//将解析出来的数据存储到City表中
							coolWeatherDB.saveCity(city);
						}
						return true;
					}
		}
		return false;
	}
	/*解析和处理服务器返回的县级数据*/
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] Counties=response.split(",");
					if(Counties!=null&&Counties.length>0){
						for(String c : Counties){
							String[] array=c.split("\\|");
							County county=new County();
							county.setCountyCode(array[0]);
							county.setCountyName(array[1]);
							county.setCityId(cityId);
							//将解析出来的数据存储到County表中
							coolWeatherDB.saveCounty(county);
						}
						return true;
					}
		}
		return false;
	}
	/*解析服务器返回的JSON数据，并将解析出的数据存储到本地*/
	public static void handleWeatherResponse(Context context,String response){//解析JSON格式的全部天气信息
		try{//使用JSONObject将数据全部解析出来
			JSONObject jsonObject=new JSONObject(response);
			JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
			String cityName=weatherInfo.getString("city");
			String weatherCode=weatherInfo.getString("cityid");
			String temp1=weatherInfo.getString("temp1");
			String temp2=weatherInfo.getString("temp2");
			String weatherDesp=weatherInfo.getString("weather");
			String publishTime=weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	/*将服务器返回的所有天气信息存储到SharedPreferences文件中*/
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {//调用此方法将所有天气都存储到SharedPreferences文件中（除了天气信息外还存储了city_selected标志位，以此来辨别当前是否已选中了一个城市）
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
