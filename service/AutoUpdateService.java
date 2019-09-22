package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import com.google.gson.Gson;

import java.io.IOException;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    String weatherId;
    String nowWeather;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HeConfig.init("HE1909101751061655", "7a2ec53c0c6745f7ad2acbaf65715f5f");
        HeConfig.switchToFreeServerNode();
        this.weatherId = intent.getStringExtra("weather_id");
        this.nowWeather = intent.getStringExtra("now_weather");

//        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 20 * 60 * 1000;    //20min的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        /*HeWeather.getWeather(this, weatherId, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultWeatherDataListBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(final interfaces.heweather.com.interfacesmodule.bean.weather.Weather weather) {
                        final String responseText = new Gson().toJson(weather);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather.getStatus().equals("ok")) {
                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                    editor.putString("weather", responseText);
                                    editor.apply();
                                    Intent intent = new Intent(AutoUpdateService.this,
                                            AutoUpdateService.class);
//                                    intent.putExtra()
                                    startService(intent);
                                    showWeatherInfo(weather);
                                } else {
                                    Toast.makeText(AutoUpdateService.this, "获取天气信息失败",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

        HeWeather.getAirNow(AutoUpdateService.this, weatherId, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultAirNowBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(final AirNow airNow) {
                        final String aqi = new Gson().toJson(airNow.getAir_now_city());
                        if (aqi != null) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("aqi", aqi);
                            editor.apply();
                            showWeatherInfo(airNow.getAir_now_city());
                            Log.d("ttw", "aqi now :" + airNow.getAir_now_city());
                        } else {
                            Toast.makeText(AutoUpdateService.this, "请求AQI失败",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        HeWeather.getWeatherLifeStyle(AutoUpdateService.this, weatherId, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultWeatherLifeStyleBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(final Lifestyle lifestyle) {
                        final String lifesty = new Gson().toJson(lifestyle.getLifestyle());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (lifesty != null) {
                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                    editor.putString("lifesty", lifesty);
                                    editor.apply();
                                    showWeatherInfo(lifestyle.getLifestyle());
                                } else {
                                    Toast.makeText(AutoUpdateService.this, "请求AQI失败",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });*/


/*        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null) {
            final Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                    "&key=bc0418b57b2d4918819d3974ac1285d9";
            HttpUtil.sendokHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }*/
    }

    private void updateBingPic() {
        int pic = 0;
        if (nowWeather.contains("雨")) {
            pic = R.drawable.bg_rain;
        } else if (nowWeather.contains("多云")) {
            pic = R.drawable.bg_mostly_cloudy;
        } else if (nowWeather.contains("晴")) {
            pic = R.drawable.bg_sunny;
        } else if (nowWeather.contains("阴")) {
            pic = R.drawable.bg_cloudy;
        } else if (nowWeather.contains("雪")) {
            pic = R.drawable.bg_snow;
        }
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(this).edit();
        editor.putString("bing_pic", String.valueOf(pic));
        editor.apply();


/*        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendokHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });*/
    }
}
