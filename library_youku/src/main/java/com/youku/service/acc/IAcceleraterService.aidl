package com.youku.service.acc;

interface IAcceleraterService{
	void start();
	void stop();
	int getHttpProxyPort();
	String getAccPort();
	int pause();
	int resume();
	int isAvailable();
	String getVersionName();
	int getVersionCode();
	boolean isACCEnable();
	int getCurrentStatus();
}