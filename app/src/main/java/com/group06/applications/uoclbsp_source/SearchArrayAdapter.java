package com.group06.applications.uoclbsp_source;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchArrayAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> searchProperties;

    public SearchArrayAdapter(Context context, int resource, ArrayList<JSONObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.searchProperties = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        JSONObject property = searchProperties.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.uocmap_list_item, null);

        TextView mainSearch = (TextView) view.findViewById(R.id.main_search);
        TextView altSearch = (TextView) view.findViewById(R.id.alt_search);
        try {
            mainSearch.setText(property.getString("name"));
            altSearch.setText(property.getString("alt"));
        }catch (Exception e){
            e.printStackTrace();
        }


        return view;
    }
}
