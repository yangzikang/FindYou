package com.example.dell.findyou;

import com.amap.api.navi.model.NaviLatLng;

/**
 * Created by dell on 2016/9/12.
 */
public class MyData {
    public static String number;
    public static String messageContent;

    public static String myLocation="";
    public static double latitude;
    public static double longitude;

    public static NaviLatLng startPoint;
    public static NaviLatLng endPoint;

    public static void exchangeData()throws Exception{
        //String string = "3904.165219,N12145.901689,E";
        String []strings = messageContent.split(",");
        String stringOfStrings = strings[1].substring(1);
        System.out.println(stringOfStrings);
        latitude = Double.parseDouble(strings[0]);
        latitude=(int)latitude/100+(latitude-(int)(latitude/100)*100)/60;
        longitude =Double.parseDouble(stringOfStrings);
        longitude = (int)longitude/100+(longitude-(int)(longitude/100)*100)/60;

        endPoint = new NaviLatLng(latitude,longitude);
    }
}
