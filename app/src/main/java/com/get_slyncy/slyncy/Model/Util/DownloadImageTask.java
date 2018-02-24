package com.get_slyncy.slyncy.Model.Util;

import android.os.AsyncTask;
import android.os.Build;

import com.get_slyncy.slyncy.View.ConfirmationActivity;
import com.get_slyncy.slyncy.View.LoginActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by undermark5 on 2/23/18.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Void>
{
    private String cachePath;
    private PostExecCallBack callBack;

    public DownloadImageTask(String cachePath, PostExecCallBack callBack)
    {
        this.cachePath = cachePath;
        this.callBack = callBack;
    }

    protected Void doInBackground(String... urls) {
        String urldisplay = urls[0];
        try
        {
            InputStream in = new URL(urldisplay).openStream();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                Files.copy(in,new File(cachePath + "/profilePic.jpg").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            else
            {
                File targetFile = new File(cachePath + "/profilePic.jpg");
                OutputStream outStream = new FileOutputStream(targetFile);

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                in.close();
                outStream.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        callBack.callBack();
    }
    public interface PostExecCallBack
    {
        void callBack();
    }
}
