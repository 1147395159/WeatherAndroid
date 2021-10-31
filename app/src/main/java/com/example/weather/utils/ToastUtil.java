package com.example.weather.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;

    @SuppressLint("ShowToast")
    public static void showToast(Context context, String content) {
        try {
            if (toast == null) {
                toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                toast.setText(content);
            }
            toast.show();
        }catch (Exception e){
            Log.d("Toast", "出现异常");
        }

    }
}
