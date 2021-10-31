package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cf.androidpickerlibrary.AddressPicker;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.weather.utils.DateUtil;
import com.example.weather.utils.ToastUtil;
import com.example.weather.utils.WeatherIcon;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private final String CITY_INFO = "CITY";//设置参数类型名

    private static final String CURRENT_WEATHER
            = "https://v0.yiketianqi.com/api/worldchina?appid=35613581&appsecret=1iUoxprC";

    private TextView city, time, week, updateTime, temp, tempLowHigh, breath, weatherCn, wind;
    private TextView summarize, airM, feelsLike, altimeter, barometerTrend, humidity, visibility;
    private TextView windSpeed, windDirCompass, windDirDegrees, uvIndex, uvDescription;
    private ImageView weatherIcon;
    private ImageView updateAddress, addressMenu;
    private LinearLayout currentAddress;
    private TextView address;
    private View bg;

    //抽屉相关控件
    private NavigationView navigationView;
    private LinearLayout kevin;
    private DrawerLayout drawerLayout;//抽屉

    //将数据渲染到控件
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            try {
                JSONObject result = new JSONObject(msg.obj.toString());
                JSONObject day = result.getJSONObject("day");
                bg.setBackgroundResource(WeatherIcon.getWeatherBg(day.getString("icon")));
                time.setText(DateUtil.getDay());
                week.setText(DateUtil.getWeek());
                String cityName = result.get("city").toString();
                String updateTimeFormat = result.get("updateTimeFormat").toString().substring(11);
                city.setText(cityName);
                updateTime.setText(String.format("更新时间%1$s", updateTimeFormat));
                //详细信息
                loadBasicsData(day);
                //时段天气
                loadRealTime(result.getJSONArray("hours"));
                //未来15天天气
                loadMonth(result.getJSONArray("month"));
                //保存设置的城市
                saveCityInfo(cityName);
                //加载抽屉  初始化当前位置
//                initDrawer(String.format("%1$s %2$s", DateUtil.getHourMinute(), cityName), "当前使用位置", day.getString("temperature"), day.getString("icon"));//初始化抽屉
            } catch (Exception e) {
                city.setText("定位失败,请重新进入");
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //basic
        init();//初始化控件
        if (!getCityInfo().equals("")) {
            getCurrentWeatherData(String.format("&city=%1$s", getCityInfo()));//获取当前位置天气数据
        } else {
            getCurrentWeatherData("");//获取当前位置天气数据
        }

        commonUseDrawer();//常用地址

        //设置地址
        updateAddress.setOnClickListener(v -> {
            AddressPicker picker = new AddressPicker(MainActivity.this);
            picker.setAddressListener((province, city, area) -> {
                String city1 = city.replace("市", "");
                getCurrentWeatherData(String.format("&city=%1$s", city1));
                address.setText(String.format("%1$s%2$s%3$s", province, city, area));
                ToastUtil.showToast(MainActivity.this, String.format("获取%1$s的天气信息", city1));
                saveCityInfo(city1);
            });
            picker.show();
        });

        //展开抽屉
        addressMenu.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        //获取当前位置天气情况
        currentAddress.setOnClickListener(v -> {
            getCurrentWeatherData("");
            address.setText("当前位置");
            ToastUtil.showToast(MainActivity.this, "获取当前位置");
        });

        //测试
        temp.setOnClickListener(v -> {
            ToastUtil.showToast(MainActivity.this, getCityInfo());
        });


    }

    //初始化控件
    public void init() {
        city = findViewById(R.id.city);
        currentAddress = findViewById(R.id.currentAddress);
        bg = findViewById(R.id.bg_main);
        time = findViewById(R.id.time);
        week = findViewById(R.id.week);
        updateTime = findViewById(R.id.updateTime);
        temp = findViewById(R.id.temp);
        tempLowHigh = findViewById(R.id.temp_low_high);
        breath = findViewById(R.id.breath);
        weatherCn = findViewById(R.id.weatherCn);
        wind = findViewById(R.id.wind);
        weatherIcon = findViewById(R.id.weatherIcon);

        //详细信息初始化
        summarize = findViewById(R.id.summarize);
        airM = findViewById(R.id.airM);
        feelsLike = findViewById(R.id.feelsLike);
        altimeter = findViewById(R.id.altimeter);
        barometerTrend = findViewById(R.id.barometerTrend);
        humidity = findViewById(R.id.humidity);
        visibility = findViewById(R.id.visibility);
        windSpeed = findViewById(R.id.windSpeed);
        windDirCompass = findViewById(R.id.windDirCompass);
        windDirDegrees = findViewById(R.id.windDirDegrees);
        uvIndex = findViewById(R.id.uvIndex);
        uvDescription = findViewById(R.id.uvDescription);

        //地址
        address = findViewById(R.id.address);
        addressMenu = findViewById(R.id.addressMenu);
        updateAddress = findViewById(R.id.updateAddress);

        //抽屉
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.menu);
    }

    //设置UI基础详细信息
    public void loadBasicsData(JSONObject day) throws JSONException {
        temp.setText(String.format("%1$s℃", day.get("temperature").toString()));
        breath.setText(String.format("空气质量%1$s-%2$s", day.get("air").toString(), day.get("air_level").toString()));
        weatherCn.setText(day.get("phrase").toString());
        wind.setText(String.format("%1$s风%2$s km/h", day.get("windDirCompass").toString(), day.get("windSpeed").toString()));
        tempLowHigh.setText(String.format("最高温 %1$s℃", day.get("temperatureMaxSince7am").toString()));
        summarize.setText(String.format("今天: 现在%1$s。最高气温%2$s。体感温度%3$s", day.get("phrase").toString(),
                day.get("temperatureMaxSince7am").toString(), day.get("feelsLike").toString()));
        airM.setText(String.format("%1$s-%2$s", day.get("air").toString(), day.get("air_level").toString()));
        feelsLike.setText(String.format("%1$s℃", day.get("feelsLike").toString()));
        altimeter.setText(String.format("%1$s m百帕", day.getString("altimeter")));
        barometerTrend.setText(day.get("barometerTrend").toString());
        humidity.setText(String.format("%1$s%%", day.get("humidity").toString()));
        visibility.setText(day.get("visibility").toString());
        windSpeed.setText(String.format("%1$skm/h", day.get("windSpeed").toString()));
        windDirCompass.setText(String.format("%1$s风", day.getString("windDirCompass")));
        windDirDegrees.setText(String.format("%1$s°", day.getString("windDirDegrees")));
        uvIndex.setText(String.format("%1$s级", day.get("uvIndex").toString()));
        uvDescription.setText(day.get("uvDescription").toString());
        WeatherIcon icon1 = new WeatherIcon();
        String weaImg = day.get("phrase_img").toString();
        weatherIcon.setImageResource(icon1.getWeatherIcon(weaImg));
    }

    //获取当前城市的天气数据
    public void getCurrentWeatherData(String url) {
        new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(CURRENT_WEATHER + url).build();
                        Response response = client.newCall(request).execute();//执行发送的指令

                        if (response.isSuccessful()) {
                            Message message = handler.obtainMessage();
                            message.obj = Objects.requireNonNull(response.body()).string();
                            handler.sendMessage(message);
                        }
                    } catch (Exception e) {
                        ToastUtil.showToast(MainActivity.this, "网络请求失败");
                    }
                }
            }
        }.start();
    }

    //初始化抽屉控件 时间 城市 温度 图片
    public void initDrawer(String time, String cityC, String temp, String wea) {
        //将nav_header加入到抽屉的上部分
        final View headview = navigationView.inflateHeaderView(R.layout.remember_address);
        kevin = headview.findViewById(R.id.kevin);
        kevin.removeAllViews();
        LinearLayout layout = rememberLayout();
        layout.addView(rememberTextView(time, 10, true));
        layout.addView(rememberTextView(cityC, 16, true));
        kevin.addView(layout);
        kevin.addView(rememberTextView(temp, 40, false));
        kevin.setBackgroundResource(WeatherIcon.getRememberWeather(wea));

        kevin.setOnClickListener(v -> {
            Toast.makeText(headview.getContext(), String.format("查询到%1$s的天气情况", cityC), Toast.LENGTH_SHORT).show();//显示消息提示框
            getCurrentWeatherData(String.format("&city=%1$s", cityC));
            saveCityInfo(cityC);
            address.setText("");
            drawerLayout.closeDrawers();
        });
    }

    //热门城市列表
    public void commonUseDrawer() {
        List<String> list = new ArrayList<>();
        list.add("北京");
        list.add("上海");
        list.add("深圳");
        list.add("广州");
        list.add("成都");
        list.add("西安");
        for (int i = 0; i < list.size(); i++) {
            initDrawer(DateUtil.getHourMinute(), list.get(i), "?", DateUtil.randomWea());
        }

    }

    //####################################### 存储城市信息 ##########################################
    private void saveCityInfo(String cityName) {
        SharedPreferences city = getSharedPreferences(CITY_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = city.edit();
        editor.putString("cityName", cityName);
        editor.apply();
    }

    private String getCityInfo() {
        SharedPreferences cityInfo = getSharedPreferences(CITY_INFO, MODE_PRIVATE);
        return cityInfo.getString("cityName", "");
    }

    //################################ 加载控件 #####################################################
    //加载15天控件
    public void loadMonth(JSONArray month) throws JSONException {
        LinearLayout layout = findViewById(R.id.sevenWeather);
        layout.removeAllViews();
        for (int i = 0; i < month.length(); i++) {
            JSONObject jsonObject = (JSONObject) month.get(i);
            LinearLayout monthLayout = horizontalLayout();
            monthLayout.addView(monthTextView(String.format("%1$s %2$s         ", jsonObject.getString("date"),
                    jsonObject.getString("dateOfWeek")), false, 0f, 14, false));
            JSONObject day = jsonObject.getJSONObject("day");
            monthLayout.addView(monthImageView(day.getString("phrase_img"), 0f));
            //降雨概率
            String precipPct = day.getString("precipPct");
            if (Integer.parseInt(precipPct) >= 5) {
                monthLayout.addView(monthTextView(String.format("%1$s%%", precipPct), true, 0f, 12, true));
            }
            monthLayout.addView(monthTextView(day.getString("phrase"), true, 1f, 12, false));
            monthLayout.addView(monthTextView(String.format("%1$s°  ", day.getString("temperature")), false, 0f, 14, false));

            String morn = String.format("日出%1$s  日落%2$s  降雨概率%3$s%%  湿度%4$s%%  风速%5$skm/h",
                    jsonObject.getString("sunrise"), jsonObject.getString("sunset"),
                    day.getString("precipPct"), day.getString("humidity"), day.getString("windSpeed"));
            layout.addView(monthLayout);
            layout.addView(monthDetail(morn));
        }
    }

    //实时时段天气
    public void loadRealTime(JSONArray hour) throws JSONException {
        LinearLayout layout = findViewById(R.id.scene_layout);
        layout.removeAllViews();
        for (int i = 0; i < hour.length(); i++) {
            JSONObject jsonObject = (JSONObject) hour.get(i);
            LinearLayout sceneLayout = verticalLayout();
            sceneLayout.addView(sceneTextView(jsonObject.getString("time").substring(0, 2) + "时"));
            sceneLayout.addView(sceneImage(jsonObject.getString("wea_img")));
            sceneLayout.addView(sceneTextView(jsonObject.getString("tem") + "°"));
            layout.addView(sceneLayout);
        }
    }

    //################################### 创建控件 ##################################################

    //线性布局 居中 水平
    public LinearLayout horizontalLayout() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 40, 0, 20);
        linearLayout.setLayoutParams(layoutParams);
