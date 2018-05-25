package net.anigoo.ladies.lib;

import java.util.ArrayList;
import java.util.regex.Pattern;
import android.os.StrictMode;
import android.util.Base64;

import net.anigoo.ladies.model.Activities;
import net.anigoo.ladies.model.Activity;
import net.anigoo.ladies.model.Product;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.jsoup.Jsoup;

public class DB {

    private boolean PRINT = true;//in ra man hinh câu lệnh được thực hiện
    public int count = 0; //Số lượng các bản ghi lấy được
    public ArrayList<ArrayList<String>> kq = null; //Danh sách trả về các bản ghi
    public ArrayList<String> firstKq = null; //Bản ghi đầu tiên của kq trả về

    public DB() {
    }
    public static void main(String[] args){
        DB db = new DB();
        if(db.select("cmt, created, product_id", "comments", "user_id = '3'")){
            for(int i = 0; i < db.kq.size() ; i ++){
                System.out.println(db.kq.get(0));
            }
        }
    }

   
    /**
     * Thực thi 1 câu truy vẫn có trả về dữ liệu
     *
     * @param query : câu truy vấn sql
     * @return the boolean: thành công/thất bại
     */
    public boolean execute(String query) {
        //Nếu câu truy vấn không trả về dữ liệu thì nên sử dụng state.execute(sql);, bằng không sẽ báo lỗi
                String response = vps_excute("excute",query);
                return Boolean.valueOf(response);
    }


    /**
     * Truy vấn select (phần chính)
     *
     * @param column cột cần select , có thể select *, distinct(id) , ...
     * @param table bảng đang được select
     * @param condition điều kiện select
     * @return the boolean : thành công/thất bại
     */
    public boolean select(String column, String table, String condition)  {
        String sql = "SELECT " + column + " FROM " + table + " WHERE " + condition + " ;"; //Câu lệnh sql
            String returnStr = vps_excute("select",sql);
            if("false".equalsIgnoreCase(returnStr)) return false;
            ArrayList<ArrayList<String>> retu = convertJsontoArrayListArrayListString(returnStr);
            if (retu != null && retu.size() > 0) {
                kq = new ArrayList<ArrayList<String>>(retu);
                firstKq = kq.get(0);
                return true;
            }
            else return false;
    }

    /*
    Convert String[] to ArrayList<String>
    String[] a = new String[]{"No thing"};
    ArrayList<String> b = new ArrayList<String>(Arrays.asList(a))
    
    Convert ArrayList<String> to String[]
    ArrayList<String> a = new ArrayList<String>();
    String[] b =  a.toArray(new String[a.size()])
     */
    /**
     * Hàm insert con
     */
    public boolean insert(String table, ArrayList<String> colName, ArrayList<String> colValue)  { //Truy vấn insert
        return insert(table, colName.toArray(new String[colName.size()]), colValue.toArray(new String[colValue.size()]));
    }

    /**
     * Hàm insert con
     */
    public boolean insert(String table, ArrayList<String> colName, String[] colValue)  { //Truy vấn insert
        return insert(table, colName.toArray(new String[colName.size()]), colValue);
    }

    /**
     * Hàm insert con
     */
    public boolean insert(String table, String[] colName, ArrayList<String> colValue)  { //Truy vấn insert
        return insert(table, colName, colValue.toArray(new String[colValue.size()]));
    }

    /**
     * Thay thế 1 số ký tự đặc biệt có trong giá trị value
     *
     * @param value : dữ liệu thô, lấy từ web hoặc do người dùng nhập vào
     */
    public static String validSql(String value) {
        if (value == null) {
            return null;
        }
        //Các câu lệnh sau phải đúng thứ tự
        value = value.replace("\\", "\\\\");
        value = value.replace("\"", "\\\"");
        value = value.replace("\'", "\\\'");
        return value;
    }

    /**
     * Hàm chính Insert bản ghi vào database
     *
     * @param table : bảng muốn insert
     * @param colName : danh sách các tên cột của các giá trị insert
     * @param colValue : danh sách các giá trị insert
     * @return true/false: thành công/thất bại
     * @
     */
    public boolean insert(String table, String[] colName, String[] colValue)  { //Truy vấn insert
        int totalColumn = colValue.length;
        if (colName.length != totalColumn) {
            System.err.println("Số lượng các cột không trùng nhau");
            return false;
        }

        String column = "";//Tên các cột
        String strColValue = "";//Giá trị các cột
        for (int i = 0; i < totalColumn; i++) {
            if (colName[i] != null && colValue[i] != null) {
                strColValue += "\"" + validSql(colValue[i]) + "\"";
                column += colName[i].trim();
                if (i < totalColumn - 1) {
                    column += ", ";
                    strColValue += ", ";
                }
            }
        }
        column = column.trim();
        strColValue = strColValue.trim();
        while (column.substring(column.length() - 1).equals(",")) {
            column = column.substring(0, column.length() - 1).trim();
        }
        while (strColValue.substring(strColValue.length() - 1).equals(",")) {
            strColValue = strColValue.substring(0, strColValue.length() - 1).trim();
        }
        while (column.substring(0, 1).equals(",")) {
            column = column.substring(1).trim();
        }
        while (strColValue.substring(0, 1).equals(",")) {
            strColValue = strColValue.substring(1).trim();
        }
        String sql = "INSERT INTO " + table + "(" + column + ")" + " VALUES " + "(" + strColValue + ");";

        return this.execute(sql);//Thực thi câu lệnh insert
    }

