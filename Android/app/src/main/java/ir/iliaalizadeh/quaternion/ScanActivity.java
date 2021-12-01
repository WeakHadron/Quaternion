package ir.iliaalizadeh.quaternion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.ArrayList;
import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    ZXingScannerView scannerView;
    String request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        initScanner();
        setContentView(scannerView);
        request = getIntent().getStringExtra("REQ");
    }

    private void initScanner(){
        scannerView.setResultHandler(this);
        scannerView.setAspectTolerance(0.5f);
        scannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
        scannerView.startCamera();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initScanner();
    }


    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }


    @Override
    public void handleResult(Result rawResult) {
        String[] data = rawResult.getText().split("#");
        Intent intent;

        if(data[0].equals("QUAT") && data[1].equals(request) ){
            if(request.equals("RCV")){
                intent = new Intent(ScanActivity.this,UploadActivity.class);
            }else{
                intent = new Intent(ScanActivity.this,DownloadActivity.class);
            }

            intent.putExtra("DATA",data[2]);
            finishAfterTransition();
            startActivity(intent);
        }

        else{
            Toast.makeText(ScanActivity.this,"Invalid barcode, DICKHEAD",Toast.LENGTH_SHORT).show();
            initScanner();
        }


    }
}