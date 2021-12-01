package ir.iliaalizadeh.quaternion;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class HomeActivity extends AppCompatActivity {

    AppCompatButton btnSend;
    AppCompatButton btnReceive;
    final private static int REQUEST_CODE = 201;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        File mainDirectory = new File(Environment.getExternalStorageDirectory() + "/Quaternion");
        File sendDirectory = new File(Environment.getExternalStorageDirectory() + "/Quaternion/Send");
        File receiveDirectory = new File(Environment.getExternalStorageDirectory() + "/Quaternion/Receive");

        btnSend = findViewById(R.id.btn_send);
        btnReceive = findViewById(R.id.btn_receive);

        btnReceive.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                    HomeActivity.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){

                if(!mainDirectory.exists()){
                    mainDirectory.mkdir();
                }

                if(!receiveDirectory.exists()){
                    receiveDirectory.mkdir();
                }

                Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                intent.putExtra("REQ","SND");
                startActivity(intent);

            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
                }
            }

        });

        btnSend.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                    HomeActivity.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){

                if(!mainDirectory.exists()){
                    mainDirectory.mkdir();
                }

                if(!sendDirectory.exists()){
                    sendDirectory.mkdir();
                }

                Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                intent.putExtra("REQ","RCV");
                startActivity(intent);

            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
                }
            }

        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int result : grantResults){

            if(result == PackageManager.PERMISSION_DENIED){
                Toast.makeText(HomeActivity.this,"This piece of shit can't do a fuck without permissions, you DUMMY!",Toast.LENGTH_LONG).show();
            }
        }
    }
}