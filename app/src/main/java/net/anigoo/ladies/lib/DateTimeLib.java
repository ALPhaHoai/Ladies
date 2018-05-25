package net.anigoo.ladies.lib;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * Các hàm thư viện hỗ trợ cho các thao tác với date time
 * @author Long
 */
public class DateTimeLib {
    public static void main(String[] args) throws ParseException {
        String time = "2018-05-12 14:23:14";
        String format = "yyyy-MM-dd HH:mm:ss";
        System.out.println(DateTimeLib.convertStringtoDate(time, format));
    }
    
    /*
    * Kiểm tra xem xâu date có phải là ngày hôm nay không
    * Chỉ so sánh ngày không so sánh giờ
    */
    public static boolean isToday(Date date) {
        try {
            String today = getToday("yyyy-MM-dd");
            String thisDate = convertDatetoString(date, "yyyy-MM-dd");
            return (today.equals(thisDate));
        } catch (Exception e) {
//            System.out.println(e);
            return false;
        }
    }
    
    /*
    * Trả về giá trị ngày thán năm hiện tại với format
    */
    public static String getToday(String format) {
//        return convertDatetoString(new Date(),"yyyy-M-d"); //Loại bỏ các số 0 ở đầu này và tháng (nếu có)
        return convertDatetoString(new Date(),format);
    }
    /*
    * Trả về giá trị ngày thán năm hiện tại theo chuẩn sql yyyy-MM-dd
    */
    public static String getToday() {
//        return convertDatetoString(new Date(),"yyyy-M-d"); //Loại bỏ các số 0 ở đầu này và tháng (nếu có)
        return convertDatetoString(new Date(),"yyyy-MM-dd");
    }
    
    /*
    * Chuyển đỗi String sang Date với SimpleDateFormat
    */
    public static Date convertStringtoDate(String datetime, String format){
        try {
            return new SimpleDateFormat(format).parse(datetime);
        } catch (Exception e) {
//            System.out.println(e);
            return null;
        }
    }
    /*
    * Chuyển đỗi Date sang String với SimpleDateFormat
    */
    public static String convertDatetoString(Date datetime, String format){
        try {
            return new SimpleDateFormat(format).format(datetime);
        } catch (Exception e) {
//            System.out.println(e);
            return null;
        }
    }
}
