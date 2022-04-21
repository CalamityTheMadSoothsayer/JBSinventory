package com.rockwell.jbsinv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class updater extends AppCompatActivity {

    String lastfile = "";
    Integer buttonPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);

        TextView progressTxt = (TextView) findViewById(R.id.updaterTXT);
        progressTxt.setText("Press button to download.");
    }

    public void onClick(View v) {
        buttonPressed = 0;

        switch (v.getId()) {
            case R.id.button:
                new DownloadFileFromURL().execute("https://www.rockwell.click/InventoryJBS.xls");
                buttonPressed = 1;
                break;
            case R.id.button2:
                new DownloadFileFromURL().execute("https://www.rockwell.click/products.txt");
                buttonPressed = 2;
                break;
            case R.id.button3:
                new DownloadFileFromURL().execute("https://www.rockwell.click/chemicals.txt");
                buttonPressed = 3;
                break;
            case R.id.button4:
                DeleteFolderContents(new File(String.valueOf(getExternalFilesDir("/"))));
                break;
        }
    }

        private void DeleteFolderContents(File folder)
        {
            Boolean failure = false;
            TextView progressTxt = (TextView) findViewById(R.id.updaterTXT);
            File directory = folder;

// Get all files in directory

            File[] files = directory.listFiles();
            for (File file : files)
            {
                // Delete each file
                if (!file.delete())
                {
                    // Failed to delete file
                    progressTxt.setText("Failed to clear folder.");
                    failure = true;
                }
            }
            if(!failure){
                progressTxt.setText("Success. Ready to update.");
            }
        }



    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView progressTxt = (TextView) findViewById(R.id.updaterTXT);
            progressTxt.setText("Starting download...");

        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                String root = Environment.getExternalStorageDirectory().toString();

                TextView progressTxt = (TextView) findViewById(R.id.updaterTXT);
                progressTxt.setText("Downloading...");
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                if(buttonPressed == 1) {
                    lastfile = getExternalFilesDir(null) + "/InventoryJBS.xls";
                }else if(buttonPressed == 2){
                    lastfile = getExternalFilesDir(null) + "/products.txt";
                } else if (buttonPressed == 3) {
                    lastfile = getExternalFilesDir(null) + "/chemicals.txt";
                }
                    OutputStream output = new FileOutputStream(lastfile);
                    byte data[] = new byte[1024];


                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }



        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            TextView progressTxt = (TextView) findViewById(R.id.updaterTXT);
            progressTxt.setText("Download complete.");
        }

    }
}