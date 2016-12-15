package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

/*���������ص�ʡ�������ݶ��ǡ�����|���У�����|���С����ָ�ʽ�ģ�����������Ҫ�ṩһ�������������ʹ�����������*/
public class Utility {//�ṩһ�������������ʹ�������
	/*�����ʹ�����������ص�ʡ������*/
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){//��������
			String[] allProvinces=response.split(",");//1.�Ȱ��������ָ�
					if(allProvinces!=null&&allProvinces.length>0){
						for(String p : allProvinces){
							String[] array=p.split("\\|");//2.�ٰ������߷ָ�
							Province province=new Province();
							province.setProvinceCode(array[0]);//3.�������������������õ�ʵ������
							province.setProvinceName(array[1]);
							//���������������ݴ洢��Province����
							coolWeatherDB.saveProvince(province);//4.����CoolWeatherDB�е�save()���������ݴ洢����Ӧ�ı��С�
						}
						return true;
					}
		}
		return false;
	}
	/*�����ʹ�����������صĳ�������*/
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
							//���������������ݴ洢��City����
							coolWeatherDB.saveCity(city);
						}
						return true;
					}
		}
		return false;
	}
	/*�����ʹ�����������ص��ؼ�����*/
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
							//���������������ݴ洢��County����
							coolWeatherDB.saveCounty(county);
						}
						return true;
					}
		}
		return false;
	}
}
