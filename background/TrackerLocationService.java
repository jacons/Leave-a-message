package project.leaveamessage.background;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import project.leaveamessage.R;
import project.leaveamessage.activities.MainActivity;
import project.leaveamessage.interfaces.CallBackInterface;
import project.leaveamessage.roomdb.Message;
import project.leaveamessage.roomdb.MessagesDatabase;

import static project.leaveamessage.utils.Utils.isConnected;

public class TrackerLocationService extends Service implements LocationListener, CallBackInterface {

    public static final int DELAY = 2000; // Time(s)  for update
    public static final int DISTANCE = 1; // Space(m) for update

    private LocationManager location;
    private Gson gson;
    private String userid;
    private MessagesDatabase db;

    @Override
    public void onCreate() {
        gson = new Gson();

        location = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER,DELAY,DISTANCE,this);

        //noinspection MissingPermission
        Location lastLocation = location.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastLocation!=null) {
            new NearestMessageTask(this,gson,userid)
                    .execute(lastLocation.getLatitude(),lastLocation.getLongitude());
        }

        db = MessagesDatabase.getDbInstance(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        userid= intent.getStringExtra("userid");

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "leave_message")
                .setSmallIcon(R.drawable.notifyicon)
                .setContentTitle("Leave a  message")
                .setContentText("Sto cercando messaggi...")
                .setColor(474);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel("leave_message", "leave_message", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(mChannel);
        startForeground(startId,notification.build());

        return super.onStartCommand(intent, flags, startId);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(location != null){
            //noinspection MissingPermission
            location.removeUpdates(this);
        }
    }

    public void onLocationChanged(Location location) {

        if(isConnected(getApplicationContext())) return;

        new NearestMessageTask(this,gson,userid)
                .execute(location.getLatitude(),location.getLongitude());

        Intent i = new Intent("location_update");
        i.putExtra("Longitude",location.getLongitude());
        i.putExtra("Latitude" ,location.getLatitude());
        sendBroadcast(i);
    }
    @Override
    public void callBackResult(JsonMessage nearestJsonMessage) {
        // debug
        Log.d("MESSAGE", nearestJsonMessage.getDescription()+" "+ nearestJsonMessage.getDistance());


        // If the distance(between current and message position) is minus than 3m and I haven't discovered this message yet
        // than I will show a notification!
        if(nearestJsonMessage.getNumDistance()<3) {

            Message entry = new Message();
            nearestJsonMessage.inflateEntry(entry);

            // Add message into room database
            db.messageDao().addMessage(entry);
            Log.d("MESSAGGIO", nearestJsonMessage.getUser());

            // run notificaton
            MessageFound(nearestJsonMessage.getName(), nearestJsonMessage.getId());

        }
    }
    @Override
    public void onProviderDisabled(String s) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }
    @Override
    public void onProviderEnabled(String s) { }

    public void MessageFound(String user, int code) {

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "leave_message")
                .setSmallIcon(R.drawable.notifyicon)
                .setContentTitle("Leave a message")
                .setContentText("Eilà hai trovato un nuovo mesaggio di "+user+"! Vieni a controllare")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),code,new Intent(getApplicationContext(), MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationChannel mChannel = new NotificationChannel("leave_message", "leave_message", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(code, notificationBuilder.build());
    }
}