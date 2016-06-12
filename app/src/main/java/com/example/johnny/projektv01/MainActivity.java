package com.example.johnny.projektv01;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends AppCompatActivity {

    Spinner currencyListSpinnerFrom;
    Spinner currencyListSpinnerTo;
    ArrayList<String> currencyNames = new ArrayList<>();
    ArrayList<String> currencyCourse = new ArrayList<>();
    ArrayList<String> currencyCode = new ArrayList<>();
    String courseFrom, courseTo, courseToCode;
    String courseFromCode = null;
    private Button b;
    private LocationManager locationManager;
    private LocationListener listener;
    String provider;

    public void goToCantors(View view){
        //Log.i("intent", "go yo list cantors");
        Intent i = new Intent(getApplicationContext(),KantoryKRK.class);
        startActivity(i);
    }

    public void calculateCurrency(View view){
        TextView resultLabel = (TextView) findViewById(R.id.resultLabel);
        EditText moneyAmountField = (EditText) findViewById(R.id.moneyAmount);

        Double moneyAmount = Double.parseDouble(String.valueOf(moneyAmountField.getText()));
        Double courseFromDouble = Double.parseDouble(courseFrom);
        Double exchangeAmount =  (courseFromDouble * moneyAmount) / Double.parseDouble(courseTo);

        resultLabel.setText(moneyAmount +" "+ courseFromCode + " = " + String.format("%.2f",exchangeAmount) +" " +courseToCode);
    }
    public class DownloadCurrencyCode extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                URLConnection conn = url.openConnection();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());

                NodeList nodes = doc.getElementsByTagName("pozycja");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    NodeList title = element.getElementsByTagName("kod_waluty");
                    Element line = (Element) title.item(0);
                    currencyCode.add(line.getTextContent());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return currencyCode;
        }
    }
    public class DownloadList extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                URLConnection conn = url.openConnection();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());

                NodeList nodes = doc.getElementsByTagName("pozycja");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    NodeList title = element.getElementsByTagName("nazwa_waluty");
                    Element line = (Element) title.item(0);
                    currencyNames.add(line.getTextContent());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return currencyNames;
        }
    }
    public class DownloadCourseList extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                URLConnection conn = url.openConnection();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());

                NodeList nodes = doc.getElementsByTagName("pozycja");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    NodeList title = element.getElementsByTagName("kurs_sredni");
                    Element line = (Element) title.item(0);
                    currencyCourse.add(line.getTextContent());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return currencyCourse;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadList taskNames = new DownloadList();
        DownloadCourseList taskCourse = new DownloadCourseList();
        DownloadCurrencyCode taskCode = new DownloadCurrencyCode();

        ArrayList<String> courses = new ArrayList<>();
        ArrayList<String> codes = new ArrayList<>();
        try {
            ArrayList<String> names = taskNames.execute("http://www.nbp.pl/kursy/xml/LastA.xml").get();
            names.add(0,"Polski złoty");
            for(int i=0; i < names.size(); i++) {
                names.set(i, names.get(i).toUpperCase());
            }
            currencyListSpinnerFrom = (Spinner) findViewById(R.id.currencyListSpinnerFrom);
            currencyListSpinnerTo = (Spinner) findViewById(R.id.currencyListSpinnerTo);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names);
            currencyListSpinnerFrom.setAdapter(arrayAdapter);
            currencyListSpinnerTo.setAdapter(arrayAdapter);

            courses = taskCourse.execute("http://www.nbp.pl/kursy/xml/LastA.xml").get();
            courses.add(0,"1");
            for(int i=0; i < courses.size(); i++) {
                String value = courses.get(i);
                value = value.replace(',','.');
                courses.set(i,value);
            }
            codes = taskCode.execute("http://www.nbp.pl/kursy/xml/LastA.xml").get();
            codes.add(0,"PLN");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        final ArrayList<String> finalCourses = courses;
        final ArrayList<String> finalCodes = codes;
        currencyListSpinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseFrom = String.valueOf(finalCourses.get(position));
                courseFromCode = String.valueOf(finalCodes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        currencyListSpinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseTo = String.valueOf(finalCourses.get(position));
                courseToCode = String.valueOf(finalCodes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //location stuff


        b = (Button) findViewById(R.id.findCantorsBtn);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=kantor&latlng="+location.getLatitude()+","+location.getLongitude()+"&sll="+location.getLatitude()+","+location.getLongitude()));
                //startActivity(intent);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    Intent i = new Intent(getApplicationContext(),NearestCantors.class);
                    i.putExtra("LAT",String.valueOf(location.getLatitude()));
                    i.putExtra("LNG",String.valueOf(location.getLongitude()));
                    Log.i("lat/lng",String.valueOf(location.getLatitude()) +"/"+String.valueOf(location.getLongitude()));
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "Nie zlokalizowano :(", Toast.LENGTH_LONG).show();
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                                    ,10);
                        }
                        return;
                    }
                    locationManager.requestLocationUpdates(provider,0,0,listener);
                }
            }
        });

        find_cantor_btn_action();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                find_cantor_btn_action();
                break;
            default:
                break;
        }
    }

    void find_cantor_btn_action(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        locationManager.requestLocationUpdates(provider,0,0,listener);
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        //Log.i("resume", "works");
        locationManager.requestLocationUpdates(provider, 50000, 0, listener);
    }
}
