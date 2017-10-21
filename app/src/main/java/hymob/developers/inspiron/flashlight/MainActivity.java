package hymob.developers.inspiron.flashlight;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import hymob.developers.inspiron.flashlight.service.NotificationService;

public class MainActivity extends AppCompatActivity {

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.durga.action.close")){
                finish();
            }
        }
    };


    /*
  **FOR permission
   */
    private static final int CAMERA_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    public static  final  int NOTIFICATION_ID = 1;
    public static  final  String ACTION_ID = "1";
    NotificationManager manager;

         Handler handler = new Handler();
    boolean doubleBackToExitPressedOnce = false;

    Button flashbutton;
    ImageView imageView;
     android.hardware.Camera camera;
    CardView cardView;
  //  private android.hardware.Camera.Parameters parameters;
    private boolean IS_FLASH_ON = false;
    private boolean HAS_FLASH = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //destrong from service
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.durga.action.close");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

        // view initialization
        flashbutton = (Button) findViewById(R.id.light_button);
        imageView = (ImageView) findViewById(R.id.imageView_bulb);
        cardView = (CardView) findViewById(R.id.cardview_button);

        //checking support for flash
        HAS_FLASH = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!HAS_FLASH){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("SORRY, your device doesn't have flashlight").setTitle("Alert")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // closing application
                            finish();
                        }
                    });
            AlertDialog dialog =builder.create();
            dialog.show();
            return;
        }

        //===================================================================

        //========== asking permision for CAMERA ==================================
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

      /*  if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            askPermission();
        }else{

            getCamera();
            togleImage();
        }*/

        getCamera();
        togleImage();

        //===================================================================








        flashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (IS_FLASH_ON){
                 turnOffFlash();
               }else {
                   if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                       askPermission();
                   }else{
                   turnOnFlash();}
               }
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_FLASH_ON){
                    turnOffFlash();
                }else {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        askPermission();
                    }else{
                        turnOnFlash();}
                }
            }
        });


    }

    //calling camera
    private void getCamera(){
        if (camera == null){
            try {
                camera = android.hardware.Camera.open();
                android.hardware.Camera.Parameters parameters = camera.getParameters();

            }catch (RuntimeException e){
                Log.e("Cmaera failed to open",e.getMessage());

            }
        }
    }
    /*
    **Turning On Flash
     */
    private void turnOnFlash(){
       /* if (!IS_FLASH_ON){
            if (camera == null || parameters == null)
                return;
        }*/
        try {
            camera = android.hardware.Camera.open();
            android.hardware.Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();

            camera.autoFocus(new android.hardware.Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, android.hardware.Camera camera) {

                }
            });
            //notification for flashlight
                showNotification();
            flashbutton.setText(R.string.off);
            cardView.setBackgroundColor(getResources().getColor(R.color.red));


            IS_FLASH_ON = true;

            //changing IMage in image view
            togleImage();
        } catch (RuntimeException e) {
            Toast.makeText(MainActivity.this,"Camera Permission is not granted",Toast.LENGTH_SHORT).show();
            askPermission();
        }
    }

    /*
    **Turning off flash
     */

    public void turnOffFlash(){
       /* if (!IS_FLASH_ON){
            if (camera == null || parameters == null)
                return;
        }*/
        handler.post(new Runnable() {
            @Override
            public void run() {
                camera = android.hardware.Camera.open();
                android.hardware.Camera.Parameters parameters = camera.getParameters();

                parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);

                camera.stopPreview();
                camera.release();
                flashbutton.setText(R.string.on);
                cardView.setBackgroundColor(getResources().getColor(R.color.green));

                //hide notifcation
                hideNotification();

                IS_FLASH_ON = false;


                //togleImage
                togleImage();
            }
        });

    }


    /*
    **method to chnage image
     */
    private void togleImage(){
        if (IS_FLASH_ON){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_bulb));
        }else{
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_bulbyellow));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(IS_FLASH_ON){
            turnOnFlash();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera !=null){
            camera.release();
            camera = null;
           if (manager != null)
            manager.cancel(NOTIFICATION_ID);
        }
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);


    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (IS_FLASH_ON){
            turnOnFlash();
        }*/

       // manager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }
    /*
**method to ask permission
 */

    private  void askPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.CAMERA)) {
            //Show Information about why you need the permission

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);

      /*      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();*/
        } else if (permissionStatus.getBoolean(android.Manifest.permission.CAMERA,false)) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Camera Permission");
            builder.setMessage("This app needs camera permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            //just request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
        }


        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(android.Manifest.permission.CAMERA,true);
        editor.commit();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The camera Permission is granted to you... Continue your left job...

                    turnOnFlash();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Camera Permission");
                    builder.setMessage("This app needs camera permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);


                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                //proceedAfterPermission();

                turnOnFlash();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
               // proceedAfterPermission();
               // turnOnFlash();
            }
        }
    }
/*
 *method to show notificaiton
 */

    public void showNotification(){
        Log.e("ntification","eneterd");
        Log.i("ntification","eneterd");

        //creation of pending intent to handle click notificaiton
        Intent actionIntent = new Intent(MainActivity.this, NotificationService.class);
        actionIntent.setAction(ACTION_ID);


        PendingIntent actionPendingIntent = PendingIntent.getService(MainActivity.this, 0,
                actionIntent, PendingIntent.FLAG_ONE_SHOT);



    //    new NotificationService(MainActivity.this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_torch);

        mBuilder.setAutoCancel(true);
        mBuilder.setColor(getResources().getColor(R.color.green));
        mBuilder.setContentTitle(getResources().getString(R.string.notification_title));
        mBuilder.setContentIntent(actionPendingIntent);
        mBuilder.setContentText(getResources().getString(R.string.notification_content_title));

        Notification mNotificaiton;

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificaiton =  mBuilder.build();
        } else {
            mNotificaiton = mBuilder.getNotification();
        }
        mNotificaiton.flags |= Notification.FLAG_NO_CLEAR |Notification.FLAG_ONGOING_EVENT;

        manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID,mNotificaiton);


    }

    /*
 *method to hide notificaiton
 */

    protected void hideNotification(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.cancel(NOTIFICATION_ID);
            }
        });
    }


}

