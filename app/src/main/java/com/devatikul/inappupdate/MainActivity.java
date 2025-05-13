package com.devatikul.inappupdate;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;

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
              .setPositiveButton("Update", (dialog, which) -> new AppUpdater(MainActivity.this)
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
                            Toast.makeText(MainActivity.this, "Error: "+errorMsg, Toast.LENGTH_SHORT).show();
                            checkForAppUpdate();
                         }
                      }).start())
              .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
              .setCancelable(false)
              .show();
   }

}
