package com.example.android.transittrackerapp;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class DataBusParser {
    public String vehicleid="";
    @RequiresApi(api = Build.VERSION_CODES.O)
    HashMap<LatLng,ZonedDateTime> parse(JSONObject jObject, String sName){

        JSONArray vehicleActivity;
        String LineRef="";
        ArrayList<LatLng> busPosition=new ArrayList<>();
        String arrivalTime;
        HashMap<String,HashMap<LatLng,ZonedDateTime>> fin=new HashMap<>();
        HashMap<LatLng,ZonedDateTime> result=new HashMap<>();
        // new DecimalFormat("#.##").format(dblVar);
        DecimalFormat df5 = new DecimalFormat(".#####");

        try {
            vehicleActivity = jObject.getJSONObject("Siri")
                    .getJSONObject("ServiceDelivery")
                    .getJSONObject("VehicleMonitoringDelivery")
                    .getJSONArray("VehicleActivity");
            Log.d("Praveen","SNAme is --> "+sName);
            /** Traversing all Vehicle Activity */
            for(int i=0;i<vehicleActivity.length();i++){
                LineRef=((JSONObject)vehicleActivity.get(i))
                        .getJSONObject("MonitoredVehicleJourney")
                        .getString("LineRef");

                if(LineRef.equals(sName))
                {
                    String lat=((JSONObject)vehicleActivity.get(i))
                            .getJSONObject("MonitoredVehicleJourney")
                            .getJSONObject("VehicleLocation")
                            .getString("Latitude");
                    String lon=((JSONObject)vehicleActivity.get(i))
                            .getJSONObject("MonitoredVehicleJourney")
                            .getJSONObject("VehicleLocation")
                            .getString("Longitude");
                    arrivalTime= ((JSONObject) vehicleActivity.get(i))
                            .getJSONObject("MonitoredVehicleJourney")
                            .getJSONObject("MonitoredCall")
                            .getString("ExpectedArrivalTime");
                    vehicleid = ((JSONObject) vehicleActivity.get(i))
                            .getJSONObject("MonitoredVehicleJourney")
                            .getString("VehicleRef");

                    Double latitude=Double.parseDouble(lat);
                    Double longitude=Double.parseDouble(lon);

                    latitude = Double.parseDouble(df5.format(latitude));
                    longitude=Double.parseDouble(df5.format(longitude));
                    busPosition.add(new LatLng(latitude,longitude));
                    Log.d("Praveen","Bus Location -- > " +busPosition.toString());

                    Instant timestamp = Instant.parse(arrivalTime);
                    ZonedDateTime losAngelesTime=timestamp.atZone(ZoneId.of("America/Los_Angeles"));
                    Log.d("Praveen","Bus ETA -- > " +losAngelesTime.getHour()+":"+losAngelesTime.getMinute());
                    result.put((new LatLng(latitude,longitude)),losAngelesTime);
                    //Log.d("Praveen","Bus id -- > " +vehicleid);
                    //fin.put(vehicleid,result);
                }
            }
        }
        catch (JSONException e){
            Log.d("Praveen","Catched Exception in Bus Parser");
        }
        return result;
    }
}
