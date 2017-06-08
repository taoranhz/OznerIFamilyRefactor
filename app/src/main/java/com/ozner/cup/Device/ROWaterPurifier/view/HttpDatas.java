package com.ozner.cup.Device.ROWaterPurifier.view;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by taoran on 2017/6/6.
 */

public class HttpDatas {

    public  static void getStatus(Context context, String status,String msg){
        if(status.equals("2")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("3")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("4")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("5")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("6")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("7")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("9")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("10")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("11")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("97")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("98")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }else if(status.equals("-1")){
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
        }
    };



}
