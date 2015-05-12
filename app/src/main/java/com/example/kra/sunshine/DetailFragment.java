package com.example.kra.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kra.sunshine.data.WeatherContract;
import com.example.kra.sunshine.data.WeatherContract.WeatherEntry;

public class DetailFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{

  private static final String LOG_TAG = DetailFragment.class.getSimpleName();
  private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
  private static final int LOADER_ID_DETAIL = 0;
  private ShareActionProvider mSAP;
  private String mForecastStr;

  private static final String[] DETAIL_COLUMNS =
          {
                  WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                  WeatherEntry.COLUMN_DATE,
                  WeatherEntry.COLUMN_SHORT_DESC,
                  WeatherEntry.COLUMN_MAX_TEMP,
                  WeatherEntry.COLUMN_MIN_TEMP,
                  WeatherEntry.COLUMN_HUMIDITY,
                  WeatherEntry.COLUMN_PRESSURE,
                  WeatherEntry.COLUMN_WIND_SPEED,
                  WeatherEntry.COLUMN_DEGREES,
                  WeatherEntry.COLUMN_WEATHER_ID,
                  // This works because the WeatherProvider returns location data joined with
                  // weather data, even though they're stored in two different tables.
                  WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
          };

  // These indices are tied to DETAIL_COLUMNS.
  // If DETAIL_COLUMNS changes, these must change.
  public static final int COL_WEATHER_ID = 0;
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  public static final int COL_WEATHER_HUMIDITY = 5;
  public static final int COL_WEATHER_PRESSURE = 6;
  public static final int COL_WEATHER_WIND_SPEED = 7;
  public static final int COL_WEATHER_DEGREES = 8;
  public static final int COL_WEATHER_CONDITION_ID = 9;

  private ImageView mIconView;
  private TextView mFriendlyDateView;
  private TextView mDateView;
  private TextView mDescriptionView;
  private TextView mHighTempView;
  private TextView mLowTempView;
  private TextView mHumidityView;
  private TextView mWindView;
  private TextView mPressureView;

  public DetailFragment()
  {
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
    mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
    mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
    mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
    mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
    mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
    mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
    mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
    mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
    mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    inflater.inflate(R.menu.detailfragment, menu);

    // Retrieve the share menu item
    MenuItem mi = menu.findItem(R.id.action_share);

    // Get the provider and hold onto it to set/change the share intent
    mSAP = (ShareActionProvider) MenuItemCompat.getActionProvider(mi);

    // If onLoadFinished happens before this, we can go ahead and
    // set the share intent now.
    if (mForecastStr != null)
    {
      mSAP.setShareIntent(createIntent_shareForecast());
    }
  }

  private Intent createIntent_shareForecast()
  {
    Intent intent_share = new Intent(Intent.ACTION_SEND);
    intent_share.setType("text/plain");
    //            intent_share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    intent_share.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
    return intent_share;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    getLoaderManager().initLoader(LOADER_ID_DETAIL, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  // LoaderCallbacks Implementation
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    Intent intent = getActivity().getIntent();
    if (intent == null)
    {
      return null;
    }

    // Now create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed
    return new CursorLoader(getActivity(),
                            intent.getData(),
                            DETAIL_COLUMNS,
                            null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cs)
  {
    if ( cs != null && cs.moveToFirst() )
    {
      Context context = getActivity();
      int weatherId = cs.getInt(COL_WEATHER_CONDITION_ID);
      long date = cs.getLong(COL_WEATHER_DATE);
      String weatherDesc = cs.getString(COL_WEATHER_DESC);
      double high = cs.getDouble(COL_WEATHER_MAX_TEMP);
      double low = cs.getDouble(COL_WEATHER_MIN_TEMP);
      float humidity = cs.getFloat(COL_WEATHER_HUMIDITY);
      float windSpeed = cs.getFloat(COL_WEATHER_WIND_SPEED);
      float windDir = cs.getFloat(COL_WEATHER_DEGREES);
      float pressure = cs.getFloat(COL_WEATHER_PRESSURE);

      String friendlyDateText = Utility.getDayName(context, date);
      String dateText = Utility.getFormattedMonthDay(context, date);
      boolean isMetric = Utility.isMetric(context);
      String highTempStr = Utility.formatTemperature(context, high, isMetric);
      String lowTempStr = Utility.formatTemperature(context, low, isMetric);

      mIconView.setImageResource(R.drawable.ic_launcher);
      mFriendlyDateView.setText(friendlyDateText);
      mDateView.setText(dateText);
      mDescriptionView.setText(weatherDesc);
      mHighTempView.setText(highTempStr);
      mLowTempView.setText(lowTempStr);
      mHumidityView.setText( context.getString(R.string.format_humidity, humidity) );
      mWindView.setText(Utility.getFormattedWind(context, windSpeed, windDir));
      mPressureView.setText(context.getString(R.string.format_pressure, pressure));

      // Still need this for share intent
      mForecastStr = String.format("%s - %s - %s/%s",
                                    dateText, weatherDesc,
                                    high, low);

      // If onCreateOptionsMenu has already happened
      // we need to update the share intent now.
      if (mSAP != null)
      {
        mSAP.setShareIntent(createIntent_shareForecast());
      }
    }
  }

  // Upon CursorLoader destruction
  @Override
  public void onLoaderReset(Loader<Cursor> loader) {}
}
