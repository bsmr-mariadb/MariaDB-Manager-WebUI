package com.skysql.consolev;


import org.vaadin.artur.icepush.ICEPush;

import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.SystemProperties;
import com.skysql.consolev.api.UserLogin;

public class SessionData {

	private SystemInfo systemInfo;
	private SystemProperties systemProperties;
	private UserLogin userLogin;
	private ICEPush pusher;
	
	
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}
	public void setSystemInfo(SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
	}
	public SystemProperties getSystemProperties() {
		return systemProperties;
	}
	public void setSystemProperties(SystemProperties systemProperties) {
		this.systemProperties = systemProperties;
	}
	public UserLogin getUserLogin() {
		return userLogin;
	}
	public void setUserLogin(UserLogin userLogin) {
		this.userLogin = userLogin;
	}
	public ICEPush getICEPush() {
		return pusher;
	}

	public SessionData(SystemInfo systemInfo, SystemProperties systemProperties) {
		this.systemInfo = systemInfo;
		this.systemProperties = systemProperties;
		pusher = new ICEPush();
	}
	
	
}

