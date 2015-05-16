package com.example.kra.sunshine;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends ActionBarActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    if (savedInstanceState == null)
    {
      // Create the detail fragment and add it to the
      // activity using fragment transaction
      Bundle args = new Bundle();
      args.putParcelable(DetailFragment.DETAIL_URI_KEY, getIntent().getData());

      DetailFragment df = new DetailFragment();
      df.setArguments(args);

      getSupportFragmentManager().beginTransaction()
              .add(R.id.weather_detail_container, df)
              .commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_detail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings)
    {
      Intent intent_detail = new Intent(this, SettingsActivity.class);
      startActivity(intent_detail);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