    public boolean update(String table, ArrayList<String> col, ArrayList<String> colValue, String condition)  { //Truy vấn update, các giá trị null thì không update
        return update(table, col.toArray(new String[col.size()]), colValue.toArray(new String[colValue.size()]), condition);
    }

    public boolean update(String table, String[] colName, ArrayList<String> colValue, String condition)  { //Truy vấn update, các giá trị null thì không update
        return update(table, colName, colValue.toArray(new String[colValue.size()]), condition);
    }

    public boolean update(String table, ArrayList<String> colName, String[] colValue, String condition)  { //Truy vấn update, các giá trị null thì không update
        return update(table, colName.toArray(new String[colName.size()]), colValue, condition);
    }

    /**
     * Hàm chính Truy vấn update, các giá trị null thì không update
     *
     * @param table : tên bảng
     * @param colName : danh sách tên các cột
     * @param colValue : danh sách các giá trị
     * @param condition : điều kiện update
     * @return the boolean
     */
    public boolean update(String table, String[] colName, String[] colValue, String condition)  {
        int totalColumn = colValue.length;
        if (colName.length != totalColumn) {
            System.err.println("Số lượng các cột không trùng nhau");
            return false;
        }

        String set = "";
        for (int i = 0; i < totalColumn; i++) {
            String tempColName = colName[i];
            String tempColValue = colValue[i];

            if (tempColValue != null && tempColName != null) {
                set += tempColName + " = \"" + validSql(tempColValue) + "\"";
                if (i < totalColumn - 1) {
                    set += ", ";
                }
            }
        }
        //Loại bỏ các dấu , thừa trong set
        set = set.trim();
        while (set.substring(set.length() - 1).equals(",")) {
            set = set.substring(0, set.length() - 1).trim();
        }
        while (set.substring(0, 1).equals(",")) {
            set = set.substring(1).trim();
        }

        if (condition == null || condition.trim().equals("")) {
            condition = "1";
        }
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + condition;

        return this.execute(sql);//Thực thi câu lệnh update
    }

    /*
    * insert or update records
    * Nếu bản ghi đã tồn tại trong csdl thì {
    * - nếu bản ghi có sự khác biệt với dữ liệu insert vào thì update lại
    * - nếu bản ghi không có sự khác biệt thì không làm gì cả
    }
    * Còn bản ghi chưa có thì insert
    * inodate = insert + or + update
     */
    public boolean inodate(String table, String[] colName, String[] colValue, String condition)  {
        int totalColumn = colValue.length;
        if (colName.length != totalColumn) {
            System.err.println("Số lượng các cột không trùng nhau");
            return false;
        }
        String column = "";
        for (int i = 0; i < totalColumn; i++) {
            column += colName[i];
            if (i < totalColumn - 1) {
                column += ", ";
            }
        }
        if (this.select(column, table, condition)) {
            //Bản ghi đã có trong csdl
            ArrayList<String> a = this.kq.get(0);
//            System.out.println(a);
            boolean update = false;//Biến quyết định thực thi lệnh update
            ArrayList<String> colNamesArrayList = new ArrayList<String>();
            ArrayList<String> colValuesArrayList = new ArrayList<String>();
            for (int i = 0; i < a.size(); i++) {
                String colDBName = colName[i];//Tên của cột chứa giá trị này trong DB
//                System.out.println(a.get(i));
                String valueDB = a.get(i);//Giá trị trong db
                String valueOut = colValue[i];//Giá trị bên ngoài đang muốn thêm vào
                if (valueDB == null) {
                    if (valueOut != null) {
                        update = true;
                        colNamesArrayList.add(colDBName);//Tên của cột có giá trị này
                        colValuesArrayList.add(valueOut);//Giá trị của giá trị này
                    }
                } else if (!valueDB.equals(valueOut)) {//Nếu hai giá trị trong - ngoài khác nhau (so sánh theo kiểu string)
                    if (lib.checkDouble(valueDB) && lib.checkDouble(valueOut)) {//Nếu cả hai giá trị trong - ngoài đều là số thì so sánh kiểu số
                        Double d_valueDB = Double.valueOf(valueDB);
                        Double d_valueOut = Double.valueOf(valueOut);
                        if (!d_valueDB.equals(d_valueOut)) {//Nếu hai giá trị này khác nhau
                            update = true;
//                            System.out.println(colDBName);
//                            System.out.println(valueDB);
//                            System.out.println(valueOut);
                            colNamesArrayList.add(colDBName);//Tên của cột có giá trị này
                            colValuesArrayList.add(valueOut);//Giá trị của giá trị này
                        }
                    } else {
                        update = true;
//                            System.out.println(colDBName);
//                            System.out.println(valueDB);
//                            System.out.println(valueOut);
                        colNamesArrayList.add(colDBName);
                        colValuesArrayList.add(colValue[i].trim());
                    }
                }
            }
            //Nếu bản ghi có sự khác biệt với dữ liệu update thì mới update
            //Còn bản ghi giống hệt thì không làm gì cả
            if (update) {
//                System.out.println("dang update");
                return this.update(table, colNamesArrayList, colValuesArrayList, condition);
            }
        } else {
//            System.out.println("dang insert");
            return this.insert(table, colName, colValue);
        }
        return true;

    }


