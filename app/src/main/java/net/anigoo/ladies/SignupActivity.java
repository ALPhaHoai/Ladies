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

import net.anigoo.ladies.databinding.ActivitySignupBinding;
import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.model.User;

import com.mikepenz.iconics.context.IconicsLayoutInflater2;

public class SignupActivity extends AppCompatActivity {
    ActivitySignupBinding binding;

    String android_id ;
    User user;
    Button signup;
    Button back_to_login;
    EditText username,password,email;
    ProgressDialog progressDialog;
    DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        signup = (Button)findViewById(R.id.btn_signup);
        username = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);
        email = (EditText) findViewById(R.id.et_email_address);

        progressDialog=new ProgressDialog(this);
        db = new DB();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DoSignup().execute();
            }
        });

    }


    public void login(View view) {
        startActivity(new Intent(this, LoginActivity.class));
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

    private class DoSignup extends AsyncTask<String,String,String> {
        String usernamestr=username.getText().toString().trim();
        String passwordstr=password.getText().toString().trim();
        String emailstr=email.getText().toString().trim();
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
            if(usernamestr.equals("")|| passwordstr.equals("")|| emailstr.equals("")){
                z = "Hãy nhập đầy đủ thông tin";
            }
            else if (!isValidEmailAddress(emailstr)){
                z = "Email không hợp lệ";
            }  else {
                if(db.select("username","users","username = '" + DB.validSql(usernamestr) + "'")){
                    z = "Username đã có người sử dụng. Vui lòng nhập username khác";
                } else if(db.select("email","users","email = '" + DB.validSql(emailstr) + "'")){
                    z = "Email đã có người sử dụng. Vui lòng nhập email khác";
                } else {
                    if(db.insert("users", new String[]{"username","password","email","device_id"}, new String[]{usernamestr, passwordstr, emailstr, android_id})){
                        isSuccess = true;
                        user = new User(usernamestr);
                        user.getFullInfofromUsername(db);
                    } else {
                        z = "Không thể đăng ký thông tin tài khoản được";
                    }
                }
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isSuccess) {
                Toast.makeText(getBaseContext(),"Đăng ký thành công",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(SignupActivity.this,ViewUserProfile.class);
                intent.putExtra("user",user);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();
            }

            progressDialog.hide();

        }
    }
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
