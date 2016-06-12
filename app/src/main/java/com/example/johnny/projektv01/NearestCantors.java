package com.example.johnny.projektv01;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class NearestCantors extends FragmentActivity implements OnMapReadyCallback {
    String lat = "";
    String lng = "";
    private GoogleMap mMap;

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
                    //cantorListName.add(i,name);
                    String location = jsonPart.getJSONObject("geometry").getString("location");
                    JSONObject jsonLOC = new JSONObject(location);
                    String lat = jsonLOC.getString("lat");
                    String lng = jsonLOC.getString("lng");
                    Double latD = Double.parseDouble(lat);
                    Double lngD = Double.parseDouble(lng);
                    LatLng place = new LatLng(latD, lngD);
                    mMap.addMarker(new MarkerOptions().position(place).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_cantors);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getString("LAT");
            lng = extras.getString("LNG");
        }
        Double latDouble = Double.parseDouble(lat);
        Double lngDouble = Double.parseDouble(lng);
        LatLng center = new LatLng(latDouble,lngDouble);
        mMap.addMarker(new MarkerOptions().position(center));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center,15));
        DownloadTaskJSON task = new DownloadTaskJSON();
        task.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+",%20"+lng+"&radius=10000&name=%22kantor%22&sensor=true&key=AIzaSyCYBoox_hqWpk5Fj4mUlqf7nXMX71UWKoo");
    }
}
