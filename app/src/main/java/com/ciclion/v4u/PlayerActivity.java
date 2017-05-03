package com.ciclion.v4u;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    String playlistURL;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playlistURL = getIntent().getStringExtra("playlistURL");
        Filename host = new Filename(playlistURL,'/', '.');
        videoView = (VideoView) findViewById(R.id.videoView);

        new Downloader().execute(playlistURL);


    }

    private void playVideoWithURLS(ArrayList<String> urls){
        for (int i = 0; i < urls.size(); i++) {
            Uri uri = Uri.parse(urls.get(i));
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.start();
        }
    }

    private void parseMediaPlaylist(String data){

        ArrayList<String> urls = new ArrayList<String>();
        String[] sub = data.split(",");

        for(int i = 0; i<sub.length; i++){
            urls.add(sub[i].split("#")[0]);
        }

        for(int i = 0; i<urls.size(); i++) {
            Log.d("urls", urls.get(i));
        }

        playVideoWithURLS(urls);
    }

    private void parseData(final String data){
        //El parameter data es un string que conté el text de l'arxiu m3u8 seleccionat.
        //S'ha de parsejar per saber de quin tipus de playlist es tracta
        Boolean isMasterAdaptativePlaylist = data.contains("#EXT-X-STREAM-INF:");
        Boolean isMasterFixedPlaylist = data.contains("#EXT-X-MEDIA:");
        Boolean isMediaPlaylist = data.contains("#EXTINF:");

        if (isMediaPlaylist){
            parseMediaPlaylist(data);
        }
        else if (isMasterAdaptativePlaylist){

            Log.d("Decisor", "master adaptative playlist");
        }
        else if (isMasterFixedPlaylist){
            CharSequence qualitats[] = new CharSequence[] {"Alta", "Mitja", "Baixa"};
            new AlertDialog.Builder(PlayerActivity.this)
                    .setTitle("Selecciona la qualitat del video")
                    .setItems(qualitats, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parserFixed(data, which);
                        }
            }).show();

            Log.d("Decisor", "master fixed playlist");
        }
    }

    private void parserFixed(String data, int which){
        String quality = "";
        String url = "http://";
        Filename host = new Filename(playlistURL,'/', '.');
        switch (which) {
            case 0:
                quality = "hi";
                break;
            case 1:
                quality = "mid";
                break;
            case 2:
                quality = "low";
                break;
            default:
                break;
        }
        new Downloader().execute(url.concat(host.host()).concat(Helper.splitFixed(data,quality)));
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

            parseData(result);
        }

    }
}
