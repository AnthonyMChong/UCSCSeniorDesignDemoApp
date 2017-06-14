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
import android.media.MediaPlayer;
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
import android.widget.Switch;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;


public class MainActivity extends AppCompatActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener{

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
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
    protected Button infomodeButton;

    String SendURL = "https://routeme2app.mybluemix.net/api/check_location";
    //String SendURL = "https://requestb.in/1inhlsx1";
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
    boolean transition = true;
    int onBusFlag = 0;
    int losingBus =0;

    private static final long SCAN_PERIOD = 100000;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    String LastBusSeen = "none";
    String WhichStop = "none";
    int ProximityBecVal;
    int BusClearindex = 0;
    boolean atdestination = false;

    double f_lat;
    double f_long;


// Beacon names here, this app will only take data on beacons in this list....
    String MeasuredIbeaconxW1I = "xW1I"; // the bus beacon 200ms 6dBm
    String MeasuredIbeaconUos6 = "33tM"; // Beacon A 200ms 6dBm
    String MeasuredIbeacond2Rq = "XHzQ"; // Beacon B 200ms 6dBm
    String MeasuredIbeaconpYEM = "pWEE"; // Beacon C 200ms 6dBm

    int[][] BeaconVal = new int[12][256];
    int[] AverageRSSI = new int [12];
    int[] ReadingIndex = new int [12];
    int[] BusClear = new int [10];

    int[] LastLocalBeacons = new int[21];
    int llindex =0;
    int localBecCount = 0;
    int transitBecCount = 0;

    int localizationTHRSHLD = 240;
    int proxTHRSHLD = -72;
    int proxRSSIHolder = 0;
    int proxcount = 0;


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

    ArrayList <Float> b_lat_raw = new ArrayList();
    ArrayList <Float> b_long_raw = new ArrayList();
    ArrayList <Float> b_accuracy_raw = new ArrayList();

    MediaPlayer NoStopSound;
    MediaPlayer WrongStopSound;
    MediaPlayer BoardingZoneSound;
    MediaPlayer SeeBusSound;
    MediaPlayer SeeDestinationSound;
    MediaPlayer CorrectNorth;
    MediaPlayer LeftBoardingZoneSound;
    MediaPlayer OnBusSound;


    String modeParam = "walking";

    //My States
        final int NoStop = 0;
        final int WrongStop = 1;
        final int DestinationStop = 2;
        final int BoardingZone = 3;
        final int OnBus =4;
        final int OffBus = 5;
        final int OnBusCheck = 6;

    public int muserstate;

    String BusStopCheck;
    int CorrectStop = -1;

