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

public class ChooseAreaActivity extends Activity {//����ʡ��������
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	/*ʡ�б�*/
	private List<Province> provinceList;
	/*���б�*/
	private List<City> cityList;
	/*���б�*/
	private List<County> countyList;
	/*ѡ�е�ʡ��*/
	private Province selectedProvince;
	/*ѡ�еĳ���*/
	private City selectedCity;
	/*��ǰѡ�еļ���*/
	private int currentLevel;
	/*�����־λ���Ƿ��WeatherActivity����ת����*/
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle  savedInstanceState){
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		//��ChooseAreaActivity��ת��WeatherActivity
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//�Ѿ�ѡ���˳����Ҳ��ǴӴ�WeatherActivity��ת�������Ż�ֱ����ת��WeatherActivity
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){//��ȡcity_selected��־λ����Ϊtrue���������ǰ��ѡ������У�ֱ����ת��WeatherActivity
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleText=(TextView) findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);//��ʼ��ArrayAdapter
		listView.setAdapter(adapter);//��ArrayAdapter����ΪlistView��������
		coolWeatherDB=CoolWeatherDB.getInstance(this);//��ȡCoolWeatherDB��ʵ��
		listView.setOnItemClickListener(new OnItemClickListener(){//listView�ĵ���¼�
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if(currentLevel==LEVEL_PROVINCE){//���ݵ�ǰ�����жϵ����м���ѯ�����ؼ���ѯ
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY){//�жϵ�ǰ����ΪLEVEL_COUNTY��������WeatherActivity�������ݵ�ǰѡ�е��ؼ�����
					String countyCode=countyList.get(position).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
			});
		queryProvinces();//����ʡ������
	}
	
	/*��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ*/
	private void queryProvinces() {
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){//��ȡ�������˾ͽ�������ʾ��������
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel=LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");//���û�ж�ȡ�����ݣ������queryFromServer()�����ӷ������ϲ�ѯ����
		}
	}

	/*��ѯѡ�е�ʡ�������У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ*/
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
	/*��ѯѡ�е����������أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ*/
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
	/*���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������*/
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){//���ݴ���Ĳ���ƴװ��ѯ��ַ
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){//����HttpUtil��sendHttpRequest()�����������������������Ӧ�����ݻص���onFinish()������
			@Override
			public void onFinish(String response) {//����Utility��handleProvincesResponse()�����������ʹ�����������ص����ݣ����洢�����ݿ�
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){//ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();//���¼���ʡ������
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
			public void onError(Exception e) {//ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}});
			}});
	}
	/*��ʾ���ȶԻ���*/
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/*�رս��ȶԻ���*/
	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/*����back�������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�*/
	public void onBackPressed(){//��дonBackPressed()��������Ĭ�ϵ�back����Ϊ
		if(currentLevel==LEVEL_COUNTY){//�����ǰ�б�Ϊ�ؼ��б��򷵻��м��б�
			queryCities();
		}else if(currentLevel==LEVEL_CITY){//�����ǰ�б�Ϊ�м��б��򷵻�ʡ���б�
			queryProvinces();
		}else{//�˳�
			if(isFromWeatherActivity){
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
