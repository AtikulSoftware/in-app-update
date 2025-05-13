package com.devatikul.inappupdate;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

   private static final String APK_FILE_URL = "https://cheapprimo.com/app/NewFolder%201/debug/app-debug.apk";

   private LinearProgressIndicator progressBar;
   private TextView percentageText;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_main);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      // initialize
      progressBar = findViewById(R.id.progressBar);
      percentageText = findViewById(R.id.percentageText);

   } // onCreate bundle end here =============

   @Override
   protected void onResume() {
      super.onResume();
      checkForAppUpdate();
   }

   private void checkForAppUpdate() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Update Available")
              .setMessage("A new version of the app is available. Would you like to update?")
              .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    new AppUpdater().setUp(MainActivity.this)
                            .setAppUrl(APK_FILE_URL)
                            .setFileNameFromUrl()
                            .setOnDownloadListener(new onDownloadListener() {
                               @Override
                               public void onBuffer(String connectingMsg) {

                               }

                               @Override
                               public void onProgress(int progress) {
                                  progressBar.setVisibility(View.VISIBLE);
                                  percentageText.setVisibility(View.VISIBLE);
                                  progressBar.setProgress(progress);
                                  percentageText.setText(progress + "%");
                               }

                               @Override
                               public void onComplete(File path) {
                                  progressBar.setVisibility(View.GONE);
                                  percentageText.setVisibility(View.GONE);
                               }

                               @Override
                               public void onError(String errorMsg) {

                               }
                            }).start();
                 }
              })
              .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                 }
              })
              .setCancelable(false)
              .show();
   }


}
