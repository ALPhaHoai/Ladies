package net.anigoo.ladies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.Result;
import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.lib.lib;
import net.anigoo.ladies.model.Product;
import net.anigoo.ladies.model.User;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;


public class Scanner extends AppCompatActivity  implements ZXingScannerView.ResultHandler {
    User user;
    String android_id;
    ProgressDialog progressDialog;
    DB db;
    String previousScreen;

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        progressDialog=new ProgressDialog(this);
        db = new DB();
        if(getIntent().hasExtra("user")){
            user = (User)getIntent().getSerializableExtra("user");
        }
        if(getIntent().hasExtra("prev")){
            previousScreen = (String)getIntent().getSerializableExtra("prev");
        }

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >=  Build.VERSION_CODES.M)
        {
            if(checkPermission())
            {
//                Toast.makeText(getApplicationContext(), "Đã set quyền!", Toast.LENGTH_LONG).show();
            }
            else
            {
                requestPermission();
            }
        }
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
//                        Toast.makeText(getApplicationContext(), "Đã set quyền truy cập camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Không thể truy cập camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("Ứng dụng cần quyền truy cập camera",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(Scanner.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Đóng", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(1000);
        final String myResult = result.getText();
        Log.d("BarCodeScanner", result.getText());
        Log.d("BarCodeScanner", result.getBarcodeFormat().toString());
        String product_id = result.getText();
        if(lib.checkLong(product_id)){
            Toast.makeText(getBaseContext(),product_id ,Toast.LENGTH_SHORT).show();
            Log.d("SCNNER PRODUCT", product_id);
            Intent intent = new Intent(Scanner.this, ViewProduct.class);
            intent.putExtra("product", product_id);
            if(user != null) {
                intent.putExtra("user", user);
                intent.putExtra("user_scan", true);
            }
            if(null != previousScreen) intent.putExtra("prev", previousScreen);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getBaseContext(),"Dữ liệu không hợp lệ",Toast.LENGTH_SHORT).show();
            finish();
        }

    }


}
