package com.example.coolweather.util;

public interface HttpCallbackListener {//HttpCallbackListener接口，回调服务返回的结果
	void onFinish(String response);
	void onError(Exception e);
}
