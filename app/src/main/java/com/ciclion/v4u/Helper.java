package com.ciclion.v4u;

import java.util.ArrayList;

/**
 * Created by alber on 03/05/2017.
 */

public class Helper {

    public static String splitFixed(String data ,String buscar){
        String[] parts = data.split(buscar);
        parts = parts[2].split("\"");
        return buscar+""+parts[0];
    }

    public static ArrayList<String> getUrlsFromMediaPlaylist(String data){
        ArrayList<String> urls = new ArrayList<String>();
        String[] sub = data.split(",");

        for(int i = 0; i<sub.length; i++){
            urls.add(sub[i].split("#")[0]);
        }
        return urls;
    }

    public static String betweenStrings(String data, String start, String end){
        String retorn = data.split(start)[1];
        return retorn.split(end)[0];
    }

    public static ArrayList<String> infoAdaptative (String data){
        ArrayList<String> retorn = new ArrayList<String>();
        String[] parts = data.split("BANDWIDTH=");
        for (int i = 1; i < parts.length; i++){
            retorn.add(parts[i].split(",CODECS")[0]);
            retorn.add("http" + betweenStrings(parts[i], "http", "/prog") + "/prog");
        }
        return retorn;
    }

    public static String extension(String fullPath, char extensionSeparator) {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    public static String host(String fullPath){
        return fullPath.split("/")[2];
    }

    public static String basename(String fullPath, char pathSeparator, char extensionSeparator) {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        int sep = fullPath.lastIndexOf(pathSeparator);
        return fullPath.substring(sep + 1, dot);
    }


}
