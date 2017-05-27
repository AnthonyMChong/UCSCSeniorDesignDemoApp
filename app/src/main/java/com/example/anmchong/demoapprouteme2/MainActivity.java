package com.example.anmchong.demoapprouteme2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;


public class MainActivity extends AppCompatActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener{

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 8000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    protected GoogleApiClient mGoogleApiClient;

    protected Boolean mRequestingLocationUpdates = Boolean.FALSE;

    protected String mLastUpdateTime;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;


    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected  Button fakeButton;

    String SendURL = "https://routeme2app.mybluemix.net/api/check_location";
    //String SendURL = "https://requestb.in/1hnp9hs1";
    String BasicAuth;

    Button DegValButton;
    Button RadValButton;

    TextView TV1;
    TextView TV2;
    TextView TV3;
    TextView TV4;
    TextView MyCartText;

    EditText DegVal;
    EditText RadVal;
    EditText OffsetVal;
    EditText TripInstanceId;

    TextView RadiusOut;
    TextView DegreeOut;
    TextView MyLatLongTextView;

    EditText SimBecVal1;
    EditText SimBecVal2;
    EditText SimBecVal3;

    EditText ActualDeg;
    EditText ActualRad;

    RelativeLayout ColorLayout;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    String StringOfData = "";
    String StringOfDataFinal = "";
    String StringOfDataIB = "";
    String StringOfDataFinalIB = "";
    private static final int REQUEST_ENABLE_BT = 1;

    int maxReadings = 4;

    private static final long SCAN_PERIOD = 100000;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    String LastBusSeen = "none";
    String WhichStop = "none";
    int ProximityBecVal;
    int BusClearindex = 0;


// Beacon names here, this app will only take data on beacons in this list....
    String MeasuredIbeaconxW1I = "xW1I"; // the bus beacon 200ms 6dBm
    String MeasuredIbeaconUos6 = "33tM"; // Beacon A 200ms 6dBm
    String MeasuredIbeacond2Rq = "XHzQ"; // Beacon B 200ms 6dBm
    String MeasuredIbeaconpYEM = "pWEE"; // Beacon C 200ms 6dBm

    String MeasuredBUS928 = "z1dy";
    String MeasuredBUS921 = "mnZr";
    String MeasuredBUSd2Rq = "d2Rq";
    String MeasuredBUSpYEM = "pYEM";
    //String MeasuredBUS928 = "Uos6";
    //String MeasuredBUS921 = "d2Rq";

    int[][] BeaconVal = new int[12][256];
    int[] AverageRSSI = new int [12];
    int[] ReadingIndex = new int [12];
    int[] BusClear = new int [10];

    int[] LastLocalBeacons = new int[21];
    int LastLocalIndex = 0;
    int llindex =0;


    int BeaconCount = 4; // number of beacons we want to read

//    int[0] StorexW1I = new int[20];
//    int[1] StoreUos6 = new int[20];
//    int[2] Stored2Rq = new int[20];
//    int[3] StorepYEM = new int[20];

    String[] fileLines;
    String[] fileIndividual;

    float b_lat = 0;
    float b_long = 0;
    float b_accuracy = 0;

    String modeParam = "walking";

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_app_route_me2);
        mHandler = new Handler();

        fakeButton = (Button) findViewById( R.id.fakebusbutton);

        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        TV1 = (TextView) findViewById(R.id.textView);
        TV2 = (TextView) findViewById(R.id.textView2);
        TV3 = (TextView) findViewById(R.id.textView3);
        TV4 = (TextView) findViewById(R.id.textView4);
        MyLatLongTextView = (TextView) findViewById(R.id.LatLongTextView);
        MyCartText = (TextView) findViewById(R.id.CartText);
        TripInstanceId = (EditText)findViewById(R.id.tripinstanceidedittext);
        TripInstanceId.setText("d09bb388429cc600a287840fb9862e9a");

        RadiusOut = (TextView) findViewById(R.id.RadiusOut);
        DegreeOut = (TextView) findViewById(R.id.DegreeOut);

        SimBecVal1 = (EditText) findViewById(R.id.SimBecVal1);
        SimBecVal2 = (EditText) findViewById(R.id.SimBecVal2);
        SimBecVal3 = (EditText) findViewById(R.id.SimBecVal3);

        ActualDeg = (EditText) findViewById(R.id.ActualDeg);
        ActualRad = (EditText) findViewById(R.id.ActualRad);

        ColorLayout = (RelativeLayout) findViewById(R.id.activity_main);

        Arrays.fill(BeaconVal[0],1);
        Arrays.fill(BeaconVal[1],1);
        Arrays.fill(BeaconVal[2],1);
        Arrays.fill(BeaconVal[3],1);

