package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.coolweather.service.AutoUpdateService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNowCity;
import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherActivity extends AppCompatActivity {

    private static String weatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView pm10Text;
    private TextView comfortText;
    private TextView wearText;
    private TextView coldText;
    private TextView sportText;
    private TextView UVText;
    private TextView carWashText;
    private ImageView bingPicImg;
    private String nowWeather;
    private String cityName;
    private int bingPic;
    private boolean isExcute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        HeConfig.init("HE1909101751061655", "7a2ec53c0c6745f7ad2acbaf65715f5f");
        HeConfig.switchToFreeServerNode();

        //初始化各控件
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        pm10Text = findViewById(R.id.pm10_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        wearText = findViewById(R.id.wear_text);
        sportText = findViewById(R.id.sport_text);
        UVText = findViewById(R.id.UV_text);
        coldText = findViewById(R.id.cold_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String aqiString = prefs.getString("aqi", null);
        String lifeStyString = prefs.getString("lifesty", null);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            this.bingPic = Integer.parseInt(bingPic);
        }
        this.cityName = prefs.getString("city_name", null);
        final String weatherId;

        if (getIntent().getStringExtra("weather_id") != null) {
            weatherId = getIntent().getStringExtra("weather_id");
            WeatherActivity.weatherId = weatherId;
            SharedPreferences.Editor editor = PreferenceManager.
                    getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("weather_id", weatherId);
            editor.apply();
        } else {
            weatherId = prefs.getString("weather_id", null);
            WeatherActivity.weatherId = weatherId;
        }

        Log.d("ttw", "weatherId: " + weatherId);

        if (bingPic != null) {
            Glide.with(WeatherActivity.this).load(Integer.parseInt(bingPic)).into(bingPicImg);
        } else {
            loadBingPic();
        }

        Gson gson = new Gson();
        if (weatherString != null && aqiString != null && lifeStyString != null) {
            //有缓存时直接解析天气数据
            Weather weatherObj = gson.fromJson(weatherString, new TypeToken<interfaces.heweather.com.interfacesmodule.bean.weather.Weather>() {
            }.getType());
            AirNowCity aqiObj = gson.fromJson(aqiString, new TypeToken<AirNowCity>() {
            }.getType());

            List<LifestyleBase> lifestyleObj = gson.fromJson(lifeStyString, new TypeToken<List<LifestyleBase>>() {
            }.getType());

            showWeatherInfo(weatherObj);
            showWeatherInfo(aqiObj);
            showWeatherInfo(lifestyleObj);
        } else {
            //无缓存时去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(WeatherActivity.weatherId);
                loadBingPic();
                swipeRefresh.setRefreshing(false);
                Log.d("ttw", "onRefresh执行了: ");
            }
        });
    }

    /**
     * 加载每日一图
     */
    private void loadBingPic() {
        if (nowWeather != null) {
            Log.d("ttw", "loadBingPic执行了: ");
            int pic = this.bingPic;
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
                    getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("bing_pic", String.valueOf(pic));
            editor.apply();
            Glide.with(WeatherActivity.this).load(pic).into(bingPicImg);
            isExcute = false;
        }



/*        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendokHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });*/
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        if (weatherId != null) {
            this.weatherId = weatherId;
            HeWeather.getWeather(WeatherActivity.this, weatherId, Lang.CHINESE_SIMPLIFIED,
                    Unit.METRIC, new HeWeather.OnResultWeatherDataListBeansListener() {
                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }

                        @Override
                        public void onSuccess(final interfaces.heweather.com.interfacesmodule.bean.weather.Weather weather) {
                            final String responseText = new Gson().toJson(weather);
                            nowWeather = weather.getNow().getCond_txt();
//                            loadBingPic();
//                            Log.d("ttw", "nowWeather 更新了: ");
                            isExcute = true;
                            cityName = weather.getBasic().getLocation();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (weather.getStatus().equals("ok")) {
                                        SharedPreferences.Editor editor = PreferenceManager.
                                                getDefaultSharedPreferences(WeatherActivity.this).edit();
                                        editor.putString("weather", responseText);
                                        editor.putString("city_name", weather.getBasic().getLocation());
                                        editor.apply();
                                        Intent intent = new Intent(WeatherActivity.this,
                                                AutoUpdateService.class);
                                        intent.putExtra("weather_id", weatherId);
                                        intent.putExtra("now_weather", nowWeather);
                                        startService(intent);
                                        showWeatherInfo(weather);
                                    } else {
                                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    swipeRefresh.setRefreshing(false);
                                }
                            });
                        }
                    });

            HeWeather.getAirNow(WeatherActivity.this, weatherId, Lang.CHINESE_SIMPLIFIED,
                    Unit.METRIC, new HeWeather.OnResultAirNowBeansListener() {
                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }

                        @Override
                        public void onSuccess(final AirNow airNow) {
                            final String aqi = new Gson().toJson(airNow.getAir_now_city());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (aqi != null) {
                                        SharedPreferences.Editor editor = PreferenceManager.
                                                getDefaultSharedPreferences(WeatherActivity.this).edit();
                                        editor.putString("aqi", aqi);
                                        editor.apply();
                                        showWeatherInfo(airNow.getAir_now_city());
                                    } else {
                                        Toast.makeText(WeatherActivity.this, "请求AQI失败",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    swipeRefresh.setRefreshing(false);
                                }
                            });

                        }
                    });

            HeWeather.getWeatherLifeStyle(WeatherActivity.this, weatherId, Lang.CHINESE_SIMPLIFIED,
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
                                                getDefaultSharedPreferences(WeatherActivity.this).edit();
                                        editor.putString("lifesty", lifesty);
                                        editor.apply();
                                        showWeatherInfo(lifestyle.getLifestyle());
                                    } else {
                                        Toast.makeText(WeatherActivity.this, "请求AQI失败",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    swipeRefresh.setRefreshing(false);
                                }
                            });

                        }
                    });
