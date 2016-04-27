package com.example.sumit.mobile_lab_0;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Marker um;
    static double lattitude;
    static double longitude;
    private LocationManager locManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }


    @Override
    //Function for displaying the current location
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;


        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Code for giving permissons
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        Location lastLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         lattitude = lastLocation.getLatitude();
        longitude = lastLocation.getLongitude();

        LatLng lastLatLng = new LatLng(lattitude, longitude);
        if(um!=null) {um.remove();}
        um = myMap.addMarker(new MarkerOptions()
                .position(lastLatLng)
                .title("You are here")
                .snippet("Your last recorded location"));

        myMap.animateCamera(CameraUpdateFactory.newLatLng(lastLatLng), 3000, null);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastLatLng)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(18)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));}

//Function for fetching near by places
    public void show_near_by(View V){
        EditText ed;
        ed=(EditText)findViewById(R.id.searchtext);
        String input=ed.getText().toString();
        StringBuilder sb =new StringBuilder( "https://maps.googleapis.com/maps/api/place/nearbysearch/json?") ;
                sb.append("location="+lattitude+","+longitude);
        sb.append("&radius=1000&sensor=true");
        sb.append("&types="+input);
                sb.append("&key=AIzaSyB-vjw2UBI9cltxHpmj7T9bXMjbTFu1rlM");

        NearByPlace nearplace = new NearByPlace();
        nearplace.execute(sb.toString());
    }

private class NearByPlace extends AsyncTask<String, Integer, String> {

    String newdata = null;

    // Invoked by execute() method of this object
    @Override
    protected String doInBackground(String... url) {
        try {
            newdata = takeUrl(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return newdata;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(String res) {
        TaskParser pt = new TaskParser();

        // Start parsing the Google places in JSON format
        // Invokes the "doInBackground()" method of the class ParserTask
        pt.execute(res);
    }
}
    //For setting the Http Connection and giving the input
    private String takeUrl(String stringUrl) throws IOException {
        String datanew = "";
        InputStream inputStream = null;
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(stringUrl);

            // Creating an http connection to communicate with url
            urlConn = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConn.connect();

            // Reading data from url
            inputStream = urlConn.getInputStream();

            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringbuf = new StringBuffer();

            String newline = "";
            while ((newline = breader.readLine()) != null) {
                stringbuf.append(newline);
            }

            datanew = stringbuf.toString();

            breader.close();

        } catch (Exception e) {
            Log.d("Exception while", e.toString());
        }
        return datanew;
    }

    //Class for parsing the JSON object
 class TaskParser extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

    JSONObject jsonObject;

    // Invoked by execute() method of this object
    @Override
    //Method for getting all the places and storing it in a list
    protected List<HashMap<String, String>> doInBackground(String... jData) {

        List<HashMap<String, String>> pl = null;
        Pl_JSON plJson = new Pl_JSON();

        try {
            jsonObject = new JSONObject(jData[0]);

            pl = plJson.parse(jsonObject);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return pl;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {

        Log.d("Map", "list size: " + list.size());
        // Clears all the existing markers;
        myMap.clear();

        for (int k = 0; k < list.size(); k++) {

            // Creating a marker
            MarkerOptions marker = new MarkerOptions();

            // Getting a place from the places list
            HashMap<String, String> hashmapPlace = list.get(k);


            // Getting latitude of the place
            double lattitude2 = Double.parseDouble(hashmapPlace.get("lat"));

            // Getting longitude of the place
            double longitude2 = Double.parseDouble(hashmapPlace.get("lng"));

            // Getting name
            String place_name = hashmapPlace.get("place_name");

            Log.d("Map", "place: " + place_name);

            // Getting vicinity
            String vic = hashmapPlace.get("vicinity");

            LatLng lattitude_Longitude = new LatLng(lattitude2, longitude2);

            // Setting the position for the marker
            marker.position(lattitude_Longitude);

            marker.title(place_name + " : " + vic);

            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

            // Placing a marker on the touched position
            Marker m = myMap.addMarker(marker);

        }
    }
}
 class Pl_JSON {

    //      Receives a JSONObject and returns a list

    public List<HashMap<String, String>> parse(JSONObject jObject) {

        JSONArray jsonPlace = null;
        try {
            // Retrieves all the elements in the 'jsonPlace' array
            jsonPlace = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Invoking get_Places with the array of json object where each json object represent a place

        return get_place(jsonPlace);
    }
//Putting all the places in a list
    private List<HashMap<String, String>> get_place(JSONArray jsonPlaces) {
        int pCount = jsonPlaces.length();
        List<HashMap<String, String>> plList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> pl = null;

        // Taking each place, parses and adds to list object
        for (int j = 0; j < pCount; j++) {
            try {
                /** Call getPlace with place JSON object to parse the place */
                pl = getPl((JSONObject) jsonPlaces.get(j));
                plList.add(pl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return plList;
    }

   // Parsing the Place JSON object

    private HashMap<String, String> getPl(JSONObject jsonPlace) {

        HashMap<String, String> pl = new HashMap<String, String>();
        String plName = "-NA-";
        String vic = "-NA-";
        String lat = "";
        String lng = "";
        String ref = "";

        try {
            // Extracting Place name,
            if (!jsonPlace.isNull("name")) {
                plName = jsonPlace.getString("name");
            }

            // Extracting Place Vicinity,
            if (!jsonPlace.isNull("vicinity")) {
                vic = jsonPlace.getString("vicinity");
            }

            lat = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            lng = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            ref = jsonPlace.getString("reference");

            pl.put("place_name", plName);
            pl.put("vicinity", vic);
            pl.put("lat", lat);
            pl.put("lng", lng);
            pl.put("reference", ref);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pl;
    }
}}