    /**
     * in kết quả các bản ghi trong mảng kq
     */
    public void print() {
        int totalRecords = 0;
        if (kq != null) {
            totalRecords = kq.size();
            for (int i = 0; i < totalRecords; i++) {
                int percent = (100 * (i + 1)) / totalRecords;
                if (percent < 10) {
                    System.out.println(String.valueOf(percent) + "% (" + String.valueOf(i + 1) + "/" + totalRecords + ") - " + kq.get(i));
                } else {
                    System.out.println(String.valueOf(percent) + "%(" + String.valueOf(i + 1) + "/" + totalRecords + ") - " + kq.get(i));
                }
            }
        } else {
            System.out.println("Không có dữ liệu để in");
        }
    }


    /**
     * Chuyển đỗi dữ liệu dạng json sang dữ liệu có cấu trúc để xử
     * lý
     *
     * @param jsonString the value of jsonString
     */
    public static ArrayList<ArrayList<String>> convertJsontoArrayListArrayListString(String jsonString) {
        if (jsonString == null
                || jsonString.equals("[]")
                || !jsonString.contains("\"")
                || !jsonString.contains("{")
                || !jsonString.contains("}")
                || !jsonString.contains(":")
                || jsonString.length() < 4) {
            return null;
        }
        try {
            JSONArray jo = (JSONArray) JSONValue.parse(jsonString);;//Sau khi convert sang Json thì nó đã bị mất đi thứ tự của các thuộc tính
            if (jo == null || jo.size() < 1) {
                return null;
            }
            ArrayList<ArrayList<String>> returnValue = new ArrayList<ArrayList<String>>();

            //Tìm lại thứ tự của các thuộc tính
            String firstObject = jsonString;
            if (firstObject.contains("},{")) {
                firstObject = firstObject.substring(0, firstObject.indexOf("},{"));
            }
//        String[] colums = firstObject.split("\",\""); Đây là cách cũ, sẽ bị lỗi với những giá trị null, vì sẽ không có dấu "" bao quanh giá trị đó
            String[] colums = firstObject.split(",\"");
            for (int i = 0; i < colums.length; i++) {
                colums[i] = colums[i].replace("[{\"", "");//Đúng thứ tự
                colums[i] = colums[i].replace("{\"", "");//Đúng thứ tự
                if (colums[i].contains("\"")) {
                    colums[i] = colums[i].substring(0, colums[i].indexOf("\""));
                }
//                System.out.println(colums[i]);
            }
            //Thứ tự các thuộc tính được lưu trong mảng colums

            //Lấy dữ liệu
            for (Object object : jo) {
                JSONObject jsonLineItem = (JSONObject) object;
                ArrayList<String> allValues = new ArrayList<String>();
                for (String colum : colums) {
                    Object value = jsonLineItem.get(colum);//vì null không thể cast sang tring được
                    if (value != null) {
                        allValues.add(value.toString());//Nếu khác null thì chuyển đỗi sang string
                    } else {
                        allValues.add(null);//Nếu == null thi thêm null
                    }
                }
                returnValue.add(allValues);
            }

            return returnValue;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return null;
    }

    /**
     * OUT OF DATE Thực thi 1 câu lệnh sql trên server và nhận về dữ liệu
     *
     * @param sql the value of sql
     */
    public String vps_excute(String cmd,String sql) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                .permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        if (this.PRINT) {
            System.out.println(sql);
        }
        String url = "http://108.61.223.107/ladies_sql.php";
        org.jsoup.Connection connection = Jsoup.connect(url);//Kết nối url 
        connection.ignoreContentType(true);
        connection.method(org.jsoup.Connection.Method.POST);
        connection.data("key", "c2VsZWN0ICogZnJvbSBhbmlnb28ubmV3cyBvcmRlciBieSByYW5kKCkgbGl");
       try {
           String encodedSql = Base64.encodeToString(sql.getBytes("UTF-8"), Base64.DEFAULT);
           connection.data("sql", encodedSql);
       } catch (Exception e){
           System.err.println(e);
       }
        connection.data("cmd", cmd);

        org.jsoup.Connection.Response response;
        try {
            response = connection.execute();
            String returnStatus = response.body();
            if (this.PRINT) {
                System.out.println(returnStatus);
            }
            return returnStatus;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return null;
    }

}
