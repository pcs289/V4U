package com.ciclion.v4u;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import io.vov.vitamio.MediaPlayer;

/**
 * Player activity with a videoView to play the m3u8 source selected in the Browser activity
 */
public class PlayerActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    String playlistURL, serverURL;
    VideoView videoView;
    Boolean isAdaptative, isTesting;
    Double currentBandwidth, hiBW, midBW, lowBW;
    ArrayList<String> urlsAdaptativeHi, urlsAdaptativeMid, urlsAdaptativeLow, urlsMediaPlaylist;
    int currentVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!LibsChecker.checkVitamioLibs(this)){
            return;
        }
        setContentView(R.layout.activity_player);
        playlistURL = getIntent().getStringExtra("playlistURL");
        serverURL = getIntent().getStringExtra("serverURL");
        videoView = (VideoView) findViewById(R.id.videoView);
        mediaPlayer = new MediaPlayer(this);
        isAdaptative = false;
        isTesting = false;
        currentVideo = 0;

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentVideo++;
                if(isAdaptative){
                    int quality = evaluateChannel();
                    if(quality == 1){ //qualitat alta
                        playVideoWithURLS(urlsAdaptativeHi.get(currentVideo));
                        Log.d("Qualitat video", "Alta");
                    } else if(quality == 2){ //qualitat mitja
                        playVideoWithURLS(urlsAdaptativeMid.get(currentVideo));
                        Log.d("Qualitat video", "Mitja");
                    } else if(quality == 3){ //qualitat baixa
                        playVideoWithURLS(urlsAdaptativeLow.get(currentVideo));
                        Log.d("Qualitat video", "Baixa");
                    }
                }else{
                    playVideoWithURLS(urlsMediaPlaylist.get(currentVideo));
                }
            }
        });


        new Downloader().execute(playlistURL);
    }

    /**
     * It changes the videoView uri expecting for the onPreparedListener to play it once loaded
     * @param uri URI object to be set to the videoView
     */
    private void playVideo(Uri uri){
        Log.d("Reproduint video", ""+uri);
        videoView.setVideoURI(uri);
    }

    /**
     * Parses string urls into URI objects
     * @param url URL string to be parsed into an URI object
     */
    private void playVideoWithURLS(String url){
        Uri uri = Uri.parse(url);
        Log.d("playVideoWithURLS", ""+url);
        playVideo(uri);
    }

    /**
     * It evaluates channel bandwidth downloading a file of known size storing its result in the global variable currentBandwidth
     * @return quality
     */
    private int evaluateChannel() {
        int quality = 0;
        try {
            new Timer().execute().get();
            Log.d("Avaluant Canal", "Timer finalitzat amb currentBandwidth "+currentBandwidth);
            if(currentBandwidth >= hiBW){
                quality = 1;
            }else if(currentBandwidth >= midBW){
                quality = 2;
            }else{
                quality = 3;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d("Qualitat del canal", ""+quality);
        return quality;
    }

    /**
     * It acts like a decisor to choose amongst the three different types of playlist
     * @param data downloaded string containing the m3u8 text to be parsed
     */
    private void parseData(final String data){
        //El parametre data es un string que conté el text de l'arxiu m3u8 seleccionat.
        //S'ha de parsejar per saber de quin tipus de playlist es tracta
        Boolean isMasterAdaptativePlaylist = data.contains("#EXT-X-STREAM-INF:");
        Boolean isMasterFixedPlaylist = data.contains("#EXT-X-MEDIA:");
        Boolean isMediaPlaylist = data.contains("#EXTINF:");

        if (isMediaPlaylist){
            parserMediaPlaylist(data, currentVideo);
            Log.d("Decisor", "Media Playlist");
        }
        else if (isMasterAdaptativePlaylist){
            isAdaptative = true;
            parserAdaptative(data);
            Log.d("Decisor", "Master Adaptative Playlist");
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
            Log.d("Decisor", "Master Fixed Playlist");
        }
    }

    /**
     * It parses a mediaPlaylist which has come from a AdaptativeMediaPlaylist
     * @param urls ArrayList of URLS depending on bandwidth
     * @param currentVideo reference counter on the current cut of the full video
     */
    private void parserMediaPlaylistFromAdaptative (ArrayList<String> urls, int currentVideo){
        playVideoWithURLS(urls.get(currentVideo));
    }

    /**
     * It parses a mediaPlaylist to get the URL into an ArrayList of Strings
     * @param data Downloaded String containing mediaPlaylist text
     * @param currentVideo reference counter on the current cut of the full video
     */
    private void parserMediaPlaylist(String data, int currentVideo){
        urlsMediaPlaylist = Helper.getUrlsFromMediaPlaylist(data);
        for(int i = 0; i<urlsMediaPlaylist.size(); i++) {
            Log.d("urls", urlsMediaPlaylist.get(i));
        }

        playVideoWithURLS(urlsMediaPlaylist.get(currentVideo));
    }

    /**
     * It parses adaptatives master playlists to get its different bandwidth and URLs
     * @param data Downloaded String containing MasterAdaptativePlaylist text
     */
    private void parserAdaptative(String data){
        ArrayList<String> info = Helper.infoAdaptative(data);
        for (int i = 1; i < info.size(); i += 2){ //ens descarreguem els 3 tipus d'adaptatives
            new Downloader().execute(info.get(i));
        }
        Log.d("Descarregades", "Tres qualitats adaptatives");
        hiBW = Double.parseDouble(info.get(4));
        midBW = Double.parseDouble(info.get(2));
        lowBW = Double.parseDouble(info.get(0));
    }

    /**
     * It parses fixed master playlists to get the URLs depending upon quality selected
     * @param data Downloaded String containing MasterFixedPlaylist text
     * @param which Integer representing the three diferent options selected by the user(high, mid or low quality)
     */
    private void parserFixed(String data, int which){
        String quality = "";
        String url = "http://";
        String host = Helper.host(playlistURL);
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

        String mediaPlaylistURL = url.concat(host).concat("/").concat(Helper.splitFixed(data,quality));
        Log.d("MediaPlaylistURL-Parsed", mediaPlaylistURL);
        new Downloader().execute(mediaPlaylistURL);
    }

    /**
     * It reads inputStreams and converts it into a string
     * @param is inputStream to be converted
     * @return returns the String out of the converted InputStream
     */
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

    /**
     * It converts an input string into a URL object
     * @param urlString string to be converted into URL object
     * @return the converted URL
     */
    private  URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AsyncTask in charge of downloading the m3u8 files through HTTP
     */
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
            if(result.contains("BANDWIDTH")) {
                parseData(result);
                return;
            }

            if(isAdaptative) {
                Log.d("MasterPlaylist", "Adaptativa");
                if (result.contains("hi")) {
                    urlsAdaptativeHi = Helper.getUrlsFromMediaPlaylist(result);
                    parserMediaPlaylistFromAdaptative(urlsAdaptativeHi, currentVideo);
                } else if (result.contains("lo")) {
                    urlsAdaptativeLow = Helper.getUrlsFromMediaPlaylist(result);
                    parserMediaPlaylistFromAdaptative(urlsAdaptativeLow, currentVideo);
                } else if (result.contains("me")) {
                    urlsAdaptativeMid = Helper.getUrlsFromMediaPlaylist(result);
                    parserMediaPlaylistFromAdaptative(urlsAdaptativeMid, currentVideo);
                }
            }else {
                Log.d("MasterPlaylist", "Fixa o mediaPlaylist");
                parseData(result);
            }
        }

    }

    /**
     * AsyncTask in charge of downloading a specific file in order to estimate the channel's bandwidth
     */
    private class Timer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... timer){
            long startTime = System.currentTimeMillis();
            URL url = stringToURL(serverURL+"/Justice-dance/hi/fileSequence5.ts");
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

            return String.valueOf(startTime);
        }

        protected void onPostExecute(String startTimer) {
            long endTime = System.currentTimeMillis();
            long time = endTime - Long.parseLong(startTimer);
            currentBandwidth = (3.7*1024*1024)/((double) time);//tamany del arxiu entre el temps de descarrega
            Log.d("Bandwidth del canal", "Calculat");
        }

    }
}
