package com.example.kra.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

  private ArrayAdapter<String> mForecastAdapter;
  public ForecastFragment() {}

  @Override
  public void onStart()
  {
    super.onStart();
    updateWeather();
  }


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // To handle menu events
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    inflater.inflate(R.menu.forecast_fragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == R.id.action_refresh)
    {
      updateWeather();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void updateWeather()
  {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String location = sharedPref.getString( getString(R.string.pref_key_location), getString(R.string.pref_value_default_location) );
    new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mForecastAdapter =
            new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());
    ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
    lv.setAdapter(mForecastAdapter);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        String forecastStr_oneDay = mForecastAdapter.getItem(position);
        //  Toast.makeText(getActivity(), forecast_oneDay, Toast.LENGTH_SHORT).show();
        Intent intent_detail = new Intent(getActivity(), DetailActivity.class);
        intent_detail.putExtra(Intent.EXTRA_TEXT, forecastStr_oneDay);
        startActivity(intent_detail);
      }
    });

    return rootView;
  }
}
