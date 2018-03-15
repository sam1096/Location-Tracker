package a.b;

import android.app.Activity;
import static android.app.Activity.RESULT_OK;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.View;

import java.io.*;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {

    protected static final int RESULT_SPEECH = 1;
    /**
     * Called when the activity is first created.
     */

    TextView txtText;
    String bmobileno = null;
    GPSTracker gps;
String finalMsg="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
         try {

            File myFile = new File("/sdcard/userdata.txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) 
            {
                aBuffer += aDataRow ;
            }
           
            
            setContentView(R.layout.speak);
        txtText = (TextView) findViewById(R.id.txtText);
             Toast.makeText(this,aBuffer,Toast.LENGTH_LONG).show();
             String tempdata[] = aBuffer.split(",");
             bmobileno = tempdata[3];
             
             
            myReader.close();
           // Toast.makeText(this,"Done reading SD 'mysdfile.txt'",Toast.LENGTH_SHORT).show();
        } 
        catch (Exception e)
        {
             setContentView(R.layout.main);
           // Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        
       
    }

    public void submitclick(View v) {
        EditText name = (EditText) findViewById(R.id.name);
        EditText mobile = (EditText) findViewById(R.id.mobile);
        EditText bname = (EditText) findViewById(R.id.bname);
        EditText bmobile = (EditText) findViewById(R.id.bmobile);
        bmobileno = bmobile.getText().toString();

        if(name.getText().toString().length()==0 
                || mobile.getText().toString().length()==0
                || bname.getText().toString().length()==0
                || bmobile.getText().toString().length()==0)
        {
            Toast.makeText(this, "Value cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }
                
                
                
        try {
            String firstmsg = "Hi " + bname.getText().toString()
                    + "\n, i have added as a helping person";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(bmobile.getText().toString(),
                    null, firstmsg, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        String msg = name.getText().toString() + ";"
                + mobile.getText().toString() + ";"
                + bname.getText().toString() + ";"
                + bmobile.getText().toString() + ";";

        try {

            File myFile = new File("/sdcard/userdata.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
            myOutWriter.append(msg);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(v.getContext(),"Done writing SD 'mysdfile.txt'", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        setContentView(R.layout.speak);
        txtText = (TextView) findViewById(R.id.txtText);

    }

    public void speakClick(View v) {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                "en-US");

        try {
            startActivityForResult(intent, RESULT_SPEECH);
            txtText.setText("");
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Ops! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    txtText.setText(text.get(0));
                    if (txtText.getText().equals("help")) {
                        helpSMS();
                    }
                    //  SendSMS(msg, mob);
                }
                break;
            }

        }
    }

    public void helpSMS() {

        gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled	
        String msg = null;
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            LocationAddress locationAddress = new LocationAddress();
                    locationAddress.getAddressFromLocation(latitude, 
                            longitude,
                            getApplicationContext(),
                            new GeocoderHandler());
      
            
            
            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            msg = "Your Location is - \nLat: " + latitude + "\nLong: " + longitude;

        } else {
		        	// can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        

    }
private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            finalMsg = locationAddress;
            
             Toast.makeText(getApplicationContext(),
                     locationAddress,Toast.LENGTH_SHORT).show(); 
             try {
            String firstmsg = "i need help \n" + finalMsg;

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(bmobileno,
                    null, firstmsg, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
            //tvAddress.setText(locationAddress);
        }
    }
}
