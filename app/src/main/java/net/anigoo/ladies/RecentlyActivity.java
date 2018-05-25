package net.anigoo.ladies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.model.Activities;
import net.anigoo.ladies.model.Activity;
import net.anigoo.ladies.model.User;

public class RecentlyActivity extends AppCompatActivity {
    ListView mListView;
    MyActivityAdapter myActivityAdapter;
    int MAX_ACTIVITY;

    ProgressDialog progressDialog;

    String android_id;
    User user;
    Activities activities;
    DB db;

    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently);
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        MAX_ACTIVITY = 50;
        progressDialog=new ProgressDialog(this);
        db = new DB();
        if(getIntent().hasExtra("user")){
            user = (User)getIntent().getSerializableExtra("user");
        }

        mListView = (ListView)findViewById(R.id.list_recently_activity);

        new LoadRecentlyActivity().execute();

    }
    private class LoadRecentlyActivity extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {

            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            activities = user.getRecentlyActivity(db);
            Log.d("Tổng số hđộng gần đây", "" + activities.size());
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            if(activities == null || activities.size() == 0){
                Toast.makeText(getBaseContext(),"Không có hoạt động gần đây nào",Toast.LENGTH_LONG).show();
                finish();
            }
            myActivityAdapter = new MyActivityAdapter(RecentlyActivity.this, activities);
            mListView.setAdapter(myActivityAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(RecentlyActivity.this, ViewProduct.class);
                    intent.putExtra("product", String.valueOf(activities.get(position).product.id));
                    intent.putExtra("user", user);
                    intent.putExtra("prev", "RecentlyActivity");
                    startActivity(intent);

                }
            });
            progressDialog.hide();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Left to Right swipe action
                    if (x2 > x1)
                    {
                                if(null != user) {
                                    Intent intent=new Intent(RecentlyActivity.this,ViewUserProfile.class);
                                    intent.putExtra("user",user);
                                    startActivity(intent);
                                }
                    }

                }

                break;
        }
        return super.onTouchEvent(event);
    }

}