    public String[] northBuses = {"XUK3","OMe6","Yldd","Uos6"};
    public String[] southBuses = {"z1dy","IRdH","mnZr","eNGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_app_route_me2);
        mHandler = new Handler();

        fakeButton = (Button) findViewById( R.id.fakebusbutton);

        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        infomodeButton = (Button) findViewById(R.id.DemoButton);
        TV1 = (TextView) findViewById(R.id.textView);
        TV2 = (TextView) findViewById(R.id.textView2);
        TV3 = (TextView) findViewById(R.id.textView3);
        TV4 = (TextView) findViewById(R.id.textView4);
        MyLatLongTextView = (TextView) findViewById(R.id.LatLongTextView);
        MyCartText = (TextView) findViewById(R.id.CartText);
        TripInstanceId = (EditText)findViewById(R.id.tripinstanceidedittext);
        TripInstanceId.setText("458ec04fbd56a29fc169aceb713fa985");

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

        muserstate = NoStop;

        NoStopSound = MediaPlayer.create(this, R.raw.intro);
        WrongStopSound = MediaPlayer.create(this, R.raw.wrongbusstop);
        BoardingZoneSound = MediaPlayer.create(this, R.raw.boardingzone);
        SeeBusSound = MediaPlayer.create(this, R.raw.seebus);
        SeeDestinationSound = MediaPlayer.create(this, R.raw.seedestination);
        CorrectNorth = MediaPlayer.create(this, R.raw.atnorth);
        LeftBoardingZoneSound = MediaPlayer.create(this,R.raw.leftboardingarea);
        OnBusSound = MediaPlayer.create(this, R.raw.onbus);

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

        //scanLeDevice(true);
        //startLocationUpdates();

        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        for(llindex = 0; llindex < 10; llindex++){
            LastLocalBeacons [llindex] = 0;
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
                        // to a beacon and its specific framework*/

                    // this statement checks for certain buses in a given array
                    int ourbusindex = 0;

                    for (ourbusindex = 0; ourbusindex < northBuses.length; ourbusindex++){
                       // Log.d("myTag",FoundMeasureIB + northBuses[ourbusindex] );
                        if (FoundMeasureIB.compareTo(northBuses[ourbusindex])==0){
                            TV1.setText("BUS: "+ northBuses[ourbusindex] + Integer.toString(rssi));
                            if (((CheckRightBusStop(BusStopCheck) == 1)
                                    &&(WhichStop == "north_science_hill_busstop"))
                                    |((muserstate == OnBusCheck))|(muserstate == OnBus)) {
                                LastBusSeen =northBuses[ourbusindex];
                                ColorLayout.setBackgroundColor(Color.GREEN);
                            }
                            onBusFlag = 1;
                            LastLocalBeacons[BusClearindex] = 1;
                            break;
                        }
                    }

                    for (ourbusindex = 0; ourbusindex < southBuses.length; ourbusindex++){
                        if (FoundMeasureIB.compareTo(southBuses[ourbusindex])==0){
                            TV1.setText("BUS: "+ southBuses[ourbusindex] + Integer.toString(rssi));
                            if (((CheckRightBusStop(BusStopCheck) == 1)
                                    &&(WhichStop == "south_science_hill_busstop"))
                                    |((muserstate == OnBusCheck)|(muserstate == OnBus))) {
                                LastBusSeen = southBuses[ourbusindex];
                                ColorLayout.setBackgroundColor(Color.CYAN);
                            }
                            onBusFlag = 1;
                            LastLocalBeacons[BusClearindex] = 1;
                            break;
                        }
                    }
                    if (FoundMeasureIB.compareTo("uspD") == 0) {
                        ColorLayout.setBackgroundColor(Color.BLUE);
                        WhichStop = "Destination";
                        atdestination = true;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconxW1I) == 0) {
                        StringOfDataIB +="" + rssi;
                        BeaconVal [0][ReadingIndex[0]] = rssi;
                        ReadingIndex[0]++;
                        if (ReadingIndex[0]>maxReadings)
                            ReadingIndex[0] = 0;
                        //ProximityBecVal = rssi;
                        proxRSSIHolder +=rssi;
                        proxcount++;
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconUos6) == 0) {
                        StringOfDataIB +="" + rssi;
                        //TV2.setText("Uos6:   " + Integer.toString(rssi));
                        BeaconVal [1][ReadingIndex[1]] = rssi;
                        ReadingIndex[1]++;
                        if (ReadingIndex[1]>maxReadings) {
                            ReadingIndex[1] = 0;
                        }
                        SimBecVal3.setText(Integer.toString(rssi));
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeacond2Rq) == 0) {
                        StringOfDataIB +="" + rssi;
                        BeaconVal [2][ReadingIndex[2]] = rssi;
                        ReadingIndex[2]++;
                        if (ReadingIndex[2]>maxReadings) {
                            ReadingIndex[2] = 0;
                        }
                        SimBecVal2.setText(Integer.toString((rssi)));
                    }
                    if (FoundMeasureIB.compareTo(MeasuredIbeaconpYEM) == 0) {
                        StringOfDataIB +="" + rssi;
                        BeaconVal [3][ReadingIndex[3]] = rssi;
                        ReadingIndex[3]++;
                        if (ReadingIndex[3]>maxReadings) {
                            ReadingIndex[3] = 0;
                        }
                        SimBecVal1.setText(Integer.toString((rssi)));
                    }
                    // keeps track of the last readings from localization
                    if ( (FoundMeasureIB.compareTo(MeasuredIbeaconxW1I) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconUos6) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeacond2Rq) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeaconpYEM) == 0) |
                            (FoundMeasureIB.compareTo("uspD") == 0)) {
                        LastLocalBeacons[BusClearindex] = 0;
                    }
                    BusClearindex++;
                    if (BusClearindex > 10){
                        BusClearindex = 0;
                    }
                    // keeps track the last 20 beacon readings maily to determine if we got
                    // on a bus

                    //TV2.setText(WhichStop +"  " + CheckRightBusStop(BusStopCheck));

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

                            //TV1.setText("mag: " + Float.toString(Lowest4[0][0]) + " deg: " + Float.toString(Lowest4[0][1]) + " rad: " + Float.toString(Lowest4[0][2]) +
                             //       " A:" + Float.toString(Lowest4[0][3]) + " B:" + Float.toString(Lowest4[0][4]) + " C:" + Float.toString(Lowest4[0][5]));
                            //TV2.setText(WhichStop);
                            //TV3.setText("LatLongTest: "+ Lowest4[0][6]  + ","+ Lowest4[1][6]  + ","+ Lowest4[2][6]  + ","+ Lowest4[3][6]  + ","+"\n"
                             //       + Lowest4[0][7]  + ","+ Lowest4[1][7]  + ","+ Lowest4[2][7]  + ","+ Lowest4[3][7]);
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
                            b_lat_raw.add(latSum); // = latSum;
                            b_long_raw.add(longSum); //= longSum;
                            b_accuracy_raw.add( getBecAccuracy(CurrentSimBecVal1, CurrentSimBecVal2, CurrentSimBecVal3));
                            //b_accuracy = getBecAccuracy(CurrentSimBecVal1, CurrentSimBecVal2, CurrentSimBecVal3);
                            TV4.setText("BecLatLong: " + Float.toString(b_lat) + " , " + Float.toString(b_long) + " , " + Float.toString(b_accuracy));
                            if ( localizationTHRSHLD > abs((int) (CurrentSimBecVal1+CurrentSimBecVal2+CurrentSimBecVal3))){
                                WhichStop = "north_science_hill_busstop";
                            }else if (240 <= abs((int) (CurrentSimBecVal1+CurrentSimBecVal2+CurrentSimBecVal3))){
                                WhichStop = "none";
                            }
                        }
                            if (proxcount == 0){
                                ProximityBecVal = -101;
                            } else{
                                ProximityBecVal = proxRSSIHolder/proxcount;
                            }
                            if (ProximityBecVal > proxTHRSHLD){
                                WhichStop = "south_science_hill_busstop";
                            }

                            if (atdestination == true){
                                WhichStop = "Destination";
                            }
                            atdestination = false;
                            //MyCartText.setText("x"+Double.toString(foundRadius*cos(Math.toRadians(foundDeg)))+"\n"
                            //       +"  y"+Double.toString(foundRadius*sin(Math.toRadians(foundDeg))) );
                            // how you set your ouput text displays for testing

