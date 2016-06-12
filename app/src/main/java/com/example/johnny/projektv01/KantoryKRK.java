package com.example.johnny.projektv01;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class KantoryKRK extends AppCompatActivity {
    ArrayList<String> cantorList = new ArrayList<>();
    ArrayList<String> cantorListLat = new ArrayList<>();
    ArrayList<String> cantorListLng = new ArrayList<>();
    ArrayList<String> cantorListName = new ArrayList<>();
    ListView list;

    public class DownloadTaskJSON extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";

            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader= new InputStreamReader(in);

                int data = reader.read();
                while(data != -1){
                    char current  = (char) data;

                    result += current;
                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String places = "";
            try {
                JSONObject jsonObject = new JSONObject(result);
                places = jsonObject.getString("results");
                JSONArray arr = new JSONArray(places);

                for (int i = 0; i < arr.length(); i++){
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String name = jsonPart.getString("name");
                    cantorListName.add(i,name);
                    String address = jsonPart.getString("vicinity");
                    String location = jsonPart.getJSONObject("geometry").getString("location");
                    JSONObject jsonLOC = new JSONObject(location);
                    String lat = jsonLOC.getString("lat");
                    cantorListLat.add(i,lat);
                    String lng = jsonLOC.getString("lng");
                    cantorListLng.add(i,lng);
                    //Log.i("lat/lng", lat+"/"+lng);
                    cantorList.add(i,name +" - "+address);
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,cantorList);

                list.setAdapter(arrayAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.i("website content", places);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kantory_krk);

        DownloadTaskJSON task = new DownloadTaskJSON();
        task.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=50.062461,%2019.946157&radius=10000&name=%22kantor%22&sensor=true&key=AIzaSyCYBoox_hqWpk5Fj4mUlqf7nXMX71UWKoo");
        list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                String name = cantorListName.get(position);
                String lat = cantorListLat.get(position);
                String lng = cantorListLng.get(position);
                i.putExtra("NAME", name);
                i.putExtra("LAT",lat);
                i.putExtra("LNG",lng);
                startActivity(i);
            }
        });
    }
}
