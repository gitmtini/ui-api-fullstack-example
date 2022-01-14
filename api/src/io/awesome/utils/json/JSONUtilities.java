package io.awesome.utils.json;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtilities {

	//search
	public static <T> T findType(JSONObject json, Class<T> _type) throws JSONException {
		
		Iterator<String> keys = json.keys();
		String key = null;

		while( (keys.hasNext()) && (key=keys.next())!=null){
			
			if( json.get(key).getClass().isAssignableFrom( _type) )return  (T) json.get(key);
			else if(json.get(key) instanceof JSONArray ) return findType((JSONArray)json.get(key), _type);
		
		}
		
		return null;
	}
	
	public static <T> T findType(JSONArray json, Class <T> _type) throws JSONException {
		
		for(int i = 0 ; i < ((JSONArray)json).length(); i++) {
				
			T res = findType(json.getJSONArray(i),_type);
			if(res!=null) return res;
		}
		
		return null;
		
	}
	
	public static Object findByName(JSONObject json,String label) throws JSONException {
		Iterator<String> keys = json.keys();
		String key = null;
		Object res = null;
		Object ret = null;
		while( (keys.hasNext()) && (key=keys.next())!=null){
			res=json.get(key);
			if(key.contentEquals(label)) {
				ret = res;
			}
			else if(res instanceof JSONArray) {ret = findByName((JSONArray)res,label);}
			else if(res instanceof JSONObject) {ret = findByName((JSONObject)res,label);}
			if(ret!=null)break;
		}
		
		return ret;
	}
	
	public static Object findByName(JSONArray json,String label) throws JSONException {
		Object ret = null;
		for(int i = 0 ; i < json.length(); i++) {
			Object res = json.get(i);
			if(res instanceof JSONObject) {
				ret = findByName((JSONObject)res,label);
			}else if(res instanceof JSONArray) {
				ret = findByName((JSONArray)res,label);
			}
			if(ret!=null) break;
		}
		
		return ret;
	}

	public static <T>  boolean contains(JSONObject json,Class<T> type) throws JSONException {
		return findType(json,type)!=null?true:false;
	}
	
	public static boolean contains(JSONObject json,String label) {
		return json.has(label);
	}
}
