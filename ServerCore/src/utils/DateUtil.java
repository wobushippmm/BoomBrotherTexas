package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static boolean isSameDay(long time1, long time2){
		return isSameDay(new Date(time1), new Date(time2));
	}
	public static boolean isSameDay(long time1, Date time2){
		return isSameDay(new Date(time1), time2);
	}
	public static boolean isSameDay(Date time1, long time2){
		return isSameDay(time2, time1);
	}
	public static boolean isSameDay(Date time1, Date time2){
		if(time1.getYear() == time2.getYear() 
				&& time1.getMonth() == time2.getMonth()
				&& time1.getDate() == time2.getDate()){
			return true;
		}
		return false;
	}
	
	/* 
     * 将时间转换为时间戳
     */ 
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static long dateToStamp(String s) throws ParseException{
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        return ts;
    }
	
	/* 
     * 将时间戳转换为时间
     */
    public static String stampToDate(long lt){
        String res;
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
}
