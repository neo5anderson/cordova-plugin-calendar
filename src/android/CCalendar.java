package neo.droid.cordova.calendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.util.Log;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by neo on 30/01/2018.
 */

public class CCalendar extends CordovaPlugin {

  private static final String TAG = "cordova.calendar";

  private static final String kAddAction = "add";
  private static final String kDefaultCalenderName = "协同办公";
  private static final String kDefaultAccount = "isd@jnaw.top";
  private static final String kDefaultAccountName = "智慧街办";
  private static final String kDefaultAccountType = "com.android.exchange";

  private static final String kCalendarURL = "content://com.android.calendar/calendars";
  private static final String kCalendarEventURL = "content://com.android.calendar/events";
  private static final String kCalendarReminderURL = "content://com.android.calendar/reminders";
  private static final String kCalendarTitleKey = "title";
  private static final String kCalendarDescKey = "description";
  private static final String kCalendarIdKey = "calendar_id";
  private static final String kCalendarTimeZoneValue = "Asia/Shanghai";

  public CCalendar() {
  }

  private static JSONArray dumpCursor(Cursor cursor) {
    JSONArray result = new JSONArray();

    if (null != cursor) {
      int count = cursor.getColumnCount();
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        JSONObject object = new JSONObject();

        for (int i = 0; i < count; ++i) {
          try {
            object.put(cursor.getColumnName(i), cursor.getString(i));
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }

        result.put(object);
      }
    }

    return result;
  }

  private static long checkCalendarAccount(Context context) {
    long result = -1;

    Cursor user = context.getContentResolver()
        .query(Uri.parse(kCalendarURL), null, null, null, null);
    try {
      if (null != user) {
        int count = user.getCount();

        if (count > 0) {
          for (user.moveToFirst(); !user.isAfterLast(); user.moveToNext()) {
            if (-1 == result) {
              // [Neo] .TODO username
              result = user
                  .getInt(user.getColumnIndex(Calendars._ID));
            }
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != user) {
        user.close();
      }
    }

    return result;
  }

  public static long addCalendarAccount(Context context, String calender,
      String account, String type, String name) {
    ContentValues values = new ContentValues();
    values.put(Calendars.NAME, calender);
    values.put(Calendars.ACCOUNT_NAME, account);
    values.put(Calendars.ACCOUNT_TYPE, type);
    values.put(Calendars.CALENDAR_DISPLAY_NAME, name);
    values.put(Calendars.OWNER_ACCOUNT, account);

    values.put(Calendars.VISIBLE, 1);
    values.put(Calendars.CALENDAR_COLOR, Color.BLUE);
    values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
    values.put(Calendars.SYNC_EVENTS, 1);
    values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
    values.put(Calendars.CAN_ORGANIZER_RESPOND, 0);

    Uri calendarUri = Uri.parse(kCalendarURL).buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
        .appendQueryParameter(Calendars.ACCOUNT_TYPE, type).build();

    Uri insert = context.getContentResolver().insert(calendarUri, values);
    return (null != insert) ? ContentUris.parseId(insert) : -1;
  }

  public static boolean addCalendarEvent(Context context, String title,
      String desc, long ts, int prior) {
    boolean result = false;

    long id = checkCalendarAccount(context);
    if (-1 == id) {
      return result;
    }

    ContentValues values = new ContentValues();
    values.put(kCalendarTitleKey, title);
    values.put(kCalendarDescKey, desc);
    values.put(kCalendarIdKey, id);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(ts);
    calendar.set(Calendar.SECOND, 0);
    values.put(Events.DTSTART, calendar.getTime().getTime());

    calendar.add(Calendar.HOUR, 1);
    values.put(Events.DTEND, calendar.getTime().getTime());

    calendar.add(Calendar.DATE, 1);
    values.put(Events.LAST_DATE, calendar.getTime().getTime());

    values.put(Events.HAS_ALARM, 1);
    values.put(Events.EVENT_TIMEZONE, kCalendarTimeZoneValue);

    Uri insert = context.getContentResolver()
        .insert(Uri.parse(kCalendarEventURL), values);
    if (null != insert) {
      ContentValues v = new ContentValues();
      v.put(Reminders.MINUTES, prior);
      v.put(Reminders.EVENT_ID, ContentUris.parseId(insert));
      v.put(Reminders.METHOD, Reminders.METHOD_ALERT);

      Uri i = context.getContentResolver()
          .insert(Uri.parse(kCalendarReminderURL), v);
      if (null != i) {
        result = true;
      }
    }

    return result;
  }

  public static boolean rmCalendarEvent(Context context, String title) {
    boolean result = false;

    Cursor cursor = context.getContentResolver()
        .query(Uri.parse(kCalendarEventURL), null, null, null, null);

    try {
      if (null != cursor) {
        JSONArray array = dumpCursor(cursor);
        int length = array.length();
        for (int i = 0; i < length; ++i) {
          Log.w(TAG, "dump event: " + array.getJSONObject(i).toString());
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
          String t = cursor.getString(cursor.getColumnIndex("title"));

          if (!TextUtils.isEmpty(title) && title.equals(t)) {
            int id = cursor.getInt(cursor.getColumnIndex(Calendars._ID));
            Uri delete = ContentUris
                .withAppendedId(Uri.parse(kCalendarEventURL), id);
            int rows = context.getContentResolver().delete(delete, null, null);
            if (rows > 0) {
              result = true;
            }
          }
        }
      }

    } catch (Exception e) {

    } finally {
      if (null != cursor) {
        cursor.close();
      }
    }

    return result;
  }

  /**
   * Executes the request and returns PluginResult.
   *
   * @param action The action to execute.
   * @param args JSONArry of arguments for the plugin.
   * @param callbackContext The callback id used when calling back into
   * JavaScript.
   * @return True if the action was valid, false if not.
   */
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) {
    int level = 0;
    boolean result = false;

    if (kAddAction.equals(action)) {
      level++;

      if (null != args) {
        int length = args.length();

        if (4 == length) {
          level++;

          try {
            final String title = args.getString(0);
            final String desc = args.getString(1);
            final long date = args.getLong(2);
            final int prior = args.getInt(3);

            long id = checkCalendarAccount(cordova.getContext());
            if (-1 == id) {
              id = addCalendarAccount(cordova.getContext(),
                  kDefaultCalenderName,
                  kDefaultAccount, kDefaultAccountType, kDefaultAccountName);
            }

            if (id > 0) {
              result = addCalendarEvent(cordova.getContext(), title, desc, date,
                  prior);
            } else {
              level++;
            }

          } catch (JSONException e) {
            level++;
            e.printStackTrace();
          }

        }
      }

    }

    if (result) {
      callbackContext.success();
    } else {
      callbackContext.error(level);
    }

    return result;
  }

}
