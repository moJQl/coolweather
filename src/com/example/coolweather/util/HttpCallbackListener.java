package com.example.coolweather.util;

public interface HttpCallbackListener {//HttpCallbackListener�ӿڣ��ص����񷵻صĽ��
	void onFinish(String response);
	void onError(Exception e);
}
