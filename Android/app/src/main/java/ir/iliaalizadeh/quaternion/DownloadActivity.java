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
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadActivity extends AppCompatActivity {
    FTPClient client;
    String ip;
    int port;
    ExecutorService executorService;
    String data;
    Intent intent;
    FTPFile[] files;
    FileOutputStream outputStream;
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
                                outputStream.close();
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
                client.connect(ip,port);
                client.login("FT","PASSWD");
                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.enterLocalPassiveMode();
                files = client.listFiles();

                runOnUiThread(() -> {
                    numOfFiles = files.length;
                    progressBar.setMax(files.length);
                    dialog.cancel();
                    toggleBtn.setText("CANCEL");
                    filename_txt.setVisibility(View.VISIBLE);
                    toggleBtn.setOnClickListener((view) -> {
                        cancelDialog.show();
                    });
                });
                if(files.length != 0){
                    for(int i = 0 ; i < files.length ; i++){
                        final int p = i;
                        runOnUiThread(() -> filename_txt.setText(files[p].getName()));
                        file = new File(Environment.getExternalStorageDirectory() + "/Quaternion/Receive/" + files[i].getName());
                        file.createNewFile();
                        outputStream = new FileOutputStream(file);
                        client.retrieveFile(file.getName(),outputStream);
                        outputStream.close();
                        runOnUiThread(() -> {
                            currentPercent++;
                            progressBar.incrementProgressBy(1);
                            percent_txt.setText(currentPercent * 100 / numOfFiles + "%");
                            transferred_txt.setText(String.valueOf(currentPercent + "/" + numOfFiles));
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

                runOnUiThread(() -> {
                    alertDialog.show();
                });

            }

        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            outputStream.close();
            client.logout();
            client.disconnect();
            executorService.shutdown();

        } catch (Exception e) {
            e.printStackTrace();



        }
    }
}