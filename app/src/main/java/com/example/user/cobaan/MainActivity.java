package com.example.user.cobaan;

        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import android.app.Activity;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.os.Build;
        import android.telephony.SmsManager;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.content.Intent;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Bundle;
        import android.provider.ContactsContract;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;
        import org.eclipse.paho.client.mqttv3.MqttException;

        import java.util.List;

        import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SMS = 0;
    private static final int REQ_PICK_CONTACT = 2 ;
    private EditText phoneEditText;
    private EditText messageEditText;

    private Button sendButton,logOutButton;
    private ImageView pickContact;
    private TextView sendStatusTextView;
    private TextView deliveryStatusTextView;
    public TextView tt;

    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //phoneEditText = (EditText) findViewById(R.id.phone_number_edit_text);
        //messageEditText = (EditText) findViewById(R.id.message_edit_text);
        sendButton = (Button) findViewById(R.id.send_button);
        logOutButton = (Button) findViewById(R.id.logout_button);
        sendStatusTextView = (TextView) findViewById(R.id.message_status_text_view);
        deliveryStatusTextView = (TextView) findViewById(R.id.delivery_status_text_view);
        TextView text_no1 = (TextView) findViewById(R.id.tv_no1);
        TextView text_no2 = (TextView) findViewById(R.id.tv_no2);
        TextView text_no3 = (TextView) findViewById(R.id.tv_no3);

        //Bundle bundle = getIntent().getExtras();
        //text_no1.setText(bundle.getString("data1"));
        text_no1.setText(Preferences.getLoggedInUser1(getBaseContext()));
        //text_no2.setText(bundle.getString("data2"));
        text_no2.setText(Preferences.getLoggedInUser2(getBaseContext()));
        //text_no3.setText(bundle.getString("data3"));
        text_no3.setText(Preferences.getLoggedInUser3(getBaseContext()));

        //pickContact = (ImageView) findViewById(R.id.add_contact_image_view);

        MQTTClient mqttClient = new MQTTClient(this);
        try {
            mqttClient.connectToMQTT();
            mqttClient.subscribeToMQTT();
        } catch (MqttException e) {
            e.printStackTrace();
        }


        /*sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasSMSPermission = checkSelfPermission(SEND_SMS);
                    if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to Send SMS");
                            return;
                        }
                        requestPermissions(new String[] {SEND_SMS},
                                REQUEST_SMS);
                        return;
                    }
                    sendMySMS();
                }
            }
        });*/

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Menghapus Status login dan kembali ke Login Activity
                Preferences.clearLoggedInUser(getBaseContext());
                startActivity(new Intent(getBaseContext(),RegisterActivity.class));
                finish();
            }
        });

        /*pickContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 2);
            }
        });*/
    }

    public void updateView(final String buttonMessage) {
        //textView.setText(sensorMessage); --> Exception
        //Only the original thread that created a view hierarchy can touch its views.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tt = (TextView) findViewById(R.id.tes);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasSMSPermission = checkSelfPermission(SEND_SMS);
                    if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to Send SMS");
                            return;
                        }
                        requestPermissions(new String[] {SEND_SMS},
                                REQUEST_SMS);
                        return;
                    }
                    tt.setText(buttonMessage);
                    sendMySMS();
                }
            }
        });
    }

    public void sendMySMS() {
        //Bundle bundle = getIntent().getExtras();
        String phone1 = Preferences.getLoggedInUser1(getBaseContext());
        String phone2 = Preferences.getLoggedInUser2(getBaseContext());
        String phone3 = Preferences.getLoggedInUser3(getBaseContext());

        String message = "Send help immediately!";

        //Check if the phoneNumber is empty
        if (phone1.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {

                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone1, null, msg, sentIntent, deliveredIntent);
                sms.sendTextMessage(phone2, null, msg, sentIntent, deliveredIntent);
                sms.sendTextMessage(phone3, null, msg, sentIntent, deliveredIntent);

            }
        }
    }

    public void onResume() {
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Error Tidak Diketahui";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "SMS Terkirim !!";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : Tidak Ada Layanan";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Sinyal Off";
                        break;
                    default:
                        break;
                }
                sendStatusTextView.setText(s);

            }
        };
        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                deliveryStatusTextView.setText(s);
                //phoneEditText.setText("");
                //messageEditText.setText("");
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }


    public void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, REQUEST_SMS);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access sms", Toast.LENGTH_SHORT).show();
                    sendMySMS();

                }else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and sms", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to both the permissions");
                            return;
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void showMessageOKCancel(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title dialog
        alertDialogBuilder.setTitle("Warning");

        // set pesan dari dialog
        alertDialogBuilder
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("Ya",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{SEND_SMS},
                                    REQUEST_SMS);
                        }
                        //MainActivity.this.finish();
                    }
                })
                .setNegativeButton("Tidak",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // jika tombol ini diklik, akan menutup dialog
                        // dan tidak terjadi apa2
                        dialog.cancel();
                    }
                });

        // membuat alert dialog dari builder
        AlertDialog alertDialog = alertDialogBuilder.create();

        // menampilkan alert dialog
        alertDialog.show();
    }

    /*private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneEditText.setText(number);
            }
        }
    }
}
