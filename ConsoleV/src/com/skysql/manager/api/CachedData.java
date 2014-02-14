package com.skysql.manager.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CachedData {
	/**
	 * (System ID, Node ID, Monitor ID, last modified date in RFC 2822)
	 */
	private HashMap<String, HashMap<String, HashMap<String, String>>> lastModifiedMonitorData;
	/**
	 * (System ID, Node ID, last modified date in RFC 2822)
	 */
	private HashMap<String, HashMap<String, String>> lastModifiedNodeInfo;
	
	public CachedData () {
		lastModifiedMonitorData = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		lastModifiedNodeInfo = new HashMap<String, HashMap<String,String>>();
	}
	
	public String getLastModifiedMonitorData (String systemId, String nodeId, String monitorId) {
		String result;
		try {
			result = lastModifiedMonitorData.get(systemId).get(nodeId).get(monitorId);
		} catch (NullPointerException npe) {
			result = null;
		}
		return result;
	}
	
	public void addLastModifiedMonitorData (String systemId, String nodeId, String monitorId) {
		HashMap<String, HashMap<String, String>> nodeMap = lastModifiedMonitorData.get(systemId);
		if (nodeMap == null) {
			nodeMap = new HashMap<String, HashMap<String,String>>();
		}
		HashMap<String, String> monitorMap;
		try {
			monitorMap = nodeMap.get(nodeId);
			monitorMap.put(monitorId, getNow());
		} catch (NullPointerException npe) {
			monitorMap = new HashMap<String, String>();
		}
		nodeMap.put(nodeId, monitorMap);
		lastModifiedMonitorData.put(systemId, nodeMap);
	}
	
	public String getLastModifiedNodeInfo (String systemId, String nodeId) {
		String result;
		try {
			result = lastModifiedNodeInfo.get(systemId).get(nodeId);
		} catch (NullPointerException npe) {
			result = null;
		}
		return result;
	}
	
	public void addLastModifiedNodeInfo (String systemId, String nodeId) {
		HashMap<String, String> nodeMap = lastModifiedNodeInfo.get(systemId);
		if (nodeMap == null) {
			nodeMap = new HashMap<String, String>();
		}
		nodeMap.put(nodeId, getNow());
		lastModifiedNodeInfo.put(systemId, nodeMap);
	}
	
	
	/**
	 * Returns the current time in the RFC 2822 format.
	 * @return		a string representing the time in the the RFC 2822 format
	 */
	private String getNow() {
		String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date today = new Date();
		String now = format.format(today);
		return now;
	}
}