//        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(10, 10, 10, 5);
        return linearLayout;
    }

    //线性布局 居中 垂直
    public LinearLayout verticalLayout() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                130, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(6, 0, 0, 0);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(0x00FFFFFF);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 5);
        return linearLayout;
    }

    //15天textView控件
    public TextView monthTextView(String param, boolean isCenter, float weight, int size, boolean isBlue) {
        TextView textView1 = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        textViewParams.setMargins(20, 0, 0, 0);
        textView1.setLayoutParams(textViewParams);
        if (isBlue) {
            textView1.setTextColor(0xFF94D7F8);
        } else {
            textView1.setTextColor(0xFFFFFFFF);
        }
        if (isCenter) {
            textView1.setGravity(Gravity.CENTER);
        }
        textView1.setTextSize(size);
        textView1.setText(param);
        return textView1;
    }

    public TextView monthDetail(String param) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(50, 0, 0, 0);
        textView.setLayoutParams(textViewParams);
        textView.setTextSize(9);
        textView.setTextColor(0x88FFFFFF);
        textView.setPadding(1, 1, 1, 1);
        textView.setText(param);
        return textView;
    }

    public ImageView monthImageView(String weathericon, float weight) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                100, 50, weight);
        imageView.setLayoutParams(imageViewParams);
        WeatherIcon weatherIcon1 = new WeatherIcon();
        imageView.setImageResource(weatherIcon1.getWeatherIcon(weathericon));
        return imageView;
    }

    //实时时段天气
    public TextView sceneTextView(String time) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(textViewParams);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        textView.setText(time);
        textView.setTextSize(15);
        return textView;
    }

    public ImageView sceneImage(String weathericon) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                100, 50);
        imageViewParams.setMargins(0, 20, 0, 20);
        imageView.setLayoutParams(imageViewParams);
        WeatherIcon weatherIcon1 = new WeatherIcon();
        imageView.setImageResource(weatherIcon1.getWeatherIcon(weathericon));
        return imageView;
    }

    //抽屉的控件
    public TextView rememberTextView(String time, int size, boolean padding) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(0, 0, 40, 0);
        textView.setLayoutParams(textViewParams);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        if (padding) {
            textView.setPadding(40, 10, 0, 0);
        }
        textView.setText(time);
        textView.setTextSize(size);
        return textView;
    }

    public LinearLayout rememberLayout() {
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 9f);
        layout.setLayoutParams(layoutParams);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

}