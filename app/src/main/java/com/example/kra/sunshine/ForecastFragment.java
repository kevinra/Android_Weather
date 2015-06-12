package com.example.kra.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import android.widget.AdapterView;
import android.util.Log;

import com.example.kra.sunshine.data.WeatherContract;
import com.example.kra.sunshine.service.SunshineService;

// Encapsulates fetching the forecast and displaying it as
// a {@link ListView} layout.
public class ForecastFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{
  private static final int LOADER_ID_FORECAST = 0;
  private static final String[] FORECAST_COLUMNS =
  {
    // In this case the id needs to be fully qualified with a table name, since
    // the content provider joins the location & weather tables in the background
    // (both have an _id column)
    // On the one hand, that's annoying.  On the other, you can search the weather table
    // using the location set by the user, which is only in the Location table.
    // So the convenience is worth it.
    WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
    WeatherContract.WeatherEntry.COLUMN_DATE,
    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    WeatherContract.LocationEntry.COLUMN_COORD_LAT,
    WeatherContract.LocationEntry.COLUMN_COORD_LONG
  };

  // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
  // must change.
  static final int COL_WEATHER_ID = 0;
  static final int COL_WEATHER_DATE = 1;
  static final int COL_WEATHER_DESC = 2;
  static final int COL_WEATHER_MAX_TEMP = 3;
  static final int COL_WEATHER_MIN_TEMP = 4;
  static final int COL_LOCATION_SETTING = 5;
  static final int COL_WEATHER_CONDITION_ID = 6;
  static final int COL_COORD_LAT = 7;
  static final int COL_COORD_LONG = 8;

  private ForecastAdapter mForecastAdapter;
  private ListView mListView;
  private int mPosition = ListView.INVALID_POSITION;
  private static final String SELECTED_POSITION_KEY = "POSITION";
  private boolean mShouldUseTodayLayout;

  public interface Callback
  {
    // DetailFragment callback for when an item has been selected.
    public void fragCallback_onItemSelected(Uri dateUri);
  }

  public ForecastFragment() {}

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

  // Since we read the location when we create the loader,
  // all we need to do is restart things.
  public void onLocationChanged()
  {
    updateWeather();
    getLoaderManager().restartLoader(LOADER_ID_FORECAST, null, this);
  }

  public void updateWeather()
  {
    Context ct = getActivity();
    String location = Utility.getPreferredLocation(ct);
    Intent alarmIntent = new Intent(ct, SunshineService.AlarmReceiver.class);
    alarmIntent.putExtra(SunshineService.LOCATION_KEY, location);

    // Wrap in a pending intent which only fires once.
    PendingIntent pi = PendingIntent.getBroadcast(ct, 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
    AlarmManager am = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);

    // Set the AlarmManager to wake up the system
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           final Bundle savedInstanceState)
  {
    mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
    mListView.setAdapter(mForecastAdapter);
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (c != null)
        {
          String locationSetting = Utility.getPreferredLocation(getActivity());
          ((Callback) getActivity()).fragCallback_onItemSelected(
                  WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                          locationSetting, c.getLong(COL_WEATHER_DATE)));
          mPosition = position;
        }
      }
    });
    // If there's instance state, mine it for useful information.
    // The end-goal here is that the user never knows that turning their device sideways
    // does crazy lifecycle related things.  It should feel like some stuff stretched out,
    // or magically appeared to take advantage of room, but data or place in the app was never
    // actually *lost*.
    if ( savedInstanceState != null && savedInstanceState.containsKey(SELECTED_POSITION_KEY) )
    {
      mPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
    }

    mForecastAdapter.setShouldUseTodayLayout(mShouldUseTodayLayout);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    getLoaderManager().initLoader(LOADER_ID_FORECAST, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  // When device rotates, currently selected list item needs to be saved.
  // When no item is selected, mPosition will be set to ListView.INVALID_POSITION
  // so check for that before storing
  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    if (mPosition != ListView.INVALID_POSITION)
    {
      outState.putInt(SELECTED_POSITION_KEY, mPosition);
    }
    super.onSaveInstanceState(outState);
  }

  // LoaderCallbacks Implementation

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
  {
    String locationSetting = Utility.getPreferredLocation(getActivity());
    Uri weatherForLocationUri = WeatherContract
            .WeatherEntry
            .buildWeatherLocationWithStartDate(
                    locationSetting,
                    System.currentTimeMillis()
            );

    Log.d(ForecastFragment.class.getSimpleName(), "URI:" + weatherForLocationUri.toString());

    // Sort order: Ascending, by date.
    String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
    return new CursorLoader( getActivity(), weatherForLocationUri,
                             FORECAST_COLUMNS,
                             null,
                             null,
                             sortOrder
                           );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor c)
  {
    mForecastAdapter.swapCursor(c);
    if (mPosition != ListView.INVALID_POSITION)
    {
      mListView.smoothScrollToPosition(mPosition);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    mForecastAdapter.swapCursor(null);
  }

  public void setShouldUseTodayLayout(boolean value)
  {
    mShouldUseTodayLayout = value;
    if (mForecastAdapter != null)
    {
      mForecastAdapter.setShouldUseTodayLayout(value);
    }
  }
}
