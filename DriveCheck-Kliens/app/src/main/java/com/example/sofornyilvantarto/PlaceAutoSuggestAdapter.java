package com.example.sofornyilvantarto;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaceAutoSuggestAdapter extends ArrayAdapter<OsmPlace> implements Filterable {
    private List<OsmPlace> resultList = new ArrayList<>();

    public PlaceAutoSuggestAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public OsmPlace getItem(int position) {
        return resultList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 2) {
                    // Itt hívjuk meg a külső szervert
                    List<OsmPlace> results = findPlaces(constraint.toString());
                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<OsmPlace>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private List<OsmPlace> findPlaces(String query) {
        List<OsmPlace> places = new ArrayList<>();
        try {
            // Szóközök és ékezetek kódolása az URL-hez
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // Ingyenes Nominatim API (OpenStreetMap)
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=5";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    // FONTOS: Az OSM kéri a User-Agent fejlécet, különben letilt!
                    .header("User-Agent", "DriveCheckApp/1.0")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JSONArray jsonArray = new JSONArray(jsonData);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String name = obj.getString("display_name");
                    String lat = obj.getString("lat");
                    String lon = obj.getString("lon");

                    places.add(new OsmPlace(name, lat, lon));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return places;
    }
}