//                            StringOfDataFinal += "" + ActualDeg.getText() + "," + ActualRad.getText() + "," + xSum + ","
//                                    + ySum + "," + MagTotal + "," + Integer.toString((int) (CurrentSimBecVal3 + CurrentSimBecVal2 + CurrentSimBecVal1)) + "\n";


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

    int modeChange = 0 ;
    public void infomodeHandler(View view){
        // North South Server
         modeChange = (modeChange+1)%3;
        if (modeChange == 0){
            infomodeButton.setText("North");
        }
        if (modeChange == 1){
            infomodeButton.setText("South");
        }
        if (modeChange == 2){
            infomodeButton.setText("Server");
        }
    }

    public void FakeBusHandler(View view){
        if (modeParam == "walking"){
            modeParam = "bus";
        } else if (modeParam == "bus") {
            modeParam = "walking";
        }
        fakeButton.setText(modeParam);
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
        muserstate = NoStop;
        transition = true;
        scanLeDevice(false);
        ColorLayout.setBackgroundColor(Color.WHITE);
        CorrectStop = -1;
        WhichStop = "none";

        for(llindex = 0; llindex < 10; llindex++){
            LastLocalBeacons [llindex] = 0;
        }

        int avgindex = 0;

        int beaconindex = 0;
        for(;beaconindex<BeaconCount;) {
            for (AverageRSSI[beaconindex] = 0; (BeaconVal[beaconindex][avgindex] != 1) && (avgindex < 20); ) {
                AverageRSSI[beaconindex] += BeaconVal[beaconindex][avgindex];
                avgindex++;
            }
            if (avgindex == 0) {// there were no read values
                if (beaconindex == 0)
                    //TV1.setText("avgxW1I:" + "none");
                if (beaconindex == 1)
                    //TV2.setText("avgUos6:" + "none");
                if (beaconindex == 2)
                   // TV3.setText("avgd2Rq:" + "none");
                if (beaconindex == 3)
                    //TV4.setText("avgpYEM:" + "none");
                AverageRSSI[beaconindex] = 0;
            } else {
                AverageRSSI[beaconindex] = AverageRSSI[beaconindex] / avgindex;
                if (beaconindex == 0)
                   // TV1.setText("avgxW1I:" + AverageRSSI[beaconindex]);
                if (beaconindex == 1)
                    //TV2.setText("avgUos6:" + AverageRSSI[beaconindex]);
                if (beaconindex == 2)
                  //  TV3.setText("avgd2Rq:" + AverageRSSI[beaconindex]);
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
        proxRSSIHolder = 0;
        proxcount = 0;
        fakeButton.setText(modeParam);
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        MyLatLongTextView.setText(String.format("%s: %f \n%s: %f", "LAT",
                mCurrentLocation.getLatitude(), "LON", mCurrentLocation.getLongitude()));

        Toast.makeText(this, "Location Updated",
                Toast.LENGTH_SHORT).show();

        HashMap<String, String> myparams = new HashMap<String, String>();
        myparams.put("traveler_id", "50965d1fa6e7625057c1615418819dc1b9f89139924646397b43969c9ac27a17");
        myparams.put("trip_instance_id", TripInstanceId.getText().toString());
        myparams.put("latitude", Double.toString(mCurrentLocation.getLatitude()));
        myparams.put("longitude", Double.toString(mCurrentLocation.getLongitude()));
        myparams.put("nearest", NearestParam);
        myparams.put("accuracy_dist", Integer.toString((int) mCurrentLocation.getAccuracy()));
        myparams.put("mode", modeParam);

        TV4.setText(Integer.toString((int) mCurrentLocation.getAccuracy()));
        int a;
        if (LastBusSeen != "none") {
            myparams.put("transit_name", LastBusSeen);
        }

        if (WhichStop.compareTo("none") != 0)
            myparams.put("stop_name", WhichStop);

        if (WhichStop.compareTo("north_science_hill_busstop") == 0) {
            b_lat = calculateAverage(b_lat_raw);
            b_lat_raw.clear();
            b_long = calculateAverage(b_long_raw);
            b_long_raw.clear();
            b_accuracy = calculateAverage(b_accuracy_raw);
            b_accuracy_raw.clear();
            if ((b_accuracy != 0)) {
                myparams.put("b_latitude", Float.toString(b_lat));
                myparams.put("b_longitude", Float.toString(b_long));
                myparams.put("b_accuracy_dist", Float.toString(b_accuracy));
            }
            //36.999976, -122.062330
            myparams.put("stop_latitude", "36.999976");
            myparams.put("stop_longitude", "-122.062330");
        } else if (WhichStop.compareTo("south_science_hill_busstop") == 0) {
            myparams.put("b_latitude", "36.999858");
            myparams.put("b_longitude", "-122.062170");
            myparams.put("b_accuracy_dist", "3");
            b_lat = (float) 36.999858;
            b_long = (float) -122.062170;
            b_accuracy = (float) 3;
            myparams.put("stop_latitude", Float.toString(b_lat));
            myparams.put("stop_longitude", Float.toString(b_long));
        }


        if ((WhichStop.compareTo("none") == 0) || b_accuracy == 0) {
            f_lat = mCurrentLocation.getLatitude();
            f_long = mCurrentLocation.getLongitude();
        } else {
            f_lat = (double) ((1 / ((pow((double) b_accuracy, -2)) + (pow(mCurrentLocation.getAccuracy(), -2)))) *
                    ((b_lat * (pow((double) b_accuracy, -2))) + (mCurrentLocation.getLatitude() * (pow(mCurrentLocation.getAccuracy(), -2)))));
            f_long = (double) ((1 / ((pow((double) b_accuracy, -2)) + (pow(mCurrentLocation.getAccuracy(), -2)))) *
                    ((b_long * (pow((double) b_accuracy, -2))) + (mCurrentLocation.getLongitude() * (pow(mCurrentLocation.getAccuracy(), -2)))));
        }

        myparams.put("f_latitude", Double.toString(f_lat));
        myparams.put("f_longitude", Double.toString(f_long));

//        StringOfDataFinal += "" + ActualDeg.getText() + "," + ActualRad.getText() + "," + f_lat + ","
//                + f_long + "," +WhichStop+"\n";
        /////////////////////////////STATE MACHINE/////////////////////////////////////////////////
        /* audio ref
        * NoStopSound = MediaPlayer.create(this, R.raw.continueforward);
        SouthStopSound = MediaPlayer.create(this, R.raw.wrongbusstop);
        BoardingZoneSound = MediaPlayer.create(this, R.raw.boardingzone);
        SeeBusSound = MediaPlayer.create(this, R.raw.seebus);
        SeeDestinationSound = MediaPlayer.create(this, R.raw.seedestination);
        BusStopCheck*/

        BasicAuth = "testing@aol.com" + ":" + "testing";
        BasicAuth = Base64.encodeToString(BasicAuth.getBytes(), Base64.DEFAULT);

        SimBecVal1.setText("-101");
        SimBecVal2.setText("-101");
        SimBecVal3.setText("-101");
        ProximityBecVal = -101;
        Log.d("ServerMSGERROR: ", (new JSONObject(myparams)).toString());
        StringOfDataFinal += "" + (new JSONObject(myparams)).toString() + "\n";

        JsonObjectRequest myjsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SendURL, new JSONObject(myparams),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject returnedObj) {
                        try {
                            Toast.makeText(MainActivity.this, "Response:" + returnedObj.toString(4), Toast.LENGTH_SHORT).show();
                            VolleyLog.v("Response:%n %s", returnedObj.toString(4));
                            Log.d("ServerMSGERROR: ", returnedObj.toString(4));
                            BusStopCheck = returnedObj.getString("response");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //state here
                        CorrectStop = CheckRightBusStop(BusStopCheck);
                        switch (muserstate) {
                            case NoStop:
                                if (transition == true) {
                                    NoStopSound.start();
                                }
                                if (CorrectStop == 0) {
                                    muserstate = WrongStop;
                                    transition = true;
                                } else if (CorrectStop == 1) {
                                    muserstate = DestinationStop;
                                    transition = true;
                                } else
                                    transition = false;
                                break;

                            case WrongStop:
                                if (transition == true) {
                                    WrongStopSound.start();
                                }
                                if (CorrectStop == 1) {
                                    muserstate = DestinationStop;
                                    transition = true;
                                } else
                                    transition = false;
                                break;

                            case DestinationStop:
                                if ((((f_lat < 36.999965) && (f_lat > 36.99990) && ((f_long < -122.06235) && (f_long > -122.062415))) && (CorrectStop == 1))
                                        || ((WhichStop.compareTo("south_science_hill_busstop") == 0) && (CorrectStop == 1))) {
                                    muserstate = BoardingZone;
                                    transition = true;
                                    BoardingZoneSound.start();
                                    break;
                                }
                                if (transition == true) {
                                    if (WhichStop == "north_science_hill_busstop") {
                                        CorrectNorth.start();
                                    }
                                    transition = false;
                                }
                                if (CorrectStop == 0) {
                                    WrongStopSound.start();
                                    muserstate = WrongStop;
                                    transition = true;
                                }
                                // out of bus stop
                                if (WhichStop.compareTo("none") == 0) {
                                    muserstate = NoStop;
                                    transition = false;
                                }
                                if (LastBusSeen != "none") {
                                    losingBus = 0;
                                    muserstate = OnBusCheck;
                                    transition = true;
                                }
                                // boarding zone coordinates: low 36.999904, -122.062415
                                // high : 36.999985, -122.06235

                                break;

                            case OnBusCheck:
                                // cannot see a bus anymore, we go back to on route
                                // checks the last 10 rssi readings
                                if (transition == true){
                                    SeeBusSound.start();
                                    transition = false;
                                }
                                localBecCount = 0;
                                transitBecCount = 0;
                                int nota;
                                for (nota = 0; nota < 10; nota++) {
                                    if (LastLocalBeacons[nota] == 1) {
                                        transitBecCount++;
                                    } else
                                        localBecCount++;
                                }
                                if (transitBecCount > 6) {
                                    muserstate = OnBus;
                                    transition = true;
                                    OnBusSound.start();
                                    losingBus = 0;
                                }
                                if (losingBus >2){
                                    muserstate = DestinationStop;
                                    transition = false;
                                }
                                if (LastBusSeen == "none") {
                                    losingBus++;
                                }else{
                                    losingBus = 0;
                                }
                                break;

                            case BoardingZone:
                                ColorLayout.setBackgroundColor(Color.YELLOW);
                                if (WhichStop == "north_science_hill_busstop")
                                    localizationTHRSHLD = 250;
                                if (WhichStop == "south_science_hill_busstop")
                                    proxTHRSHLD = -78;
                                if ((LastBusSeen != "none") && (transition == true)) {
                                    //play audio
                                    SeeBusSound.start();
                                    transition = false;
                                }
                                if (LastBusSeen != "none") {
                                    losingBus = 0;
                                    muserstate = OnBusCheck;
                                    transition = false;
                                }
                                if ((WhichStop == "none") && (LastBusSeen == "none")) {
                                    localizationTHRSHLD = 240;
                                    proxTHRSHLD = -72;
                                    muserstate = NoStop;
                                    transition = false;
                                    LeftBoardingZoneSound.start();
                                    ColorLayout.setBackgroundColor(Color.WHITE);
                                }
                                break;

                            case OnBus:
                                modeParam = "bus";
                                if ((WhichStop == "Destination") && (transition == true)) {
                                    //play audio
                                    SeeDestinationSound.start();
                                    transition = false;
                                }
                                if (losingBus >2){
                                    muserstate = OffBus;
                                    transition = false;
                                }
                                if (LastBusSeen == "none") {
                                    losingBus++;
                                }else{
                                    losingBus = 0;
                                }
                                break;

                            case OffBus:
                                modeParam = "walking";
                                ColorLayout.setBackgroundColor(Color.WHITE);
                                break;
                        }
                        TV3.setText(Integer.toString(muserstate) + LastLocalBeacons[0] + LastLocalBeacons[1] +
                                LastLocalBeacons[2] + LastLocalBeacons[3] + LastLocalBeacons[4] + LastLocalBeacons[5] +
                                LastLocalBeacons[6] + LastLocalBeacons[7] + LastLocalBeacons[8] + LastLocalBeacons[9]);
                        fakeButton.setText(modeParam);
                        LastBusSeen = "none";
                        onBusFlag = 0;
                        TV2.setText(WhichStop +"  " + CheckRightBusStop(BusStopCheck));
                        // end fsm
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                if (infomodeButton.getText().toString().compareTo("Server")!=0) {
                    CorrectStop = CheckRightBusStop(BusStopCheck);
                    switch (muserstate) {
                        case NoStop:
                            if (transition == true) {
                                NoStopSound.start();
                            }
                            if (CorrectStop == 0) {
                                muserstate = WrongStop;
                                transition = true;
                            } else if (CorrectStop == 1) {
                                muserstate = DestinationStop;
                                transition = true;
                            } else
                                transition = false;
                            break;

                        case WrongStop:
                            if (transition == true) {
                                WrongStopSound.start();
                            }
                            if (CorrectStop == 1) {
                                muserstate = DestinationStop;
                                transition = true;
                            } else
                                transition = false;
                            break;

                        case DestinationStop:
                            if ((((f_lat < 36.999965) && (f_lat > 36.99990) && ((f_long < -122.06235) && (f_long > -122.062415))) && (CorrectStop == 1))
                                    || ((WhichStop.compareTo("south_science_hill_busstop") == 0) && (CorrectStop == 1))) {
                                muserstate = BoardingZone;
                                transition = true;
                                BoardingZoneSound.start();
                                break;
                            }
                            if (transition == true) {
                                if (WhichStop == "north_science_hill_busstop") {
                                    CorrectNorth.start();
                                }
                                transition = false;
                            }
                            if (CorrectStop == 0) {
                                WrongStopSound.start();
                                muserstate = WrongStop;
                                transition = true;
                            }
                            // out of bus stop
                            if (WhichStop.compareTo("none") == 0) {
                                muserstate = NoStop;
                                transition = false;
                            }
                            if (LastBusSeen != "none") {
                                losingBus = 0;
                                muserstate = OnBusCheck;
                                transition = true;
                            }
                            // boarding zone coordinates: low 36.999904, -122.062415
                            // high : 36.999985, -122.06235

                            break;

                        case OnBusCheck:
                            // cannot see a bus anymore, we go back to on route
                            // checks the last 10 rssi readings
                            if (transition == true) {
                                SeeBusSound.start();
                                transition = false;
                            }
                            localBecCount = 0;
                            transitBecCount = 0;
                            int nota;
                            for (nota = 0; nota < 10; nota++) {
                                if (LastLocalBeacons[nota] == 1) {
                                    transitBecCount++;
                                } else
                                    localBecCount++;
                            }
                            if (transitBecCount > 6) {
                                muserstate = OnBus;
                                transition = true;
                                OnBusSound.start();
                                losingBus = 0;
                            }
                            if (losingBus > 2) {
                                muserstate = DestinationStop;
                                transition = false;
                            }
                            if (LastBusSeen == "none") {
                                losingBus++;
                            } else {
                                losingBus = 0;
                            }
                            break;

                        case BoardingZone:
                            ColorLayout.setBackgroundColor(Color.YELLOW);
                            if (WhichStop == "north_science_hill_busstop")
                                localizationTHRSHLD = 250;
                            if (WhichStop == "south_science_hill_busstop")
                                proxTHRSHLD = -77;
                            if ((LastBusSeen != "none") && (transition == true)) {
                                //play audio
                                SeeBusSound.start();
                                transition = false;
                            }
                            if (LastBusSeen != "none") {
                                losingBus = 0;
                                muserstate = OnBusCheck;
                                transition = false;
                            }
                            if ((WhichStop == "none") && (LastBusSeen == "none")) {
                                localizationTHRSHLD = 240;
                                proxTHRSHLD = -72;
                                muserstate = NoStop;
                                transition = false;
                                LeftBoardingZoneSound.start();
                                ColorLayout.setBackgroundColor(Color.WHITE);
                            }
                            break;

                        case OnBus:
                            modeParam = "bus";
                            if ((WhichStop == "Destination") && (transition == true)) {
                                //play audio
                                SeeDestinationSound.start();
                                transition = false;
                            }
                            if (losingBus > 2) {
                                muserstate = OffBus;
                                transition = false;
                            }
                            if (LastBusSeen == "none") {
                                losingBus++;
                            } else {
                                losingBus = 0;
                            }
                            break;

                        case OffBus:
                            modeParam = "walking";
                            ColorLayout.setBackgroundColor(Color.WHITE);
                            break;
                    }
                    TV3.setText(Integer.toString(muserstate) + LastLocalBeacons[0] + LastLocalBeacons[1] +
                            LastLocalBeacons[2] + LastLocalBeacons[3] + LastLocalBeacons[4] + LastLocalBeacons[5] +
                            LastLocalBeacons[6] + LastLocalBeacons[7] + LastLocalBeacons[8] + LastLocalBeacons[9]);
                    fakeButton.setText(modeParam);
                    LastBusSeen = "none";
                    onBusFlag = 0;
                    TV2.setText(WhichStop +"  " + CheckRightBusStop(BusStopCheck));
                    // end fsm
                }
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
            public String getBodyContentType() {
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

    public float calculateAverage(ArrayList<Float> rawval) {
        float sum = 0;
        int traverse = 0;
        if(!rawval.isEmpty()) {
            for (traverse = 0; traverse < rawval.size() ;traverse++) {
                sum += rawval.get(traverse);
            }
            return sum / rawval.size();
        }
        return sum;
    }
    public int CheckRightBusStop (String myresponse){
         String WhichSource = infomodeButton.getText().toString();
        if (WhichSource.compareTo("South") == 0){
            if (WhichStop.compareTo("south_science_hill_busstop") == 0) {
                return (1);
            }else if(WhichStop.compareTo("none") == 0){
                return (-1);
            }
            else {
                return (0);
            }
        }
        if (WhichSource.compareTo("North") == 0){
            if (WhichStop.compareTo("north_science_hill_busstop") == 0) {
                return (1);
            }else if(WhichStop.compareTo("none") == 0){
                return (-1);
            }
            else {
                return (0);
            }
        }
        if (WhichSource.compareTo("Server")==0) {
            if ((myresponse != null) && (myresponse.toCharArray()[0] == 'W') && (myresponse.toCharArray()[1] == 'a')) {
                return (1);
            } else if ((myresponse != null) && (myresponse.toCharArray()[0] == 'W') && (myresponse.toCharArray()[1] == 'r')) {
                return (0);
            }
            return (-1);
        }
        return (-1);
/*
        if ((SendURL != "https://routeme2app.mybluemix.net/api/check_location")) {
            if (WhichStop.compareTo("north_science_hill_busstop") == 0) {
                return (1);
            }else if(WhichStop.compareTo("none") == 0){
                return (-1);
            }
            else {
                return (0);
            }
        }else if((myresponse != null)&&(myresponse.toCharArray()[0] == 'W')&&(myresponse.toCharArray()[1] == 'a')){
            return  (1);
        }
        else if ((myresponse != null)&&(myresponse.toCharArray()[0] == 'W')&&(myresponse.toCharArray()[1] == 'r')){
            return  (0);
        }
        return (-1);
        */

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