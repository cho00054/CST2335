package com.example.cho00054.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherForecast extends Activity {
    TextView currentTv;
    TextView minTv;
    TextView maxTv;
    ImageView iconTv;
    ProgressBar progressBarWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        progressBarWeather = (ProgressBar) findViewById(R.id.progressBarWeather);
        progressBarWeather.setVisibility(View.VISIBLE);

        currentTv = (TextView) findViewById(R.id.textViewCurrentTemp);
        minTv = (TextView) findViewById(R.id.textViewMinTemp);
        maxTv = (TextView) findViewById(R.id.textViewMaxTemp);
        iconTv =(ImageView) findViewById(R.id.imageViewWeather);

        ForecastQuery fq = new ForecastQuery();
        fq.execute("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");
    }

    private class ForecastQuery extends AsyncTask<String, Integer, String> {
        String minTemp, maxTemp, currentTemp = null; //???
        Bitmap weatherIconBm = null; // picture for the current weather

        @Override
        protected String doInBackground(String ... args) {
            for(String siteUrl: args) {
                try {
                    URL url = new URL(siteUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inStream = urlConnection.getInputStream();

                    XmlPullParser xpp = Xml.newPullParser();
                    xpp.setInput(inStream, null);

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String tagName = xpp.getName();
                            if (tagName.equals("temperature")) {
                                //??? you must the get the value, min, and max parameters and save the text
                                //???? the attributes you are looking for are “speed”,“value”, “min”, “max”.
                                currentTemp = xpp.getAttributeValue(null, "value");
                                publishProgress(25);
                                Thread.sleep(200);
                                minTemp = xpp.getAttributeValue(null, "min");
                                publishProgress(50);
                                Thread.sleep(200);
                                maxTemp = xpp.getAttributeValue(null, "max");
                                publishProgress(75);
                                Thread.sleep(200);
                            }
                            if (tagName.equals("weather")) {
                                String iconName = xpp.getAttributeValue(null, "icon");
                                if (fileExistance(iconName + ".png")) {
                                    Log.i(iconName + ".png:", "found locally");
                                    //if exists, read the image from your disk
                                    FileInputStream fis = null;
                                    try {
                                        fis = new FileInputStream(getBaseContext().getFileStreamPath(iconName + ".png"));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    weatherIconBm = BitmapFactory.decodeStream(fis);
                                } else {
                                    Log.i(iconName + ".png:", iconName + "downloading from the Internet.");
                                    //Save the Bitmap object to the local application storage
                                    weatherIconBm = HttpUtils.getImage("http://openweathermap.org/img/w/" + iconName + ".png");
                                    FileOutputStream outputStream = openFileOutput(iconName + ".png", Context.MODE_PRIVATE);
                                    weatherIconBm.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                }
                                publishProgress(100);
                                Thread.sleep(200);
                            }
                        }

                        xpp.next();
                        eventType = xpp.getEventType();
                    }
                } catch (MalformedURLException e) {
                    Log.e("MalformedURLException:", e.getMessage());
                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException:", e.getMessage());
                } catch (IOException e) {
                    Log.e("IOException:", e.getMessage());
                } catch (XmlPullParserException e) {
                    Log.e("XmlPullParserException:", e.getMessage());
                } catch (InterruptedException e) {
                    Log.e("XmlPullParserException:", e.getMessage());
                }
            }

            return "Finished Downloading"; // how to return ????
        }
        //update the gui:
        public void onProgressUpdate(Integer ... args)
        {
            progressBarWeather.setProgress(args[0]);
        }

        //Gui thread, computation is finished
        public void onPostExecute(String result)
        {
            currentTv.setText("Current Temperature : " + currentTemp);
            minTv.setText("Minimum Temperature : " + minTemp);
            maxTv.setText("Maximum Temperature : " + maxTemp);
            iconTv.setImageBitmap(weatherIconBm);
            progressBarWeather.setVisibility(View.INVISIBLE);
        }

        public boolean fileExistance(String fname){
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }
    }

    public static class HttpUtils {
        public static Bitmap getImage(URL url) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return BitmapFactory.decodeStream(connection.getInputStream());
                } else
                    return null;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        public static Bitmap getImage(String urlString) {
            try {
                URL url = new URL(urlString);
                return getImage(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }
}
