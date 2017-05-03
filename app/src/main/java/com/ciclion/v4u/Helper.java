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


}
