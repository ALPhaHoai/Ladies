package net.anigoo.ladies.lib;


import java.util.regex.Pattern;

/**
 *
 * @author Long
 */
public class lib {
    //ucwords() - Uppercase the first character of each word in a string
    //Tương tự hàm ucwords() trong php
    public static String  titleCase(String str) {
        String kqStr = "";
        String[] splitStr = str.toLowerCase().split(" ");
        for (int i = 0; i < splitStr.length; i++) {
            // You do not need to check if i is larger than splitStr length, as your for does that for you
            // Assign it back to the array
            kqStr += splitStr[i].substring(0,1).toUpperCase() + splitStr[i].substring(1) + " ";
        }
        // Directly return the joined string
        return kqStr;
    }

    public static String splitTitle(String title, int length){
        if(title.length() > length) {
            String[] names = title.split(" ");
            title = "";
            for(int i = 0; i < names.length ; i++){
                if((title + names[i] ).length() > length) break;
                title += names[i] + " ";
            }
            title = title.trim();
            title = Pattern.compile("\\W+$").matcher(title).replaceAll("");
        }
        return title.trim();
    }

    public static boolean checkDouble(String textField){
        try {
            double in = Double.parseDouble(textField);
            return true; //Đây là 1 số
        }
        catch (Exception e) {
            return false; // Đây không phải là số
        }
    }
    public static boolean checkLong(String textField){
        try {
            long in = Long.parseLong(textField);
            return true; //Đây là 1 số
        }
        catch (NumberFormatException e) {
            return false; // Đây không phải là số
        }
    }


}
