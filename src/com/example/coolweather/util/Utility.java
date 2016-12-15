package com.example.coolweather.util;

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
}