// opening file


        AssetManager am = this.getAssets();
        InputStream InfoFileStream = null;
        try {
            InfoFileStream = am.open("FingerPrintTable.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileContent = "";    // string to be filled with data
        byte[] readbuffer = new byte[10240];
        int n;
        try {
            while ((n = InfoFileStream.read(readbuffer)) != -1) {
                fileContent += (new String(readbuffer, 0, n));  // adding every line into string
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileLines = fileContent.split("\n"); // split data with new line
        fileIndividual = (fileLines[1]).split(",");

        //-74,-89,-72,30.0,9.0,0.0
        //fileIndividual = (fileLines[1]).split(",");  // break up line starts at 0


        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        mBluetoothAdapter.enable();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                    @Override

                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }

                    }


                });

                builder.show();
            }
        }

        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        for(llindex = 0; llindex < 10; llindex++){
            LastLocalBeacons [llindex] = 1;
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }


        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.

        // Initializes list view adapter.
        //scanLeDevice(true);
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }




        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            // User chose not to enable Bluetooth.
            if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

    @Override
    protected void onPause() {
        super.onPause();
//        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    public int findRSSIavg(int beacindex){
        int avg = 0;
        int aindex = 0;
        for (aindex = 0; aindex < maxReadings; aindex++) {
            avg += BeaconVal[beacindex][aindex];
        }
        avg = avg / maxReadings;
        return avg;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //device.EXTRA_RSSI = Integer.toString(rssi);
                    String mys = new String(scanRecord);
                    String MeasuredEddyUID = "xW1I";    //Eddystone device name we want to read
                    String MeasuredIbeacon = "xW1I";    //Ibeacon device name we want to read

                    String FoundMeasureUID = "";        // initializing strings for future comparisons
                    String FoundMeasureURL = "";
                    String FoundMeasureIB = "";
                        //Toast.makeText(getApplicationContext(), "  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
                        // The above code will notify the user when a beacon is being detected along with the device RSSI
                        //StringOfData += device.getName() + "\n"+"RSSI:"+Integer.toString(rssi) +"\n"+"Bytes:"+scanRecord+"\n"+mys+"\n\n";
                        StringBuilder sb = new StringBuilder();
                        for (byte b : scanRecord) {
                            sb.append(String.format("%02X ", b));
                        }
                        FoundMeasureUID += Character.toString((char) scanRecord[45])+Character.toString((char) scanRecord[46])
                                +Character.toString((char) scanRecord[47])+Character.toString((char) scanRecord[48]);
                        FoundMeasureURL += Character.toString((char) scanRecord[47])+Character.toString((char) scanRecord[48])
                                +Character.toString((char) scanRecord[49])+Character.toString((char) scanRecord[50]);
                        FoundMeasureIB += Character.toString((char) scanRecord[46])+Character.toString((char) scanRecord[47])
                                +Character.toString((char) scanRecord[48])+Character.toString((char) scanRecord[49]);
                    /*
                        // The above code makes a string to compare to the read string
                        // if the read string matches with one of these, the bluetooth reading belongs
                        // to a beacon and its specific framework
                        if (FoundMeasureUID.compareTo(MeasuredEddyUID) == 0) {
                            //StringOfData +=" " + rssi+"\n";
                            TV1.setText(Integer.toString(rssi));
                        }

                        if (FoundMeasureURL.compareTo(MeasuredEddyUID) == 0) {

                            StringOfDataIB +=" " + rssi;
                            TV2.setText(Integer.toString(rssi));
                        }
                        if (FoundMeasureIB.compareTo(MeasuredIbeacon) == 0) {

                            //IBfound = 1;

                            StringOfDataIB +=" " + rssi;
                            TV1.setText(Integer.toString(rssi));
                        }
                */
                    if (FoundMeasureIB.compareTo(MeasuredBUS928) == 0) {
                        //StringOfDataIB +="" + rssi;
                        TV1.setText("BUS: 928" + Integer.toString(rssi));
                        ColorLayout.setBackgroundColor(Color.GREEN);
                        LastBusSeen = "928";
                        LastLocalBeacons[BusClearindex] = 1;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredBUS921) == 0) {
                        //StringOfDataIB +="" + rssi;
                        TV1.setText("BUS: 921" + Integer.toString(rssi));
                        ColorLayout.setBackgroundColor(Color.GREEN);
                        LastBusSeen = "921";
                        LastLocalBeacons[BusClearindex] = 1;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredBUSd2Rq) == 0) {
                        //StringOfDataIB +="" + rssi;
                        TV1.setText("BUS: d2Rq" + Integer.toString(rssi));
                        ColorLayout.setBackgroundColor(Color.GREEN);
                        LastBusSeen = "d2Rq";
                        LastLocalBeacons[BusClearindex] = 1;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredBUSpYEM) == 0) {
                        //StringOfDataIB +="" + rssi;
                        TV1.setText("BUS: pYEM" + Integer.toString(rssi));
                        ColorLayout.setBackgroundColor(Color.RED);
                        LastBusSeen = "pYEM";
                        LastLocalBeacons[BusClearindex] = 1;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconxW1I) == 0) {
                        StringOfDataIB +="" + rssi;
                        //TV1.setText("xW1I:   " + Integer.toString(rssi));
                        //ColorLayout.setBackgroundColor(Color.GREEN);
                        BeaconVal [0][ReadingIndex[0]] = rssi;
                        ReadingIndex[0]++;
                        if (ReadingIndex[0]>maxReadings)
                            ReadingIndex[0] = 0;
                        ProximityBecVal = rssi;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconUos6) == 0) {
                        StringOfDataIB +="" + rssi;
                        TV2.setText("Uos6:   " + Integer.toString(rssi));
                        BeaconVal [1][ReadingIndex[1]] = rssi;
                        ReadingIndex[1]++;
                        if (ReadingIndex[1]>maxReadings) {
                            ReadingIndex[1] = 0;
                        }
                        //SimBecVal3.setText(Integer.toString(findRSSIavg(1)));
                        SimBecVal3.setText(Integer.toString(rssi));
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeacond2Rq) == 0) {
                        StringOfDataIB +="" + rssi;
                        TV3.setText("d2Rq:   " + Integer.toString(rssi));
                        BeaconVal [2][ReadingIndex[2]] = rssi;
                        ReadingIndex[2]++;
                        if (ReadingIndex[2]>maxReadings) {
                            ReadingIndex[2] = 0;
                        }
                        //SimBecVal2.setText(Integer.toString(findRSSIavg(2)));
                        SimBecVal2.setText(Integer.toString((rssi)));
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconpYEM) == 0) {
                        StringOfDataIB +="" + rssi;
                        //TV4.setText("pYEM:   " + Integer.toString(rssi));
                        BeaconVal [3][ReadingIndex[3]] = rssi;
                        ReadingIndex[3]++;
                        if (ReadingIndex[3]>maxReadings) {
                            ReadingIndex[3] = 0;
                        }
                        //SimBecVal1.setText(Integer.toString(findRSSIavg(3)));
                        SimBecVal1.setText(Integer.toString((rssi)));
                    }
                    // keeps track of the last readings from localization
                    if ( (FoundMeasureIB.compareTo(MeasuredIbeaconxW1I) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconUos6) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeacond2Rq) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconpYEM) == 0)) {
                        LastLocalBeacons[LastLocalIndex] = 1;
                    }else {
                        LastLocalBeacons[LastLocalIndex] = 0;
                    }
                    LastLocalIndex++;
                    if (LastLocalIndex > 10){
                        LastLocalIndex = 0;
                    }
                    // keeps track the last 20 beacon readings maily to determine if we got
                    // on a bus

                    TV2.setText(WhichStop);

                    if ((FoundMeasureUID.compareTo(MeasuredEddyUID) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconxW1I) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconUos6) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeacond2Rq) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconpYEM) == 0) |
                            (FoundMeasureURL.compareTo(MeasuredEddyUID) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeacon) == 0) )
                    {
                        ///////////////////////////////////////////////////////////////////////////////////////////
                        // Sorting Algorithm Start here///////////////////////////////////////////////////////////
        /* code in this area will run when the cancel button is pressed

         */
                        float foundDeg;
                        float foundRadius;
                        int Kneighors = 4;
                        int lineIndex;
                        float CurrentDiff;

                        float [][] Lowest4 = new float[Kneighors][8];
                        // initializing array to be filled with high numbers or comparison will not work
                        for (int initindex = 0 ; initindex< Kneighors;initindex++ ){
                            for (int subinitindex = 0; subinitindex < 3; subinitindex++){
                                Lowest4 [initindex][subinitindex] = 300;
                            }
                        }

                        int lowindex = 0;
                        int bumpindex = 0;

                        float CurrentSimBecVal3 = new Integer(SimBecVal1.getText().toString()).floatValue();
                        float CurrentSimBecVal2 = new Integer(SimBecVal2.getText().toString()).floatValue();
                        float CurrentSimBecVal1 = new Integer(SimBecVal3.getText().toString()).floatValue();
                        // Above is how you can read from a UI input, you can test your values like this
                        if ((CurrentSimBecVal3!=-101) && (CurrentSimBecVal2!=-101) && (CurrentSimBecVal1!=-101)) {
                            for (lineIndex = 0; lineIndex < fileLines.length; lineIndex++) {  // going through each line until we dont have anymore
                                fileIndividual = fileLines[lineIndex].split(",");                // splitting current line
                                CurrentDiff = abs(Float.parseFloat(fileIndividual[2]) - (float) CurrentSimBecVal1) +
                                        abs(Float.parseFloat(fileIndividual[1]) - (float) CurrentSimBecVal2) +
                                        abs(Float.parseFloat(fileIndividual[0]) - (float) CurrentSimBecVal3);

                                for (lowindex = 0; lowindex < Kneighors; lowindex++) {  // 4 worst match, highest mag
                                    if (Lowest4[lowindex][0] > CurrentDiff) {  // start comparing at best match
                                        // this for loop will knock down all the old information in our top 4 list
                                        for (bumpindex = Kneighors - 1; bumpindex != lowindex; bumpindex--) {
                                            Lowest4[bumpindex][0] = Lowest4[bumpindex - 1][0]; // replacing because we found lower
                                            Lowest4[bumpindex][1] = Lowest4[bumpindex - 1][1];
                                            Lowest4[bumpindex][2] = Lowest4[bumpindex - 1][2];
                                            Lowest4[bumpindex][3] = Lowest4[bumpindex - 1][3]; // replacing because we found lower
                                            Lowest4[bumpindex][4] = Lowest4[bumpindex - 1][4];
                                            Lowest4[bumpindex][5] = Lowest4[bumpindex - 1][5];
                                            Lowest4[bumpindex][6] = Lowest4[bumpindex - 1][6];
                                            Lowest4[bumpindex][7] = Lowest4[bumpindex - 1][7];
                                        }
                                        Lowest4[lowindex][0] = CurrentDiff; // replacing because we found lower
                                        Lowest4[lowindex][1] = Float.parseFloat(fileIndividual[3]);
                                        Lowest4[lowindex][2] = Float.parseFloat(fileIndividual[4]);
                                        Lowest4[lowindex][3] = Float.parseFloat(fileIndividual[0]);
                                        Lowest4[lowindex][4] = Float.parseFloat(fileIndividual[1]);
                                        Lowest4[lowindex][5] = Float.parseFloat(fileIndividual[2]);
                                        Lowest4[lowindex][6] = Float.parseFloat(fileIndividual[6]);
                                        Lowest4[lowindex][7] = Float.parseFloat(fileIndividual[7]);
                                        break;
                                    }
                                }
                            }

                            // if you want to read the file, the lines are already saved in the filelines array
                            // ex: if you wanted to access the 3rd line in the file filelines[2]
                            // Since it is a CSV you must split up each line for the information
                            // ex: if you wanted the 2nd value of the 3rd line you would run
                            // fileIndividual = (fileLines[2]).split(",");
                            // fileIndividual[1];
                            // fileIndividual is a global string array defined at the top
                            // Good luck!

                            TV1.setText("mag: " + Float.toString(Lowest4[0][0]) + " deg: " + Float.toString(Lowest4[0][1]) + " rad: " + Float.toString(Lowest4[0][2]) +
                                    " A:" + Float.toString(Lowest4[0][3]) + " B:" + Float.toString(Lowest4[0][4]) + " C:" + Float.toString(Lowest4[0][5]));
                            TV2.setText(WhichStop);
                            TV3.setText("LatLongTest: "+ Lowest4[0][6]  + ","+ Lowest4[1][6]  + ","+ Lowest4[2][6]  + ","+ Lowest4[3][6]  + ","+"\n"
                                    + Lowest4[0][7]  + ","+ Lowest4[1][7]  + ","+ Lowest4[2][7]  + ","+ Lowest4[3][7]);
                            //TV4.setText("mag: " + Float.toString(Lowest4[3][0]) + " deg: " + Float.toString(Lowest4[3][1]) + " rad: " + Float.toString(Lowest4[3][2]) +
                             //       " A:" + Float.toString(Lowest4[3][3]) + " B:" + Float.toString(Lowest4[3][4]) + " C:" + Float.toString(Lowest4[3][5]));


                            // Weighted average with K near neighbors
                            float weightHold = 0;
                            for (lowindex = 0; lowindex < Kneighors; lowindex++) {
                                //Log.d("myTag", "lowindex: "+lowindex+"   Lowest4[lowindex][0]: "+Lowest4[lowindex][0] +"   rad: " + Float.toString(Lowest4[0][2]) + " RSSIA"+CurrentSimBecVal1);
                                if (Lowest4[lowindex][0] != 0) {
                                    weightHold += (1 / (Lowest4[lowindex][0]));
                                } else {
                                    weightHold = -1;    // perfect match found
                                    break;
                                }
                            }
                            foundRadius = 0;
                            foundDeg = 0;
                            float SinSum = 0;
                            float CosSum = 0;
                            float xSum = 0;
                            float ySum = 0;
                            float latSum = 0;
                            float longSum = 0;
                            int MagTotal = 0;

                            if (weightHold == -1) {  // set to perfect match
                                foundDeg = Lowest4[0][1];
                                foundRadius = Lowest4[0][2];
                                ySum = (float) ((foundRadius * sin(Math.toRadians(foundDeg))));
                                xSum = (float) ((foundRadius * cos(Math.toRadians(foundDeg))));
                            } else {   // take the weighted avg
                                for (lowindex = 0; lowindex < Kneighors; lowindex++) {
                                    SinSum += ((1 / Lowest4[lowindex][0]) / weightHold) * (sin(Math.toRadians(Lowest4[lowindex][1])));
                                    CosSum += ((1 / Lowest4[lowindex][0]) / weightHold) * (cos(Math.toRadians(Lowest4[lowindex][1])));
                                    //Log.d("myTag", "angle: " + Lowest4[lowindex][1] + " Sin: " + SinSum + " Cos: " + CosSum);
                                    ySum += ((1 / Lowest4[lowindex][0]) / weightHold) * (Lowest4[lowindex][2] * sin(Math.toRadians(Lowest4[lowindex][1])));
                                    xSum += ((1 / Lowest4[lowindex][0]) / weightHold) * (Lowest4[lowindex][2] * cos(Math.toRadians(Lowest4[lowindex][1])));
                                    latSum += ((1 / Lowest4[lowindex][0]) / weightHold) * (Lowest4[lowindex][6]);
                                    longSum += ((1 / Lowest4[lowindex][0]) / weightHold) * (Lowest4[lowindex][7]);
                                    foundRadius += ((1 / Lowest4[lowindex][0]) / weightHold) * (Lowest4[lowindex][2]);
                                    MagTotal += Lowest4[lowindex][0];
                                }
                                //Log.d("myTag","Sin: "+ SinSum+ " Cos: "+ CosSum );
                                foundDeg = (float) toDegrees(atan2(SinSum, CosSum));

                            }
                            DegreeOut.setText("Degree:" + Float.toString(foundDeg));
                            RadiusOut.setText("Radius:" + Float.toString(foundRadius));
                            MyCartText.setText("x" + xSum + "\n" + "  y" + ySum);
                            b_lat = latSum;
                            b_long = longSum;
                            b_accuracy = getBecAccuracy(CurrentSimBecVal1, CurrentSimBecVal2, CurrentSimBecVal3);
                            TV4.setText("BecLatLong: " + Float.toString(b_lat) + " , " + Float.toString(b_long) + " , " + Float.toString(b_accuracy));
                            if ( 240 > abs((int) (CurrentSimBecVal1+CurrentSimBecVal2+CurrentSimBecVal3))){
                                WhichStop = "north_science_hill_busstop";
                            }else if (240 <= abs((int) (CurrentSimBecVal1+CurrentSimBecVal2+CurrentSimBecVal3))){
                                WhichStop = "none";
                            }
                            if (ProximityBecVal > - 67){
                                WhichStop = "south_science_hill_bustop";
                            }
                            //MyCartText.setText("x"+Double.toString(foundRadius*cos(Math.toRadians(foundDeg)))+"\n"
                            //       +"  y"+Double.toString(foundRadius*sin(Math.toRadians(foundDeg))) );
                            // how you set your ouput text displays for testing

//                            StringOfDataFinal += "" + ActualDeg.getText() + "," + ActualRad.getText() + "," + xSum + ","
//                                    + ySum + "," + MagTotal + "," + Integer.toString((int) (CurrentSimBecVal3 + CurrentSimBecVal2 + CurrentSimBecVal1)) + "\n";

                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }

                }
            };
    public float getBecAccuracy(float RSSIa,float RSSIb, float RSSIc){
        int totalRSSI = abs((int) (RSSIa+RSSIb+RSSIc)); // get the sum of rssi
        float foundaccuracy = 100;
        if (totalRSSI >= 258) {
            foundaccuracy = (float)(0 * totalRSSI + 6.8399);
        }
        if ((239 <= totalRSSI)&&(totalRSSI < 258)) {
            foundaccuracy = (float)(0.035237867954*totalRSSI + -2.24485586103);
        }
        if ((220 <= totalRSSI)&&(totalRSSI < 239)) {
            foundaccuracy = (float)(0.128836566109 *totalRSSI + -24.5433842661);
        }
        if ((201  <= totalRSSI)&&(totalRSSI < 220)) {
            foundaccuracy = (float)(0.0564741573133 *totalRSSI + -8.88146057467);
        }
        if ((totalRSSI < 201)) {
            foundaccuracy = (float)(0.0801475319747 *totalRSSI + -14.0348725814);
            if (foundaccuracy < 0)
                foundaccuracy = 1;
        }
        return (foundaccuracy);
    }


    public void startUpdatesButtonHandler(View view) {
        scanLeDevice(true);
        startLocationUpdates();
//        StringOfDataFinal += StringOfData;
//        StringOfDataFinalIB += StringOfDataIB;
//        StringOfData = "";
//        StringOfDataIB = "";
    }

    public void FakeBusHandler(View view){
        if (modeParam == "walking"){
            modeParam = "bus";
        } else if (modeParam == "bus") {
            modeParam = "walking";
        }
    }

    public void RadUpClickListener(View view) {
        float Radvalcurrent = new Float(RadVal.getText().toString()).floatValue();
        Radvalcurrent += 0.5;
        RadVal.setText(Float.toString(Radvalcurrent));
    }

    public void DegUpClickListener(View view) {
        float Degvalcurrent = new Float(DegVal.getText().toString()).floatValue();
        Degvalcurrent += 30;
        if (Degvalcurrent >= 360){
            Degvalcurrent = 0;
        }
        DegVal.setText(Float.toString(Degvalcurrent));
    }

    public void OffsetClickListener(View view) {
        float OffsetValcurrent = new Float(OffsetVal.getText().toString()).floatValue();
        OffsetValcurrent += 180;
        if (OffsetValcurrent >= 360){
            OffsetValcurrent = 0;
        }
        OffsetVal.setText(Float.toString(OffsetValcurrent));
    }

    public void SaveButtonHandler(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"anmchong@ucsc.edu"});
        i.putExtra(Intent.EXTRA_SUBJECT, "FingerPrinting Table data");
        i.putExtra(Intent.EXTRA_TEXT, StringOfDataFinal);
        StringOfDataFinal = "";
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void stopUpdatesButtonHandler(View view) {
            stopLocationUpdates();

        scanLeDevice(false);
        ColorLayout.setBackgroundColor(Color.WHITE);
        int avgindex = 0;

        int beaconindex = 0;
        for(;beaconindex<BeaconCount;) {
            for (AverageRSSI[beaconindex] = 0; (BeaconVal[beaconindex][avgindex] != 1) && (avgindex < 20); ) {
                AverageRSSI[beaconindex] += BeaconVal[beaconindex][avgindex];
                avgindex++;
            }
            if (avgindex == 0) {// there were no read values
                if (beaconindex == 0)
                    TV1.setText("avgxW1I:" + "none");
                if (beaconindex == 1)
                    TV2.setText("avgUos6:" + "none");
                if (beaconindex == 2)
                    TV3.setText("avgd2Rq:" + "none");
                if (beaconindex == 3)
                    //TV4.setText("avgpYEM:" + "none");
                AverageRSSI[beaconindex] = 0;
            } else {
                AverageRSSI[beaconindex] = AverageRSSI[beaconindex] / avgindex;
                if (beaconindex == 0)
                    TV1.setText("avgxW1I:" + AverageRSSI[beaconindex]);
                if (beaconindex == 1)
                    TV2.setText("avgUos6:" + AverageRSSI[beaconindex]);
                if (beaconindex == 2)
                    TV3.setText("avgd2Rq:" + AverageRSSI[beaconindex]);
                if (beaconindex == 3){

                }
                    //TV4.setText("avgpYEM:" + AverageRSSI[beaconindex]);
            }
            beaconindex++;
            avgindex = 0;

/*
            StringOfDataFinal += "" + AverageRSSI[1] + "," + AverageRSSI[2] + "," + AverageRSSI[3] + ","
                    + DegVal.getText().toString() + "," + RadVal.getText().toString() +
                    "," + OffsetVal.getText().toString() + "\n";
                    */
        }
        Arrays.fill(BeaconVal[0],1);
        Arrays.fill(BeaconVal[1],1);
        Arrays.fill(BeaconVal[2],1);
        Arrays.fill(BeaconVal[3],1);
        Arrays.fill(ReadingIndex,0);

        SimBecVal1.setText( "-101" );
        SimBecVal2.setText( "-101" );
        SimBecVal3.setText( "-101" );
        ProximityBecVal = -101;

    }

    String  NearestParam = "true";

    @Override
    public void onLocationChanged(Location location) {
        fakeButton.setText(modeParam);
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        MyLatLongTextView.setText(String.format("%s: %f \n%s: %f", "LAT",
                mCurrentLocation.getLatitude(), "LON", mCurrentLocation.getLongitude()));

        Toast.makeText(this,"Location Updated",
                Toast.LENGTH_SHORT).show();

        HashMap<String, String> myparams = new HashMap<String, String>();
        myparams.put("traveler_id", "50965d1fa6e7625057c1615418819dc1b9f89139924646397b43969c9ac27a17");
        myparams.put("trip_instance_id", TripInstanceId.getText().toString());
        myparams.put("latitude", Double.toString(mCurrentLocation.getLatitude()));
        myparams.put("longitude", Double.toString(mCurrentLocation.getLongitude()));
        myparams.put("nearest", NearestParam );
        myparams.put("accuracy_dist", Integer.toString((int)mCurrentLocation.getAccuracy()));
        TV4.setText(Integer.toString((int)mCurrentLocation.getAccuracy()));
        int a;
        for (a = 0; a < 9 ; a++){
            if (LastLocalBeacons[a] == 1){
                break;
            } else {
                LastBusSeen = "none";
            }
        }
        if (LastBusSeen != "none") {
            myparams.put("transit_name", LastBusSeen);
        }
        LastLocalBeacons[BusClearindex] = 0;

        if (WhichStop.compareTo("none")!=0)
            myparams.put("stop_name", WhichStop);
        // to determine if we are on the bus or walking, we check the last 20 readings for any from
        // bus stop
        for (a = 0; a < 10 ; a++){
            if (LastLocalBeacons[a] == 1){
                modeParam = "walking";
                break;
            } else if (SimBecVal1.getText().toString().compareTo("-101") !=0 ){
                modeParam = "bus";
            }
        }
        myparams.put("mode", modeParam );

        if (WhichStop.compareTo("north_science_hill_busstop")==0) {
            myparams.put("b_latitude", Float.toString(b_lat));
            myparams.put("b_longitude", Float.toString(b_long));
            myparams.put("b_accuracy_dist", Float.toString(b_accuracy));
        } else if (WhichStop.compareTo("south_science_hill_bustop")==0){
            myparams.put("b_latitude", "36.999858");
            myparams.put("b_longitude", "-122.062170");
            myparams.put("b_accuracy_dist", "3");
//            myparams.put("b_latitude", SouthStopLat);
//            myparams.put("b_longitude", SouthStopLong);
//            myparams.put("b_accuracy_dist", SouthStopAccuracy);
            b_lat = (float)36.999858;
            b_long = (float)-122.062170;
            b_accuracy = (float)3;
        }
        double f_lat;
        double f_long;

        if (WhichStop.compareTo("none")==0){
            f_lat = mCurrentLocation.getLatitude();
            f_long = mCurrentLocation.getLongitude();
        }else {
            f_lat = (double) ((1 / ((pow((double) b_accuracy, -2)) + (pow(mCurrentLocation.getAccuracy(), -2)))) *
                    ((b_lat * (pow((double) b_accuracy, -2))) + (mCurrentLocation.getLatitude() * (pow(mCurrentLocation.getAccuracy(), -2)))));
            f_long = (double) ((1 / ((pow((double) b_accuracy, -2)) + (pow(mCurrentLocation.getAccuracy(), -2)))) *
                    ((b_long * (pow((double) b_accuracy, -2))) + (mCurrentLocation.getLongitude() * (pow(mCurrentLocation.getAccuracy(), -2)))));
        }

        myparams.put("f_latitude",Double.toString(f_lat));
        myparams.put("f_longitude",Double.toString(f_long));

        StringOfDataFinal += "" + ActualDeg.getText() + "," + ActualRad.getText() + "," + f_lat + ","
                + f_long + "," +WhichStop+"\n";

        BasicAuth = "testing@aol.com" + ":" + "testing";
        BasicAuth = Base64.encodeToString(BasicAuth.getBytes(), Base64.DEFAULT);

        SimBecVal1.setText( "-101" );
        SimBecVal2.setText( "-101" );
        SimBecVal3.setText( "-101" );
        ProximityBecVal = -101;
        Log.d("ServerMSGERROR: ", (new JSONObject(myparams)).toString());

        JsonObjectRequest myjsonObjectRequest = new JsonObjectRequest(Request.Method.POST,SendURL,new JSONObject(myparams),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void  onResponse (JSONObject response){
                        try{
                            Toast.makeText (MainActivity.this,"Response:" + response.toString(4), Toast.LENGTH_SHORT).show();
                            VolleyLog.v("Response:%n %s", response.toString(4));
                            Log.d("ServerMSGERROR: ", response.toString(4));

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse (VolleyError error) {
                Toast.makeText (MainActivity.this,"Error:" + error, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + BasicAuth);
                //headers.put("Content-Type", "multipart/form-data; charset=utf-8");
                return headers;
            }


            @Override
            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
                //return "multipart/form-data";
            }




        };
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(myjsonObjectRequest);

    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
            startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}