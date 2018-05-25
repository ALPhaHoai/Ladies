package net.anigoo.ladies.model;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import net.anigoo.ladies.RecentlyActivity;
import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.lib.DateTimeLib;
import net.anigoo.ladies.lib.lib;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class User implements Serializable{
    public String id;
    public String avatar;
    public String username;
    public String password;
    public String name;
    public String address;
    public String phone;
    public String email;
    public String birthday;

    public User(){}

    public User(int id){
        this.id = String.valueOf(id);
    }
    public User(String username){
        this.username = username;
    }

    public boolean logout(DB db){
        return db.execute("UPDATE users SET device_id = null WHERE id = '" + this.id + "'");
    }

    //Lấy toàn bộ thông tin của người dùng này
    public void getFullInfo(DB db){
        if(!lib.checkLong(this.id)) return ;
        if(db.select("avatar, username, password, name, address, phone, email, birthday", "users", "id = '" + this.id + "'")){
            this.avatar = db.firstKq.get(0);
            this.username = db.firstKq.get(1);
            this.password = db.firstKq.get(2);
            this.name = db.firstKq.get(3);
            this.address = db.firstKq.get(4);
            this.phone = db.firstKq.get(5);
            this.email = db.firstKq.get(6);
            this.birthday = db.firstKq.get(7);
        }
    }
    //Lấy toàn bộ thông tin của người dùng này từ usrname
    public void getFullInfofromUsername(DB db){
        if(this.username == null) return ;
        if(db.select("avatar, id, password, name, address, phone, email, birthday", "users", "username = '" + DB.validSql(this.username) + "'")){
            this.avatar = db.firstKq.get(0);
            this.id = db.firstKq.get(1);
            this.password = db.firstKq.get(2);
            this.name = db.firstKq.get(3);
            this.address = db.firstKq.get(4);
            this.phone = db.firstKq.get(5);
            this.email = db.firstKq.get(6);
            this.birthday = db.firstKq.get(7);
        }
    }
    public void updateDeviceId(String deviceId ,DB db){
        if(!lib.checkLong(this.id)) return ;
        db.execute("UPDATE users SET device_id = null WHERE device_id = '" + deviceId + "'");
        db.update("users",new String[]{"device_id"}, new String[]{deviceId}, "id = '" + this.id + "'");
    }

    //Sau khi user quét xong thì thêm sả phẩm vào lịch sử quét của user
    public boolean addUserHistory(String product_id, DB db){
        //Nếu người dùng đã từng quét sản phẩm này rồi thì cập nhật lại ngày tháng quét là hôm nay
        if(db.select("*","user_history", "user_id = '" + this.id + "' AND product_id = '" + product_id + "'")){
            return db.execute("UPDATE user_history SET created = NOW() WHERE user_id = '" + this.id + "' AND product_id = '" + product_id + "'");
        } else {
            return db.insert("user_history", new String[]{"product_id","user_id"}, new String[]{product_id, this.id});
        }
    }

    //Sau khi người dùng bình luận thì thêm bình luận ấy vào bảng comments
    public boolean addComment(String product_id, String comment,DB db ){
        return db.insert("comments", new String[]{"cmt","product_id","user_id"}, new String[]{comment, product_id, this.id});
    }

    public void removeUpvote(String product_id, DB db){
        db.execute("DELETE FROM user_vote WHERE vote = 'up' AND user_id = '" + this.id + "' AND product_id = '" + product_id + "'");
    }
    public void removeDownvote(String product_id, DB db){
        db.execute("DELETE FROM user_vote WHERE vote = 'down' AND user_id = '" + this.id + "' AND product_id = '" + product_id + "'");
    }

    //Người dùng upvote 1 sản phẩm
    public boolean upvote(String product_id, DB db){
        //Nếu sản phẩm đã được upvote/downvote trước đây thì update lại trạng thái vote và ngày giờ vote
        if(db.select("vote","user_vote","user_id = '" + this.id + "' AND product_id = '" + product_id + "'")){
            return db.execute("UPDATE user_vote SET created = NOW(), vote = 'up' WHERE user_id = '" + this.id + "' AND product_id = '" + product_id + "'");
        }
        //Còn không thì insert bản ghi mới vào
        else return db.insert("user_vote",
                new String[]{"user_id","product_id","vote"},
                new String[]{this.id, product_id, "up"});
    }

    //Người dùng downvote 1 sản phẩm
    public boolean downvote(String product_id, DB db){
        //Nếu sản phẩm đã được upvote/downvote trước đây thì update lại trạng thái vote và ngày giờ vote
        if(db.select("vote","user_vote","user_id = '" + this.id + "' AND product_id = '" + product_id + "'")){
            return db.execute("UPDATE user_vote SET created = NOW(), vote = 'down' WHERE user_id = '" + this.id + "' AND product_id = '" + product_id + "'");
        }
        //Còn không thì insert bản ghi mới vào
        else return db.insert("user_vote",
                new String[]{"user_id","product_id","vote"},
                new String[]{this.id, product_id, "down"});
    }
    //Kiểm tra xem người dùng có upvote sản phẩm này không
    public boolean isUpvote(String product_id, DB db){
        return db.select("id","user_vote","user_id = '" + this.id + "' AND vote = 'up' AND product_id = '" + product_id + "'");
    }
    //Kiểm tra xem người dùng có downvote sản phẩm này không
    public boolean isDownvote(String product_id, DB db){
        return db.select("id","user_vote","user_id = '" + this.id + "' AND vote = 'down' AND product_id = '" + product_id + "'");
    }

    //Trả về số sản phẩm mà user đã scan
    public int getTotalProductScan(DB db){
        try {
            if(db.select("count(*)","user_history","user_id = '" + this.id + "'")) {
                if(lib.checkLong(db.firstKq.get(0)))  return Integer.valueOf(db.firstKq.get(0));
            }
        } catch (Exception e){
            Log.e("ERROR", e.toString());
        }
        return 0;
    }
    //Trả về số sản phẩm mà user đã vote
    public int getTotalProductVote(DB db){
        try {
            if(db.select("count(*)","user_vote","user_id = '" + this.id + "'")) {
                if(lib.checkLong(db.firstKq.get(0)))  return Integer.valueOf(db.firstKq.get(0));
            }
        } catch (Exception e){
            Log.e("ERROR", e.toString());
        }
        return 0;
    }
    //Trả về số comment của user
    public int getTotalComments(DB db){
        try {
            if(db.select("count(*)","comments","user_id = '" + this.id + "'")) {
                if(lib.checkLong(db.firstKq.get(0)))  return Integer.valueOf(db.firstKq.get(0));
            }
        } catch (Exception e){
            Log.e("ERROR", e.toString());
        }
        return 0;
    }

    //Lấy danh sách các hoạt động gần đây của user
    public Activities getRecentlyActivity(DB db){
        Activities recentlyActivity = new Activities();
        Activities recentlyComment = getRecentlyComment(db);
        if(recentlyComment != null && recentlyComment.size() > 0){
            recentlyActivity.addAll(recentlyComment);
        }
        Activities recentlyVote = getRecentlyVote(db);
        if(recentlyVote != null && recentlyVote.size() > 0){
            recentlyActivity.addAll(recentlyVote);
        }
        Activities recentlyScan = getRecentlyScan(db);
        if(recentlyScan != null && recentlyScan.size() > 0){
            recentlyActivity.addAll(recentlyScan);
        }
        recentlyActivity.softByDate();
        return recentlyActivity;
    }
    //Lấy danh sách các comment gần đây của user
    public Activities getRecentlyComment(DB db){
        System.out.println("Getting Recently Uesr Comment");
        if(db.select("comments.cmt, comments.created, product.id, product.name, product.price, product.description, product.image, product.image_small, product.date_created", "comments, product", "comments.user_id = '" + this.id + "' AND product.id = comments.product_id")){
            Activities recentlyComment = new Activities();
            ArrayList<ArrayList<String>> arr = db.kq;
            for(int i = 0; i < arr.size() ; i ++){
                Activity ac = new Activity();
                ac.type = new Activity.ActivityType(Activity.ActivityType.COMMENT);

                ac.content = arr.get(i).get(0);
                ac.time = DateTimeLib.convertStringtoDate(arr.get(i).get(1), "yyyy-MM-dd HH:mm:ss");
				String product_id = arr.get(i).get(2);
                Product p = new Product(Long.valueOf(product_id));
                p.name = arr.get(i).get(3);
                if(lib.checkLong(arr.get(i).get(4))) p.price = Integer.valueOf(arr.get(i).get(4));
                p.description = arr.get(i).get(5);
                p.image = arr.get(i).get(6);
                p.image_small = arr.get(i).get(7);
                p.date_created = DateTimeLib.convertStringtoDate("yyyy-MM-dd HH:mm:ss", arr.get(i).get(8));
                ac.product = p;
				ac.user_id = Integer.valueOf(this.id);
				recentlyComment.add(ac);
            }
            System.out.println("Tổng số comment: " + recentlyComment.size());
			return recentlyComment;
        } else return null;

    }
    //Lấy danh sách các vote gần đây của user
    public Activities getRecentlyVote(DB db){
        System.out.println("Getting Recently Uesr Vote");
        if(db.select("user_vote.vote, user_vote.created, product.id, product.name, product.price, product.description, product.image, product.image_small, product.date_created", "user_vote, product", "user_vote.user_id = '" + this.id + "' AND product.id = user_vote.product_id")){
            Activities recentlyVote = new Activities();
            ArrayList<ArrayList<String>> arr = db.kq;
            for(int i = 0; i < arr.size() ; i ++){
                Activity ac = new Activity();
                String vote = arr.get(i).get(0);
                if(vote.equals("up")) ac.type = new Activity.ActivityType(Activity.ActivityType.UPVOTE);
                else ac.type = new Activity.ActivityType(Activity.ActivityType.DOWNVOTE);

                ac.time = DateTimeLib.convertStringtoDate(arr.get(i).get(1), "yyyy-MM-dd HH:mm:ss");
				String product_id = arr.get(i).get(2);
                Product p = new Product(Long.valueOf(product_id));
                p.name = arr.get(i).get(3);
                if(lib.checkLong(arr.get(i).get(4))) p.price = Integer.valueOf(arr.get(i).get(4));
                p.description = arr.get(i).get(5);
                p.image = arr.get(i).get(6);
                p.image_small = arr.get(i).get(7);
                p.date_created = DateTimeLib.convertStringtoDate("yyyy-MM-dd HH:mm:ss", arr.get(i).get(8));
                ac.product = p;
				ac.user_id = Integer.valueOf(this.id);
                recentlyVote.add(ac);
            }
            System.out.println("Tổng số vote: " + recentlyVote.size());
			return recentlyVote;
        } else return null;

    }
    //Lấy danh sách các scan gần đây của user
    public Activities getRecentlyScan(DB db){
        System.out.println("Getting Recently Uesr Scan");
        if(db.select("user_history.created, product.id, product.name, product.price, product.description, product.image, product.image_small, product.date_created", "user_history, product", "user_history.user_id = '" + this.id + "' AND product.id = user_history.product_id")){
            Activities recentlyScan = new Activities();
            ArrayList<ArrayList<String>> arr = db.kq;
            for(int i = 0; i < arr.size() ; i ++){
                Activity ac = new Activity();
                ac.type = new Activity.ActivityType(Activity.ActivityType.SCAN);

                ac.time = DateTimeLib.convertStringtoDate( arr.get(i).get(0), "yyyy-MM-dd HH:mm:ss");

				String product_id = arr.get(i).get(1);
                Product p = new Product(Long.valueOf(product_id));
                p.name = arr.get(i).get(2);
                if(lib.checkLong(arr.get(i).get(3))) p.price = Integer.valueOf(arr.get(i).get(3));
                p.description = arr.get(i).get(4);
                p.image = arr.get(i).get(5);
                p.image_small = arr.get(i).get(6);
                p.date_created = DateTimeLib.convertStringtoDate("yyyy-MM-dd HH:mm:ss", arr.get(i).get(7));

                ac.product = p;
				ac.user_id = Integer.valueOf(this.id);
                recentlyScan.add(ac);
            }
            System.out.println("Tổng số scan: " + recentlyScan.size());
			return recentlyScan;
        } else return null;

    }


}
