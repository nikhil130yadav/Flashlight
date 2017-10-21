package hymob.developers.inspiron.flashlight.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import hymob.developers.inspiron.flashlight.MainActivity;

/**
 * Created by Inspiron on 4/5/2017.
 */


public class NotificationService extends IntentService {

    private Context mContext;
    private NotificationManager notificationManager;
       // Handler handler = new Handler(getMainLooper());
    public NotificationService(){

        super(NotificationService.class.getSimpleName());

    }

    public NotificationService(Context context){
        super(NotificationService.class.getSimpleName());
        this.mContext = context;
         notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);



    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (MainActivity.ACTION_ID.equals(action)){

      /*      android.hardware.Camera camera = android.hardware.Camera.open();
            android.hardware.Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);

            camera.stopPreview();
            camera.release();*/
//handler.post(new Runnable() {
//    @Override
//    public void run() {
//
//    }
//});
      /*      Intent i = new Intent(NotificationService.this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle killactivity = new Bundle();
            killactivity.putInt("kill",1);
            i.putExtras(killactivity);
            getApplication().startActivity(i);*/

           /* MainActivity obj = new MainActivity();
            obj.finish();


            //cancel the notification
//           notificationManager.cancel(MainActivity.NOTIFICATION_ID);
            stopSelf();*/
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                    .getInstance(NotificationService.this);
            localBroadcastManager.sendBroadcast(new Intent(
                    "com.durga.action.close"));

        }
    }
}
