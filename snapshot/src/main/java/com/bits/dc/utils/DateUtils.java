package com.bits.dc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	public static String currentTime() {
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").format(new Date());
		return timeStamp;
	}
	
	public static void main(String[] args) {
		System.out.println(currentTime());
	}
}
