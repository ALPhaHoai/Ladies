package net.anigoo.ladies.model;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.String;
import net.anigoo.ladies.lib.TimeAgo;;
public class Activity  implements Serializable {
    public Date time;//Thời gian hành động này được thực hiện
    public ActivityType type;//Loại hành động(scan, commnet, vote)
    public Product product;//Sản phẩm mà hành động này thực hiện trên
    public String content;//Nội dung của hành động (nếu là comment thì là nội dung comment)
	public int user_id;

	public void log(){
	    if(this.time != null) Log.d("ACTIVITY TIME",new SimpleDateFormat("yyyy-MM-dd HH:mm").format(this.time));
	    Log.d("ACTIVITY TYPE",this.type.getType());
	    if(this.product != null && this.product.name != null)Log.d("ACTIVITY PRODUCT",this.product.name);
	    if(this.content != null) Log.d("ACTIVITY CONTENT",this.content);
	    Log.d("ACTIVITY USER",String.valueOf(this.user_id));
    }

	public String getMessage(){
        TimeAgo timeago = new TimeAgo();
		String timeAgo = timeago.timeAgo(this.time);
		
		String message = "Bạn đã " + this.type.getType() + " " + timeAgo;
		if(this.type.isComment()){
			message += "\nNội dung: " + this.content;
		}
		return message;

    }


    public static class ActivityType{
        public static final String SCAN = "scan";
        public static final String COMMENT = "comment";
        public static final String UPVOTE = "like";
        public static final String DOWNVOTE = "dislike";
        private String TYPE;
        public ActivityType(String type){
            this.TYPE = type;
        }
        public String getType(){
            return this.TYPE;
        }
        public boolean isComment(){
            return this.TYPE.equals(COMMENT);
        }
    }
}
