package com.parabay.cinema.youtube;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.text.Html;
import android.util.Log;

/**
 * Represents a video stream
 *
 */
public class VideoStream {
	
	protected String mUrl;
	
	/**
	 * Construct a video stream from one of the strings obtained 
	 * 	from the "url_encoded_fmt_stream_map" parameter if the video_info 
	 * @param pStreamStr - one of the strings from "url_encoded_fmt_stream_map"
	 */
	public VideoStream(String pStreamStr){
		
		Log.i("DEBUG", "Input:" + mUrl);
		
		String[] lArgs=pStreamStr.split("&");
		Map<String,String> lArgMap = new HashMap<String, String>();
		for(int i=0; i<lArgs.length; i++){
			String[] lArgValStrArr = lArgs[i].split("=");
			if(lArgValStrArr != null){
				if(lArgValStrArr.length >= 2){
					lArgMap.put(lArgValStrArr[0], lArgValStrArr[1]);
				}
			}
		}

		mUrl = URLDecoder.decode(lArgMap.get("url"));
		mUrl += "&signature=";
		mUrl += lArgMap.get("sig");
		
		Log.i("DEBUG", mUrl);
	}
	public String getUrl(){
		
		return mUrl;
	}
}