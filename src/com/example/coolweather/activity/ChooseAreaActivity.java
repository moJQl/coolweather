package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {//遍历省市县数据
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	/*省列表*/
	private List<Province> provinceList;
	/*市列表*/
	private List<City> cityList;
	/*县列表*/
	private List<County> countyList;
	/*选中的省份*/
	private Province selectedProvince;
	/*选中的城市*/
	private City selectedCity;
	/*当前选中的级别*/
	private int currentLevel;
	/*处理标志位：是否从WeatherActivity中跳转过来*/
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle  savedInstanceState){
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		//从ChooseAreaActivity跳转到WeatherActivity
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//已经选择了城市且不是从从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){//读取city_selected标志位，若为true，则表明当前已选择过城市，直接跳转到WeatherActivity
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleText=(TextView) findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);//初始化ArrayAdapter
		listView.setAdapter(adapter);//将ArrayAdapter设置为listView的适配器
		coolWeatherDB=CoolWeatherDB.getInstance(this);//获取CoolWeatherDB的实例
		listView.setOnItemClickListener(new OnItemClickListener(){//listView的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if(currentLevel==LEVEL_PROVINCE){//根据当前级别判断调用市级查询还是县级查询
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY){//判断当前级别为LEVEL_COUNTY，就启动WeatherActivity，并传递当前选中的县级代号
					String countyCode=countyList.get(position).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
			});
		queryProvinces();//加载省级数据
	}
	
	/*查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询*/
	private void queryProvinces() {
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){//读取到数据了就将数据显示到界面上
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");//如果没有读取到数据，则调用queryFromServer()方法从服务器上查询数据
		}
	}

	/*查询选中的省内所有市，优先从数据库查询，如果没有查询到再去服务器上查询*/
	protected void queryCities() {
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/*查询选中的市内所有县，优先从数据库查询，如果没有查询到再去服务器上查询*/
	protected void queryCounties() {
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county : countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	/*根据传入的代号和类型从服务器上查询省市县数据*/
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){//根据传入的参数拼装查询地址
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){//调用HttpUtil的sendHttpRequest()方法来向服务器发送请求，响应的数据回调到onFinish()方法中
			@Override
			public void onFinish(String response) {//调用Utility的handleProvincesResponse()方法来解析和处理服务器返回的数据，并存储到数据库
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();//重新加载省级数据
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
						});
				}
			}

			@Override
			public void onError(Exception e) {//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}});
			}});
	}
	/*显示进度对话框*/
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/*关闭进度对话框*/
	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/*捕获back键，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出*/
	public void onBackPressed(){//重写onBackPressed()方法覆盖默认的back键行为
		if(currentLevel==LEVEL_COUNTY){//如果当前列表为县级列表则返回市级列表
			queryCities();
		}else if(currentLevel==LEVEL_CITY){//如果当前列表为市级列表则返回省级列表
			queryProvinces();
		}else{//退出
			if(isFromWeatherActivity){
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
