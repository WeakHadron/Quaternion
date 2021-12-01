package ir.iliaalizadeh.quaternion;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadActivity extends AppCompatActivity {

    FTPClient client;
    String ip;
    int port;
    ExecutorService executorService;
    String data;
    Intent intent;
    FTPFile[] files;
    FileInputStream inputStream;
    File file;
    ProgressBar progressBar;
    AppCompatTextView filename_txt;
    AppCompatTextView percent_txt;
    AppCompatTextView transferred_txt;
    AppCompatButton toggleBtn;
    ProgressDialog dialog;
    AlertDialog alertDialog;
    AlertDialog cancelDialog;
    int numOfFiles;
    int currentPercent;
    File[] sendingFiles;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        progressBar = findViewById(R.id.file_progress);
        filename_txt = findViewById(R.id.filename_txt);
        percent_txt = findViewById(R.id.percent_txt);
        toggleBtn = findViewById(R.id.toggle_btn);
        transferred_txt = findViewById(R.id.transferred_txt);
        numOfFiles = 0;
        currentPercent = 0;
        intent = getIntent();
        data = intent.getStringExtra("DATA");
        ip = data.split(":")[0];
        port = Integer.parseInt(data.split(":")[1]);
        toggleBtn.setOnClickListener((view)->{
            startProcess();
        });
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Connecting ...");
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Service fucked up. Try again.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog1, which) -> {
                    dialog1.cancel();
                    finish();
                }).create();

        cancelDialog = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Are you fucking damn sure to cancel the process?")
                .setCancelable(false)
                .setPositiveButton("Yay", (dialog1, which) -> {
                    dialog1.cancel();

                    executorService.shutdownNow();
                    executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {
                            inputStream.close();
                            client.logout();
                            client.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    finish();

                }).setNegativeButton("Nay",(dialog1,which) -> {
                    dialog1.cancel();
                }).create();


    }

    private void startProcess(){
        executorService = Executors.newSingleThreadExecutor();
        client = new FTPClient();
        client.setBufferSize(1024000);
        client.setConnectTimeout(3000);
        client.setDefaultTimeout(3000);
        executorService.execute(() -> {
            try {
                File directory = new File(Environment.getExternalStorageDirectory() + "/Quaternion/Send/");
                sendingFiles = directory.listFiles();
                client.connect(ip,port);
                client.login("FT","PASSWD");
                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.enterLocalPassiveMode();


                runOnUiThread(() -> {
                    numOfFiles = sendingFiles.length;
                    progressBar.setMax(sendingFiles.length);
                    dialog.cancel();
                    toggleBtn.setText("CANCEL");
                    filename_txt.setVisibility(View.VISIBLE);
                    toggleBtn.setOnClickListener((view) -> {
                        cancelDialog.show();
                    });
                });

                if(sendingFiles.length != 0){
                    for (File file : sendingFiles){
                        runOnUiThread(() -> filename_txt.setText(file.getName()));
                        inputStream = new FileInputStream(file);
                        client.storeFile(file.getName(),inputStream);
                        inputStream.close();
                        runOnUiThread(() -> {
                            currentPercent++;
                            progressBar.incrementProgressBy(1);
                            percent_txt.setText(currentPercent * 100 / numOfFiles + "%");
                            transferred_txt.setText(currentPercent + "/" + numOfFiles);
                        });
                    }
                }


                client.logout();
                client.disconnect();
                runOnUiThread(() -> {
                    filename_txt.setText("Complete");
                    percent_txt.setText("100%");
                    toggleBtn.setText("EXIT");
                    toggleBtn.setOnClickListener((view) -> {
                        finish();
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(alertDialog::show);

            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            inputStream.close();
            client.logout();
            client.disconnect();
            executorService.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}