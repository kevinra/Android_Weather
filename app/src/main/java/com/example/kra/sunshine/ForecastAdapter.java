package com.example.kra.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
  /**
   * Cache of the children views for a forecast list item.
   */
  private class ViewHolder
  {
    public final ImageView iconView;
    public final TextView dateView;
    public final TextView weatherDescView;
    public final TextView highTempView;
    public final TextView lowTempView;

    public ViewHolder(View view)
    {
      iconView = (ImageView) view.findViewById(R.id.list_item_icon);
      dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
      weatherDescView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
      highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
      lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
    }
  }

  // We have more than one view type; one for today's
  // the other for rest of the days.
  private static final int VIEW_TYPE_TODAY = 0;
  private static final int VIEW_TYPE_FUTURE_DAY = 1;
  private static final int VIEW_TYPE_COUNT = 2;

  public ForecastAdapter(Context context, Cursor c, int flags)
  {
    super(context, c, flags);
  }

  @Override
  public int getItemViewType(int position)
  {
    return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
  }

  @Override
  public int getViewTypeCount()
  {
    return VIEW_TYPE_COUNT;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent)
  {
    // Choose the layout type
    int viewType = getItemViewType(cursor.getPosition());
    int layoutId;

    if (viewType == VIEW_TYPE_TODAY)
    {
      layoutId = R.layout.list_item_forecast_today;
    }
    else
    {
      layoutId = R.layout.list_item_forecast;
    }

    View v = LayoutInflater.from(context)
            .inflate(layoutId, parent, false);
    ViewHolder vh = new ViewHolder(v);
    v.setTag(vh);
    return v;
  }

  // This is where we fill-in the views with the contents of the cursor.
  @Override
  public void bindView(View view, Context context, Cursor cursor)
  {
    ViewHolder vh = (ViewHolder) view.getTag();

    long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
    String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
    double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
    double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);

    // Read user preference for metric or imperial temperature units
    boolean isMetric = Utility.isMetric(context);

    // Use placeholder image for now
    vh.iconView.setImageResource(R.drawable.ic_launcher);
    vh.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
    vh.weatherDescView.setText(weatherDesc);
    vh.highTempView.setText(Utility
            .formatTemperature(context, high, isMetric));
    vh.lowTempView.setText(Utility
            .formatTemperature(context, low, isMetric));
  }
}
