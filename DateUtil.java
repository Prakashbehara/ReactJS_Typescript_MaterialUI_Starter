package com.haddop.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static Date getDate(String text , String pattern) throws ParseException {
		DateFormat format = new SimpleDateFormat(pattern);
		return format.parse(text);
		
	}
}
