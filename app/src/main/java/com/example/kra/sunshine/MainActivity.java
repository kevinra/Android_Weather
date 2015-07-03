package com.example.kra.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kra.sunshine.sync.SunshineSyncAdapter;


public class MainActivity
        extends ActionBarActivity
        implements ForecastFragment.Callback
{
  private final String LOG_TAG = MainActivity.class.getSimpleName();
  private final String TAG_DETAILFRAGMENT = "TAGDF";
  private boolean mIsTwoPane;
  private String mLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mLocation = Utility.getPreferredLocation(this);
    setContentView(R.layout.activity_main);

    if ( findViewById(R.id.weather_detail_container) != null)
    { // Detail container view will be present only in the large
      // screen layouts (res/layout-sw600dp). If this view is present
      // then the activity should be in two-pane mode.
      mIsTwoPane = true;

      // In two-pane mode, show the detail view in this
      // activity by adding or replacing detail fragment
      // using a fragment transaction.
      if (savedInstanceState == null)
      {  // If not null, system already has the fragment
          getSupportFragmentManager().beginTransaction()
                  .replace(R.id.weather_detail_container,
                          new DetailFragment(), TAG_DETAILFRAGMENT)
                  .commit();
      }
    }
    else
    {
      mIsTwoPane = false;
      getSupportActionBar().setElevation(0);
    }

    ForecastFragment ff = ((ForecastFragment)getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_forecast));
    ff.setShouldUseTodayLayout(!mIsTwoPane);
    SunshineSyncAdapter.initializeSyncAdapter(this);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      Intent intent_detail = new Intent(this, SettingsActivity.class);
      startActivity(intent_detail);
      return true;
    }
    if(id == R.id.action_map)
    {
      openPreferredLocationInMap();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

    @Override
    protected void onResume()
    {
      super.onResume();
      String pref_location = Utility.getPreferredLocation(this);
      if ( pref_location != null && ! pref_location.equals(mLocation) )
      {
        ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_forecast);
        if (ff != null)
        {
          ff.onLocationChanged();
        }
        if (mIsTwoPane)
        {
          DetailFragment df = (DetailFragment) getSupportFragmentManager()
                                                .findFragmentByTag(TAG_DETAILFRAGMENT);
          if (df != null)
          {
            df.onLocationChanged(pref_location);
          }
        }
          mLocation = pref_location;
      }
    }

  private void openPreferredLocationInMap()
  {
    String location = Utility.getPreferredLocation(this);

    // Using the URI scheme for showing a location found on a map.  This super-handy
    // intent can is detailed in the "Common Intents" page of Android's developer site:
    // http://developer.android.com/guide/components/intents-common.html#Maps
    Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
            .appendQueryParameter("q", location)
            .build();

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(geoLocation);

    if (intent.resolveActivity(getPackageManager()) != null)
    {
      startActivity(intent);
    }
    else
    {
      Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
    }
  }

  public boolean getIsTwoPane()
  {
    return mIsTwoPane;
  }

  @Override
  public void fragCallback_onItemSelected(Uri contentUri)
  {
    if (mIsTwoPane)
    {
      // In two-pane mode, show the detail view in this activity
      // by adding or replacing the detail fragment using a
      // fragment transaction
      Bundle args = new Bundle();
      args.putParcelable(DetailFragment.DETAIL_URI_KEY, contentUri);

      DetailFragment df = new DetailFragment();
      df.setArguments(args);

      getSupportFragmentManager().beginTransaction()
              .replace(R.id.weather_detail_container, df, TAG_DETAILFRAGMENT)
              .commit();
    }
    else
    {
      Intent intent = new Intent(this, DetailActivity.class)
                          .setData(contentUri);
      startActivity(intent);
    }
  }
}
