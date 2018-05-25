package net.anigoo.ladies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import junit.framework.Test;

import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.model.User;

public class ViewUserProfile extends AppCompatActivity {
    String android_id ;
    User user;
    DB db;
    ProgressDialog progressDialog;

    ImageView avatar;
    TextView total_product_scaner, total_product_vote, total_comments;
    TextView email, phone, user_full_name;

    //variable for storing the time of first click
    long startTime;
    //constant for defining the time duration between the click that can be considered as double-tap
    static final int MAX_DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        db = new DB();
        progressDialog=new ProgressDialog(this);

        avatar = (ImageView)findViewById(R.id.user_avatar) ;

        total_product_scaner = (TextView)findViewById(R.id.total_product_scaner);
        total_product_vote = (TextView)findViewById(R.id.total_product_vote);
        total_comments = (TextView)findViewById(R.id.total_comments);

        email = (TextView)findViewById(R.id.email);
        phone = (TextView)findViewById(R.id.phone);
        user_full_name = (TextView)findViewById(R.id.user_full_name);

        if(getIntent().hasExtra("user")){
            user = (User)getIntent().getSerializableExtra("user");
        if(user.name != null){
            user_full_name.setText(user.name);
        } else {
            user_full_name.setVisibility(View.GONE);
        }

        if(user.email != null){
                email.setText(user.email);
            } else {
                findViewById(R.id.email_block).setVisibility(View.GONE);
        }


        if(user.phone != null){
            phone.setText(user.phone);
        } else {
            findViewById(R.id.phone_block).setVisibility(View.GONE);
        }

        //Load avater user
        if(user.avatar != null && user.avatar.length() > 4) {
            Picasso.with(ViewUserProfile.this).load(user.avatar).into(avatar);
        }
        new LoadUserInfo().execute();
        }
    }

    public void isLogout(View v){
        if(null == user) return ;
        if(startTime > 0){
            if(System.currentTimeMillis() - startTime < MAX_DURATION)
                new DoLogout().execute();
            else startTime = System.currentTimeMillis();
        } else startTime = System.currentTimeMillis();
    }
    private class DoLogout extends AsyncTask<String,String,String> {
        boolean isSuccess = false;
        @Override
        protected String doInBackground(String... params) {
        if(null != user){
            isSuccess = user.logout(db);
        }
        return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isSuccess) {
                Intent intent=new Intent(ViewUserProfile.this,SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        }
    }



    //When click scan button
    public void scan(View view) {
        Intent intent=new Intent(ViewUserProfile.this,Scanner.class);
        intent.putExtra("user",user);
        intent.putExtra("prev","ViewUserProfile");
        startActivity(intent);
    }

    //When click viewRecentlyActivity button
    public void viewRecentlyActivity(View view) {
        Intent intent=new Intent(ViewUserProfile.this,RecentlyActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("prev","ViewUserProfile");
        startActivity(intent);
    }


    private class LoadUserInfo extends AsyncTask<String,String,String> {
        int totalProductScan, totalProductVote, totalComments;
        @Override
        protected String doInBackground(String... params) {
           if(user == null || db == null) return null;
           totalProductScan = user.getTotalProductScan(db);
           totalProductVote = user.getTotalProductVote(db);
           totalComments = user.getTotalComments(db);
           return null;
        }
        @Override
        protected void onPostExecute(String s) {
            total_product_scaner.setText(String.valueOf(totalProductScan));
            total_product_vote.setText(String.valueOf(totalProductVote));
            total_comments.setText(String.valueOf(totalComments));

        }
    }

}
