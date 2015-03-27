package com.example.kra.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import java.util.HashSet;

public class TestDb extends AndroidTestCase
{
  public static final String LOG_TAG = TestDb.class.getSimpleName();
  private ContentValues mCV_northPoleLocation = TestUtilities.createTestCV_location();

  // Since we want each test to start with a clean slate
  void deleteTheDatabase() {
    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
  }

  /*
      This function gets called before each test is executed to delete the database.  This makes
      sure that we always have a clean test.
   */
  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    deleteTheDatabase();
  }

  /*
    Students: Uncomment this test once you've written the code to create the Location
    table.  Note that you will have to have chosen the same column names that I did in
    my solution for this test to compile, so if you haven't yet done that, this is
    a good time to change your column names to match mine.
    Note that this only tests that the Location table has the correct columns, since we
    give you the code for the weather table.  This test does not look at the
   */
  public void testCreateDb() throws Throwable {
    // build a HashSet of all of the table names we wish to look for
    // Note that there will be another table in the DB that stores the
    // Android metadata (db version information)
    final HashSet<String> tableNameHashSet = new HashSet<>();
    tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
    tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
    assertEquals(true, db.isOpen());

    // have we created the tables we want?
    Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
    assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

    // verify that the tables have been created
    do {
        tableNameHashSet.remove(c.getString(0));
    } while( c.moveToNext() );

    // if this fails, it means that your database doesn't contain both the location entry
    // and weather entry tables
    assertTrue("Error: Your database was created without both the location entry and weather entry tables",
            tableNameHashSet.isEmpty());

    // now, do our tables contain the correct columns?
    c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
            null);

    assertTrue("Error: This means that we were unable to query the database for table information.",
            c.moveToFirst());

    // Build a HashSet of all of the column names we want to look for
    final HashSet<String> locationColumnHashSet = new HashSet<>();
    locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

    int columnNameIndex = c.getColumnIndex("name");
    do {
        String columnName = c.getString(columnNameIndex);
        locationColumnHashSet.remove(columnName);
    } while(c.moveToNext());

    // if this fails, it means that your database doesn't contain all of the required location
    // entry columns
    assertTrue("Error: The database doesn't contain all of the required location entry columns",
            locationColumnHashSet.isEmpty());
    c.close();
    db.close();
  }



  public void testLocationTable()
  {
    SQLiteDatabase db_location = TestUtilities.getSQLiteDB(mContext);
    long rowId = db_location.insert(WeatherContract.LocationEntry.TABLE_NAME, null, mCV_northPoleLocation);
    assertTrue("Error: Failure to insert North Pole Location Values", rowId != -1);

    final String queryStr_all = "SELECT * FROM " + WeatherContract.LocationEntry.TABLE_NAME;
    Cursor c = db_location.rawQuery(queryStr_all, null);

    TestUtilities.validateCursorNcontentValues(null, c, mCV_northPoleLocation);
    assertFalse( "Error: More than one record returned from location query", c.moveToNext() );

    c.close();
    db_location.close();
  }

  public void testWeatherTable()
  {
    SQLiteDatabase db_weather = TestUtilities.getSQLiteDB(mContext);
    long rowId = db_weather.insert(WeatherContract.LocationEntry.TABLE_NAME, null, mCV_northPoleLocation);
    assertTrue("Error: Failure to insert North Pole Location Values", rowId != -1);

    ContentValues cv_weatherSample = TestUtilities.createTestCV_weather(rowId);
    rowId = db_weather.insert(WeatherContract.WeatherEntry.TABLE_NAME , null, cv_weatherSample);
    assertTrue("Error: Failure to insert sample weather info", rowId != -1);

    final String queryStr_all = "SELECT * FROM " + WeatherContract.WeatherEntry.TABLE_NAME;
    Cursor c = db_weather.rawQuery(queryStr_all, null);

    TestUtilities.validateCursorNcontentValues(null, c, cv_weatherSample);
    assertFalse( "Error: More than one record returned from location query", c.moveToNext() );

    c.close();
    db_weather.close();
  }


}
