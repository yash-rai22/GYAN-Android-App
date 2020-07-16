package com.example.kalyan.timetable;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.kalyan.timetable.Contract;
import com.example.kalyan.timetable.Helper;
import com.example.kalyan.timetable.R;
import com.example.kalyan.timetable.SettingsActivity;
import com.example.kalyan.timetable.Utility;
import com.example.kalyan.timetable.mute_receive;

import java.util.Date;

import static com.example.kalyan.timetable.MainActivity.alarmManager;
import static com.example.kalyan.timetable.MainActivity.getContext;



public class NotificationService extends IntentService {

    private static NotificationManager manager;
    static Cursor mCursor;
    static String tSQL[];
    AudioManager audioManager;

    public NotificationService() {
        super("NotificationService");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = this;
        Toast.makeText(getContext(),"Hello",Toast.LENGTH_SHORT).show();
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);



        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle("TimeTable");

        Calendar calendar = Calendar.getInstance();

        Date currentTime = calendar.getTime();

        String day = com.example.kalyan.timetable.Utility.Day((currentTime+"").split(" ")[0]);

        tSQL = context.getResources().getStringArray(R.array.TimeSQL);
        String selectQuery = "SELECT  * FROM " + Contract.Entry.TABLE_NAME + " WHERE "
                + Contract.Entry.COLUMN_DAY + " = " + "\""+day+"\"";

        mCursor = (new Helper(context)).getReadableDatabase().rawQuery(selectQuery, null);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(mCursor.moveToFirst() && prefs.getBoolean(SettingsActivity.SHOW_NOTIFICATION, false) ){
            String tempSt;
            int position = 0;
            do {
                tempSt = mCursor.getString(mCursor.getColumnIndex(tSQL[position]));
                if(tempSt != null && !tempSt.equals("") && !tempSt.equals("null") && !tempSt.contains("null")){
                    style.addLine(Utility.number(tSQL[position].split("to")[0])+" to "+
                            Utility.number(tSQL[position].split("to")[1])+" => " + tempSt);
                }
                position++;
            }while (position != 9);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher2)
                            .setTicker("TimeTable Notification")
                            .setContentTitle("Todays TimeTable")
                            .setContentText("You have these classes today :-")

                            .setAutoCancel(true)
                            .setStyle(style);

            manager.notify(100, builder.build());

            silentMode();

            if(Utility.daysLastClassTime(day) != 0){
                Calendar calendar1 = Calendar.getInstance();

                calendar1.set(Calendar.HOUR_OF_DAY,Utility.daysLastClassTime(day)+1);
                calendar1.set(Calendar.MINUTE,0);
                calendar1.set(Calendar.SECOND,currentTime.getSeconds());

                Intent intents = new Intent(context,mute_receive.class);



                alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);



            }
        }
    }

    private void silentMode(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }
}
