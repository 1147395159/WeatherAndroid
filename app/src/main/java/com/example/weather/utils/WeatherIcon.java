package com.example.weather.utils;

import java.util.Calendar;

import com.example.weather.R;

public class WeatherIcon {

    //转换天气icon
    public int getWeatherIcon(String weatherIcon) {
        switch (weatherIcon) {
            case "qing":
                return R.drawable.qing;
            case "bingbao":
                return R.drawable.bingbao;
            case "lei":
                return R.drawable.lei;
            case "shachen":
                return R.drawable.shachen;
            case "wu":
                return R.drawable.wu;
            case "xue":
                return R.drawable.xue;
            case "yin":
                return R.drawable.yin;
            case "yu":
                return R.drawable.yu;
            case "yun":
                return R.drawable.yun;
        }
        return R.drawable.weizhi;
    }

    //根据时间天气设置背景
    public static int getWeatherBg(String icon) {
        int wea = Integer.parseInt(icon);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour > 18 || hour < 6) {
            return R.drawable.bg_wan;
        }else {
            if (wea <= 12) {
                return R.drawable.bg_yu;
            } else if (wea <= 20) {
                return R.drawable.bg_xue;
            } else if (wea <= 34) {
                return R.drawable.bg_yun;
            } else if (wea <= 47) {
                return R.drawable.bg_lei;
            } else {
                return R.drawable.bg;
            }
        }
    }

    //天气缩略图
    public static int getRememberWeather(String icon) {
        int wea = Integer.parseInt(icon);
        if (wea <= 12) {
            return R.drawable.rm_yu;
        } else if (wea <= 20) {
            return R.drawable.rm_yin;
        } else if (wea <= 34) {
            return R.drawable.rm_yun;
        } else if (wea <= 47) {
            return R.drawable.rm_yin;
        } else {
            return R.drawable.rm_yun;
        }
    }

}
