package com.group06.applications.uoclbsp_source;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class Map extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        SearchView mSearchView = (SearchView) findViewById(R.id.searchView);
//
//        List<SearchItem> suggestionsList = new ArrayList<>();
//        suggestionsList.add(new SearchItem("search1"));
//        suggestionsList.add(new SearchItem("search2"));
//        suggestionsList.add(new SearchItem("search3"));
//
//        RecyclerView.Adapter searchAdapter = new RecyclerView.Adapter() {
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                return null;
//            }
//
//            @Override
//            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//
//            }
//
//            @Override
//            public int getItemCount() {
//                return 0;
//            }
//        };
//        mSearchView.setAdapter(searchAdapter);
//
//        suggestionsList.add(new SearchItem("search1"));
//        suggestionsList.add(new SearchItem("search2"));
//        suggestionsList.add(new SearchItem("search3"));
//        searchAdapter.notifyDataSetChanged();

//        List<SearchFilter> filter = new ArrayList<>();
//        filter.add(new SearchFilter("Filter1", true));
//        filter.add(new SearchFilter("Filter2", true));
//        mSearchView.setFilters(filter);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
