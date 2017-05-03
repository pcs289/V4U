package com.ciclion.v4u;

/**
 * Created by alber on 03/05/2017.
 */

public class Helper {

    public static String splitFixed(String data ,String buscar){
        String[] parts = data.split(buscar);
        parts = parts[2].split("\"");
        return buscar+""+parts[0];
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
