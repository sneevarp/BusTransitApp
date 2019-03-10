package com.example.android.transittrackerapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.webkit.PermissionRequest;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.libraries.places.api.Places;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location source;
    private Polyline polyline;
    private String shortName;
    private AutoCompleteTextView s;
    private AutoCompleteTextView destination;
    private String src;
    public ArrayList<LatLng> polylinePoints=new ArrayList<>();
    public String dest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        else
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyAij4Dg-TF4K83I99Roxxwh8oqyg91lEzI");

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).
                        setMessage("These permissions are mandatory to get your location. You need to allow them.").
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                                }
                            }
                        }).setNegativeButton("Cancel", null).create().show();

                return;
            }
            else if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                finish();
            }
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("Praveen","Map ready");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                Log.d("Praveen","Permission not granted");
                return;
            }
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            Log.d("Praveen", "Location enabled");
            getDeviceLocation(mFusedLocationClient, mMap);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
               dest=place.getName();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Praveen", "An error occurred: " + status);
            }
        });
        AutocompleteSupportFragment autocompleteFragment1 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete1_fragment);

        // Specify the types of place data to return.
        autocompleteFragment1.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                src=place.getName();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Praveen", "An error occurred: " + status);
            }
        });

    }
    public void getDeviceLocation(final FusedLocationProviderClient mfusedLocationProviderClient, final GoogleMap mMap) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         *
         */
        Log.d("Praveen","Getting device location");
        try {
            Task locationResult = mfusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener( this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location mLastKnownLocation = (Location)task.getResult();
                        Log.d("Praveen",mLastKnownLocation.toString());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 15));
                        source=mLastKnownLocation;
                        //LatLng newLocation=new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());

                    } else {
                        Log.d("Map", "Current location is null. Using defaults.");
                        Log.e("Map", "Exception: %s", task.getException());

                    }
                }
            });
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void findtransit(View view) {
        mMap.clear();
        LinearLayout mainLayout;
        // Get your layout set up, this is just an example
        mainLayout = findViewById(R.id.myLinearLayout);
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
        }
        catch(Exception e)
        {
            //Do nothing
        }
        //EditText destination=findViewById(R.id.destination);

        //if(dest.equals(null)||dest.equals("")) {
       //     Toast.makeText(this, "Enter a valid Destination", Toast.LENGTH_SHORT).show();
        //}
//        dest=dest.replaceAll(" ","+");
        try {
            polyline.remove();
        }
        catch (Exception e){
            Log.d("Null","Exception Handled");
        }
        //Getting URL to the Google Directions API
        String lineurl = getUrl();
        Log.d("Map",lineurl);

        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(lineurl);

    }

    private String getbusUrl() {
        return "http://api.511.org/transit/VehicleMonitoring?api_key=e1f30215-f7a1-41a5-8563-c55d9e2145e7&agency=AC&format=json";
    }

    public String getUrl(){
        //if(!(s.getText().toString().equals("My Location"))) {
        //    src = s.getText().toString();
        //}
        String url="https://maps.googleapis.com/maps/api/directions/json?origin="+src+"&destination="+dest+"&alternatives=true&mode=transit&transit_mode=bus&key=AIzaSyAij4Dg-TF4K83I99Roxxwh8oqyg91lEzI";
        return url;
    }

    public void setSource(View view) {
        //src=source.getLatitude()+","+source.getLongitude();
        //s.setText("My Location");
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class FetchBusUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("FetchBus Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Praveen","Praser Task Executed");
            ParserBusTask parserBusTask = new ParserBusTask();

            //Invokes the thread for parsing the JSON data
           parserBusTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadBusUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("PraveenParser","Shortname in parser is-->"+parser.shortName);
                shortName=parser.shortName;
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                polylinePoints.addAll(points);
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        points.get(0), 15));

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                polyline=mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
            String busurl = getbusUrl();
            FetchBusUrl FetchBusUrl = new FetchBusUrl();
            FetchBusUrl.execute(busurl);
        }
    }
    private class ParserBusTask extends AsyncTask<String, Integer, HashMap<LatLng,ZonedDateTime>> {

        // Parsing the data in non-ui thread
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected HashMap<LatLng,ZonedDateTime> doInBackground(String... jsonData) {

            JSONObject jObject;
            ArrayList<LatLng> busLocation=new ArrayList<>();
            HashMap<LatLng, ZonedDateTime> result=new HashMap<>();

           // HashMap<String,HashMap<LatLng,ZonedDateTime>> fin=new HashMap<>();
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataBusParser busParser = new DataBusParser();
                Log.d("ParserTask", busParser.toString());

                // Starts parsing data
                result = busParser.parse(jObject,shortName);
                Log.d("ParserTask","Executing routes");

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return result;
        }
        // Executes in UI thread, after the parsing process
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(HashMap<LatLng,ZonedDateTime> fin) {
            HashMap<LatLng,ZonedDateTime> temp;
            try {

                  temp=fin;
                    for(LatLng key:temp.keySet()) {
                        Integer hour = (int) temp.get(key).getHour();
                        hour -= 14;
                        String h = hour.toString();
                        mMap.addMarker(new MarkerOptions()
                                .position(key)
                                .title(shortName)
                                .snippet( "ETA :" + h + " hr:" + temp.get(key).getMinute() + " mins"))
                                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.busx));
                    }

                LatLng currLocation=new LatLng(source.getLatitude(),source.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currLocation));
            }
            catch(Exception e){
                Toast.makeText(MapsActivity.this, "Bus Transit Not Available in this Route", Toast.LENGTH_SHORT).show();
                Log.d("Praveen","Null pointer exception in bus location");
            }
        }
    }
}
