package com.devatikul.inappupdate;

import java.io.File;

public interface onDownloadListener {
   void onBuffer(String connectingMsg);

   void onProgress(int progress);

   void onComplete(File path);

   void onError(String errorMsg);
}
