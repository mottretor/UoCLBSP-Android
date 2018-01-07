package com.group06.applications.uoclbsp_source;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public Polyline mPolyline;
    View mapView;
    GoogleMap mMap;
    MaterialSearchView searchView;
    ListView lstView;
    ArrayList<JSONObject> lstSource = new ArrayList<JSONObject>();
    ArrayList<JSONObject> lstFound;
    List<double[]> lstFoundLocation;
    ArrayList<ArrayList<LatLng>> myPolygons = null;
    ArrayList<Integer> myIndex = null;
    Marker marker;
    LatLng currLatLng;
    Polyline polyline;
    Boolean searching = true;
    boolean done = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    public Button getDirectionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        findMyLocation();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("UOC Maps");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        lstView = (ListView) findViewById(R.id.lstView);
        getDirectionsButton = (Button) findViewById(R.id.get_directions_button);
        getDirectionsButton.setVisibility(View.INVISIBLE);


        SearchArrayAdapter adapter = new SearchArrayAdapter(this, R.layout.uocmap_list_item, lstSource);
        lstView.setAdapter(adapter);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                //If closed Search View , lstView will return default
                lstView = (ListView) findViewById(R.id.lstView);
                SearchArrayAdapter adapter = new SearchArrayAdapter(MapsActivity.this, R.layout.uocmap_list_item, lstSource);
                lstView.setAdapter(adapter);

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;

                if (newText != null && !newText.isEmpty()) {

                    try {
                        new GetSearchResults().execute(new Object[]{newText, MapsActivity.this, 1});
                        lstView = (ListView) findViewById(R.id.lstView);
                        lstView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    //if search text is null
                    //return default
                    lstView = (ListView) findViewById(R.id.lstView);
                    SearchArrayAdapter adapter = new SearchArrayAdapter(MapsActivity.this, R.layout.uocmap_list_item, lstSource);
                    lstView.setAdapter(adapter);
                }
                return true;
            }

        });

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = "";
                try {
                    title = lstFound.get(position).getString("name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searching = false;
                searchView.setQuery(title, true);
//                getSupportActionBar().setTitle(lstFound.get(position));
                double[] loc = lstFoundLocation.get(position);
                if (marker != null) {
                    if (mPolyline!=null){
                        mPolyline.remove();
                    }
                    marker.remove();
                }

                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(loc[0], loc[1]))
                        .title(title));
                marker.showInfoWindow();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc[0], loc[1]),16));
                lstView = (ListView) findViewById(R.id.lstView);
                lstView.setVisibility(View.INVISIBLE);
                getDirectionsButton.setVisibility(View.VISIBLE);


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Move camera to the default location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(6.902631, 76.860611), 16));

        //Check permission and get current location
        findMyLocation();

        //set move camera to current location
        setToCurrentLocation();
        new GetSearchResults().execute(new Object[]{"", MapsActivity.this, 2});
        mMap.setOnCameraIdleListener(this);
        mMap.setPadding(0,150,0,0);
    }

    private void setToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    }
                }
            });
        }
    }

    private void findMyLocation() {
        if (mMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastKnownLocation = location;
                            //System.out.println(location);
                        }
                        //System.out.println("No location");


                    }
                });
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);
            }

        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }



    public void getDirections(View view) {

        findMyLocation();
        System.out.println("I'm alone");
        System.out.println(mLastKnownLocation);
        int sourcePoly = 0;
        int destinationPoly = 0;
        LatLng sourceLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        LatLng destinationLatLng = marker.getPosition();
        System.out.println(myPolygons.size());
        for (int x = 0; x < myPolygons.size(); x++) {
            if (sourcePoly == 0 | destinationPoly == 0) {
                if (PolyUtil.containsLocation(sourceLatLng, myPolygons.get(x), true)) {
                    sourcePoly = myIndex.get(x);
                }
                if (PolyUtil.containsLocation(destinationLatLng, myPolygons.get(x), true)) {
                    destinationPoly = myIndex.get(x);
                }
            }
        }
        System.out.println(sourceLatLng.toString());
        System.out.println(destinationLatLng.toString());
        Object[] objects = new Object[]{sourceLatLng, sourcePoly, destinationLatLng, destinationPoly};
        new GetSearchResults().execute(new Object[]{objects, MapsActivity.this, 3});
    }

    public int getLineWidth() {
        float zoom = mMap.getCameraPosition().zoom;
        float a = (zoom - 12 > 0 ? (zoom - 12) * (zoom - 12) : 1);
        return Math.round(a);
    }

    @Override
    public void onCameraIdle() {
//        System.out.println("Called");
        if (mPolyline != null) {
            //mPolyline.setWidth(this.getLineWidth());
        }
    }

}

