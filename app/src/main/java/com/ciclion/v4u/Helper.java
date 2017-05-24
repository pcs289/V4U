package com.ciclion.v4u;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Helper class with some commonly used methods
 * It has been developed by Cai and Pau
 */
public class Helper {

    /**
     * It splits the 'data' string until the 'buscar' string is reached
     * @param data main string to be splitted
     * @param buscar substring to search in 'data'
     * @return the substring found in 'data'
     */
    public static String splitFixed(String data ,String buscar){
        String[] parts = data.split(buscar);
        parts = parts[2].split("\"");
        return buscar+""+parts[0];
    }

    /**
     * It parses a mediaPlaylist from a String into an ArrayList<String> containing the URLS
     * @param data mediaPlaylist plain text String
     * @return ArrayList of URLS parsed out from the mediaPlaylist
     */
    public static ArrayList<String> getUrlsFromMediaPlaylist(String data){
        ArrayList<String> urls = new ArrayList<String>();
        String[] sub = data.split(",");

        for(int i = 1; i<sub.length; i++){
            urls.add(sub[i].split("#")[0]);
        }
        return urls;
    }

    /**
     * It removes the first character of all the elements of the input ArrayList<String>
     * @param ar input array to be modified
     * @return the input array without the first character '/t'
     */
    public static ArrayList<String> extractT(ArrayList<String> ar){
        for (int i=0; i < ar.size(); i++){
            ar.add(i, ar.get(i).substring(1));
        }

        return ar;
    }

    /**
     * It splits a main string into a substring from a starting sequence until an ending sequence
     * @param data main string to be splitted
     * @param start beginning of the sequence
     * @param end end of the sequence
     * @return the string inbetween the 'start' and 'end' sequences from the 'data' string
     */
    public static String betweenStrings(String data, String start, String end){
        String retorn = data.split(start)[1];
        return retorn.split(end)[0];
    }

    /**
     * It parses an adaptativeMediaPlaylist into an ArrayList containing URLS and Bandwidths
     * @param data adaptativeMediaPlaylist string to be parsed
     * @return ArrayList of URLS and Bandwidths
     */
    public static ArrayList<String> infoAdaptative (String data){
        ArrayList<String> retorn = new ArrayList<String>();
        String[] parts = data.split("BANDWIDTH=");
        for (int i = 1; i < parts.length; i++){
            retorn.add(parts[i].split(",CODECS")[0]);
            retorn.add("http" + betweenStrings(parts[i], "http", ".m3u8") + ".m3u8");
        }
        return retorn;
    }

    /**
     * It separates the extension from a filename
     * @param fullPath main string to be parsed
     * @param extensionSeparator character to separate
     * @return the file extension of the given fullPath
     */
    public static String extension(String fullPath, char extensionSeparator) {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    /**
     * It gets the host of a URL
     * @param fullPath main string to be parsed
     * @return the host of a URL
     */
    public static String host(String fullPath){
        return fullPath.split("/")[2];
    }


}
