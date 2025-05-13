package com.devatikul.inappupdate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.URLUtil;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppUpdater {
   private final Context context;
   private String updateFileUrl;
   private String updateFileName;
   private String connectionError;
   private onDownloadListener onDownloadListener;

   public AppUpdater(Context context) {
      this.context = context;
   }

   public AppUpdater setAppUrl(String url) {
      if (URLUtil.isValidUrl(url)) {
         this.updateFileUrl = url;
      } else {
         throw new IllegalArgumentException("app url is not valid");
      }
      return this;
   }

   public AppUpdater setFileNameFromUrl() {
      if (updateFileUrl.isEmpty()) {
         throw new IllegalArgumentException("url is empty");
      } else {
         this.updateFileName = URLUtil.guessFileName(updateFileUrl, null, null);
      }
      return this;
   }

   public AppUpdater setFileName(String fileName) {
      if (fileName.isEmpty()) {
         throw new IllegalArgumentException("file name is not defined");
      } else {
         this.updateFileName = fileName;
      }
      return this;
   }

   public AppUpdater setOnDownloadListener(onDownloadListener onUpdateDownloadListener) {
      this.onDownloadListener = onUpdateDownloadListener;
      return this;
   }

   public void start() {
      new DownloadTask().startDownload(updateFileUrl);
   }


   private class DownloadTask {

      private File downloadFile;
      private final ExecutorService executorService;

      public DownloadTask() {
         executorService = Executors.newSingleThreadExecutor();
      }

      public void startDownload(final String fileUrl) {
         executorService.submit(() -> doInBackground(fileUrl));
      }

      private void doInBackground(String fileUrl) {
         try {
            if (onDownloadListener != null) {
               new Handler(Looper.getMainLooper()).post(() -> onDownloadListener.onBuffer("Downloading..."));
            }

            downloadFile = new File(context.getCacheDir(), updateFileName);

            if (downloadFile.exists()) {
               downloadFile.delete();
            }

            URL url = new URL(fileUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(12000);
            httpURLConnection.setReadTimeout(12000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            int fileLength = httpURLConnection.getContentLength();
            InputStream inputStream = httpURLConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);

            byte[] bytes = new byte[1024];
            int total = 0;
            int count;

            while ((count = inputStream.read(bytes)) != -1) {
               if (Thread.interrupted()) {
                  inputStream.close();
                  fileOutputStream.close();
                  return;
               }

               total += count;
               if (fileLength > 0) {
                  int progress = total * 100 / fileLength;
                  new Handler(Looper.getMainLooper()).post(() -> {
                     if (onDownloadListener != null) {
                        onDownloadListener.onProgress(progress);
                     }
                  });
               }

               fileOutputStream.write(bytes, 0, count);
            }

            inputStream.close();
            fileOutputStream.close();
            installApk(context, downloadFile);

            new Handler(Looper.getMainLooper()).post(() -> {
               if (onDownloadListener != null) {
                  onDownloadListener.onComplete(downloadFile);
               }
            });

         } catch (Exception e) {
            connectionError = e.getMessage();
            new Handler(Looper.getMainLooper()).post(() -> {
               if (onDownloadListener != null) {
                  onDownloadListener.onError(connectionError);
               }
            });
         }
      }
   }


   private void installApk(Context context, File file) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(getUriPath(file), "application/vnd.android.package-archive");
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      context.startActivity(intent);
   }

   private Uri getUriPath(File file) {
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
         return Uri.fromFile(file);
      } else {
         return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
      }
   }
}