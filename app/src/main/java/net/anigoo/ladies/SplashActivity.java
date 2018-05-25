package net.anigoo.ladies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.anigoo.ladies.databinding.ActivitySplashBinding;
import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.model.User;

public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;
    String android_id;
    DB db;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        db = new DB();
        new QuickLogin().execute();
    }

    public void  login(View view)
    {
        Intent intent=new Intent(SplashActivity.this,LoginActivity.class);
        if(null != user) intent.putExtra("user",user);
        intent.putExtra("prev", "SplashActivity");
        startActivity(intent);
    }


    public void scanNow(View view)
    {
        Intent intent=new Intent(SplashActivity.this,Scanner.class);
        if(null != user) intent.putExtra("user",user);
        intent.putExtra("prev", "SplashActivity");
        startActivity(intent);
    }

    private class QuickLogin extends AsyncTask<String,String,String> {
        boolean isLogin = false;
        @Override
        protected String doInBackground(String... params) {
        if(db.select("id","users","device_id = '" + android_id + "'")){
            isLogin = true;
            String iddb = db.firstKq.get(0);
            user = new User(Integer.valueOf(iddb));
            user.getFullInfo(db);
        }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isLogin){
                Intent intent=new Intent(SplashActivity.this,ViewUserProfile.class);
                intent.putExtra("user",user);
                intent.putExtra("prev", "SplashActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                binding = DataBindingUtil.setContentView(SplashActivity.this, R.layout.activity_splash);
            }
        }
    }

}
