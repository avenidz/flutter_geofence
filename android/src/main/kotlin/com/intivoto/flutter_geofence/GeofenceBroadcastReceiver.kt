package com.intivoto.flutter_geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "GeoBroadcastReceiver"

        var callback: ((GeoRegion) -> Unit)? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DC", "Called onreceive")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "something went wrong")
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val event = if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) GeoEvent.entry else GeoEvent.exit
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            for (geofence: Geofence in triggeringGeofences) {
                val region = GeoRegion(id=geofence.requestId,
                        latitude = geofencingEvent.triggeringLocation.latitude,
                        longitude = geofencingEvent.triggeringLocation.longitude,
                        radius = 50.0.toFloat(),
                        events = listOf(event)
                        )
                if(event == GeoEvent.entry){
                    sendNotification("Infinite Automation", strDetails = "Welcome to " + region.id, context = context);
                }else{
                    sendNotification("Infinite Automation", strDetails = "Exit to " + region.id, context = context);
                }

                callback?.invoke(region)
                Log.i(TAG, region.toString())
            }
        } else {
            // Log the error.
            Log.e(TAG, "Not an event of interest")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(strTital: String, strDetails: String, context: Context){
        try{
            val mNotificationManager: NotificationManager

            val mBuilder = NotificationCompat.Builder(context.getApplicationContext(), "notify_001")


            val bigText = NotificationCompat.BigTextStyle()
            bigText.bigText(strTital)
            bigText.setBigContentTitle(strDetails)
            bigText.setSummaryText(strDetails)


            mBuilder.setSmallIcon(R.drawable.common_full_open_on_phone)
            mBuilder.setContentTitle(strTital)
            mBuilder.setContentText(strDetails)
            mBuilder.priority = Notification.PRIORITY_MAX
            mBuilder.setStyle(bigText)

            mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "Your_channel_id"
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(channel)
                mBuilder.setChannelId(channelId)
            }
            mNotificationManager.notify(0, mBuilder.build())
            Log.i(TAG, "Notification send complete")
        }catch (ex:Exception){
            Log.i(TAG, ex.toString())
        }

    }

}