//            while (isExcute) {
//                loadBingPic();
//            }
        }

        /*        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=7a2ec53c0c6745f7ad2acbaf65715f5f";
        HttpUtil.sendokHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Intent intent = new Intent(WeatherActivity.this,
                                    AutoUpdateService.class);
                            startService(intent);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

        });*/
    }

    private void showWeatherInfo(List<LifestyleBase> lifestyle) {
        List<LifestyleBase> lifeStyle = lifestyle;
        int i = 0;
        for (LifestyleBase style : lifeStyle) {
            if (i == 0) {
                String comfort = "舒适度:  " + style.getBrf() + "\n\t\t\t\t" + style.getTxt();
                comfortText.setText(comfort);
                i++;
            } else if (i == 1) {
                String wearSugg = "衣着建议:  " + "\n\t\t\t\t" + style.getTxt();
                wearText.setText(wearSugg);
                i++;
            } else if (i == 2) {
                String coldIndex = "感冒指数:  " + style.getBrf();
                coldText.setText(coldIndex);
                i++;
            } else if (i == 3) {
                String sportIndex = "运动指数:  " + style.getBrf();
                sportText.setText(sportIndex);
                i++;
            } else if (i == 4) {
                i++;
            } else if (i == 5) {
                String UVIndex = "紫外线指数:  " + style.getBrf();
                UVText.setText(UVIndex);
                i++;
            } else if (i == 6) {
                String carWash = "洗车指数:  " + style.getBrf();
                carWashText.setText(carWash);
                i++;
            }
        }
    }

    private void showWeatherInfo(interfaces.heweather.com.interfacesmodule.bean.weather.Weather weather) {
        String degree = weather.getNow().getTmp() + "℃";
        String weatherInfo = weather.getNow().getCond_txt();
        nowWeather = weatherInfo;
        String cityName = this.cityName;
        String updateTime = weather.getUpdate().getLoc();

        nowWeather = weatherInfo;
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        forecastLayout.removeAllViews();
        int i = 0;
        for (ForecastBase forecast : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_min_text);

            String max_min = forecast.getTmp_max() + "℃ / " + forecast.getTmp_min() + "℃";
            if (i == 0) {
                String date[];
                date = forecast.getDate().split("-");
                dateText.setText(date[1] + "月" + date[2] + "日" + "今天");
                i++;
            } else if (i == 1) {
                String date[];
                date = forecast.getDate().split("-");
                dateText.setText(date[1] + "月" + date[2] + "日" + "明天");
                i++;
            } else if (i == 2) {
                String date[];
                date = forecast.getDate().split("-");
                dateText.setText(date[1] + "月" + date[2] + "日" + "后天");
                i++;
            }
            infoText.setText(forecast.getCond_txt_d() + " / " + forecast.getCond_txt_n());
            maxText.setText(max_min);
            forecastLayout.addView(view);
            weatherLayout.setVisibility(View.VISIBLE);
        }
    }


    private void showWeatherInfo(AirNowCity airNow) {
        if (airNow != null) {
            String aqi = aqi = airNow.getAqi();
            String pm25 = airNow.getPm25();
            String pm10 = airNow.getPm10();
            aqiText.setText(aqi);
            pm25Text.setText(pm25);
            pm10Text.setText(pm10);
        }
    }


/*    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_min_text);

            String max_min = forecast.temperature.max + "℃ / " + forecast.temperature.min + "℃";
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(max_min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }*/
}