class GetSearchResults extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] params) {

        try {
            InetAddress address = InetAddress.getByName("ec2-52-72-156-17.compute-1.amazonaws.com");
            Socket s1 = null;
            String line = null;
            BufferedReader br = null;
            BufferedReader is = null;
            PrintWriter os = null;

            s1 = new Socket(address, 1978); // You can use static final constant PORT_NUM
            br = new BufferedReader(new InputStreamReader(System.in));
            is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os = new PrintWriter(s1.getOutputStream());

            String response = null;
            JSONObject jsonObject1 = new JSONObject();
            if ((int) params[2] == 1) {
                jsonObject1.put("type", "searchRequest");
                jsonObject1.put("input", String.valueOf(params[0]));
                jsonObject1.put("role", "registered");
            } else if ((int) params[2] == 2) {
                jsonObject1.put("type", "polyRequest");
            } else {
                Object[] objects = (Object[]) params[0];
                jsonObject1.put("type", "getPath");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitudes", ((LatLng) objects[0]).latitude);
                jsonObject.put("longitudes", ((LatLng) objects[0]).longitude);
                jsonObject.put("inside", (int) objects[1]);
                jsonObject1.put("source", jsonObject);
                jsonObject = new JSONObject();
                jsonObject.put("latitudes", ((LatLng) objects[2]).latitude);
                jsonObject.put("longitudes", ((LatLng) objects[2]).longitude);
                jsonObject.put("inside", (int) objects[3]);
                jsonObject1.put("destination", jsonObject);
            }
            line = jsonObject1.toString();

            os.println(line);
            os.flush();
            response = is.readLine();
            System.out.println("Server Response : " + response);

            JSONObject jsonObject = new JSONObject(response);
            if ((int) params[2] == 1) {
                JSONArray jsonArray = jsonObject.getJSONArray("Results");
                MapsActivity mapsActivity = (MapsActivity) params[1];
                mapsActivity.lstFound = new ArrayList<JSONObject>();
                mapsActivity.lstFoundLocation = new ArrayList<double[]>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    mapsActivity.lstFound.add(jsonArray.getJSONObject(i));
                    mapsActivity.lstFoundLocation.add(new double[]{jsonArray.getJSONObject(i).getDouble("lat"), jsonArray.getJSONObject(i).getDouble("lng")});
                }
                SearchArrayAdapter adapter = new SearchArrayAdapter(mapsActivity, R.layout.uocmap_list_item, mapsActivity.lstFound);
                params[0] = adapter;
            } else if ((int) params[2] == 2) {
                JSONArray jsonArray = jsonObject.getJSONArray("polygons");
                params[0] = jsonArray;
            } else {
                JSONArray jsonArray = jsonObject.getJSONArray("steps");
                params[0] = jsonArray;
            }
            return params;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        try {
            Object[] params = (Object[]) o;
            MapsActivity mapsActivity = (MapsActivity) params[1];
            if ((int) params[2] == 1) {
                SearchArrayAdapter adapter = (SearchArrayAdapter) params[0];
                if (mapsActivity.searching) {
                    mapsActivity.lstView.setAdapter(adapter);
                }
            } else if ((int) params[2] == 2) {
                JSONArray jsonArray = (JSONArray) params[0];
                ArrayList<ArrayList<LatLng>> myPolygons = new ArrayList<ArrayList<LatLng>>();
                ArrayList<Integer> myIndex = new ArrayList<Integer>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    myIndex.add(jsonObject.getInt("id"));

                    JSONArray jsonArray1 = jsonObject.getJSONArray("vertexes");
                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray1.get(j);
                        points.add(new LatLng(jsonObject1.getDouble("lat"), jsonObject1.getDouble("lng")));
                    }
                    myPolygons.add(points);

                }

                mapsActivity.myPolygons = myPolygons;
                mapsActivity.myIndex = myIndex;
                mapsActivity.done = true;
            } else {
                JSONArray jsonArray = (JSONArray) params[0];
                ArrayList<LatLng> polylines = new ArrayList<LatLng>();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    LatLng myLatLng = new LatLng(jsonObject.getDouble("lat"), jsonObject.optDouble("lng"));
                    polylines.add(myLatLng);
                    if (i==0 | i==jsonArray.length()-1 | i%20==0){
                        builder.include(myLatLng);
                    }

                }
                if (mapsActivity.mPolyline!=null){
                    mapsActivity.mPolyline.remove();
                }
                mapsActivity.mPolyline = mapsActivity.mMap.addPolyline(new PolylineOptions().addAll(polylines).width(10).color(Color.BLUE));
                mapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),50));

                mapsActivity.getDirectionsButton.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


