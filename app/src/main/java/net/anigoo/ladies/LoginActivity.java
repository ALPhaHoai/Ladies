package net.anigoo.ladies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.anigoo.ladies.databinding.ActivityLoginBinding;
import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.model.User;

import com.mikepenz.iconics.context.IconicsLayoutInflater2;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;

    private String android_id;
    User user;
    Button login;
    EditText username,password;
    ProgressDialog progressDialog;
    DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        login = (Button)findViewById(R.id.btn_login);
        username = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);

        progressDialog=new ProgressDialog(this);
        db = new DB();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DoLogin().execute();
            }
        });

    }

    public void signup(View view) {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }
    private class DoLogin extends AsyncTask<String,String,String> {
        String usernamestr=username.getText().toString().trim();
        String passwordstr=password.getText().toString().trim();
        String z="";
        boolean isSuccess=false;

        @Override
        protected void onPreExecute() {

            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if(usernamestr.equals("")|| passwordstr.equals(""))
            {
                z = "Hãy nhập đầy đủ thông tin";
//                z += "\n" + usernamestr + "\n" + passwordstr;//debug purpose
            }
            else
            {
                if(db.select("id, password","users","username = '" + DB.validSql(usernamestr) + "'")){
                    String iddb = db.firstKq.get(0);
                    String passworddb = db.firstKq.get(1);
                    if(passwordstr.equals(passworddb)){
                        isSuccess = true;
                        user = new User(Integer.valueOf(iddb));
                        user.getFullInfo(db);
                        user.updateDeviceId(android_id, db);
                    } else {
                        z = "Thông tin bạn nhập không chính xác";
                    }
                } else {
                    z = "Thông tin bạn nhập không chính xác";
                }

            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isSuccess) {
//                Toast.makeText(getBaseContext(),"Đăng nhập thành công",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(LoginActivity.this,ViewUserProfile.class);
                intent.putExtra("user",user);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();
            }

            progressDialog.hide();

        }
    }
}
