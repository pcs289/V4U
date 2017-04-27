package com.ciclion.v4u;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PlayerActivity extends AppCompatActivity {

    String playlistURL;
    VideoView videoView;

    String fileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playlistURL = getIntent().getStringExtra("playlistURL");

        videoView = (VideoView) findViewById(R.id.videoView);

        new Downloader().execute(playlistURL);


    }

    private void parseData(String data){
        //El parameter data es un string que conté el text de l'arxiu m3u8 seleccionat.
        //S'ha de parsejar per saber de quin tipus de playlist es tracta
    }

    private  String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private  URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }


    private class Downloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urlString){

            URL url = stringToURL(urlString[0]);
            String resultString = null;
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();


                InputStream inputStream = connection.getInputStream();

                resultString = getStringFromInputStream(inputStream);


            }catch(IOException e){
                e.printStackTrace();
            }finally{
                connection.disconnect();
            }

            return resultString;
        }

        protected void onPostExecute(String result) {

            Log.d("Download result", result);

            fileData = result;

            parseData(fileData);
        }

    }
}
