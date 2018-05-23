package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import org.java_websocket.client.WebSocketClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
/**
 * This is a sample of simple iBeacon and Eddystone foreground scanning.
 */
public class BeaconEddystoneScanActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, BeaconEddystoneScanActivity.class);
  }

  public static final String TAG = "ProximityManager";
    private WebSocketClient mWebSocketClient;

  private ProximityManager proximityManager;
  private ProgressBar progressBar;
  private Boolean isConnected = true;
  private Socket mSocket;
  private static final String URL = "https://websocket-server-2018.herokuapp.com/";
  TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_beacon_eddystone_scan);
    progressBar = (ProgressBar) findViewById(R.id.scanning_progress);
    textView = findViewById(R.id.asd);
    //Setup Toolbar
    setupToolbar();

    //Setup buttons
    setupButtons();

    //Initialize and configure proximity manager
    setupProximityManager();
    //connectWebSocket();
      mSocket = new SocketConnection().getSocket();
      mSocket.on(Socket.EVENT_CONNECT,onConnect);
      mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
      mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
      mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
     // mSocket.on("new message", onNewMessage);
      mSocket.on("beaconData",onNewMessage);
      mSocket.connect();
      //connectWebSocket();
  }


  private void setupToolbar() {
    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupButtons() {
    Button startScanButton = (Button) findViewById(R.id.start_scan_button);
    Button stopScanButton = (Button) findViewById(R.id.stop_scan_button);
    startScanButton.setOnClickListener(this);
    stopScanButton.setOnClickListener(this);
  }

  private void setupProximityManager() {
    proximityManager = ProximityManagerFactory.create(this);

    //Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED)
        //OnDeviceUpdate callback will be received with 5 seconds interval
        .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(1));

    //Setting up iBeacon and Eddystone listeners
    proximityManager.setIBeaconListener(createIBeaconListener());
    proximityManager.setEddystoneListener(createEddystoneListener());
  }

  private void startScanning() {
    //Connect to scanning service and start scanning when ready
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        //Check if proximity manager is already scanning
        if (proximityManager.isScanning()) {
          Toast.makeText(BeaconEddystoneScanActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
          return;
        }
        proximityManager.startScanning();
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(BeaconEddystoneScanActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void stopScanning() {
    //Stop scanning if scanning is in progress
    if (proximityManager.isScanning()) {
      proximityManager.stopScanning();
      progressBar.setVisibility(View.GONE);
      Toast.makeText(this, "Scanning stopped", Toast.LENGTH_SHORT).show();
    }
  }

  private IBeaconListener createIBeaconListener() {
    return new IBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
        Log.i(TAG, "onIBeaconDiscovered: " + iBeacon.toString());
      }

      @Override
      public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
        Log.i(TAG, "onIBeaconsUpdated: " + iBeacons.size());
        Log.i(TAG, "Beacon to string:"+ iBeacons.toString());
        Gson gson = new Gson();
        String objJSON = "";
        for (int i =0; i<iBeacons.size();i++){
          Log.i("BeaconInfo",
                  " Adress: "+ iBeacons.get(i).getAddress() +
                  " UniqueID: "+ iBeacons.get(i).getUniqueId()+
                  " Distace: "+ iBeacons.get(i).getDistance()+
                  " TimeStamp: "+ iBeacons.get(i).getTimestamp());
//          sendMessage(" Adress: "+ iBeacons.get(i).getAddress() +
//                  " UniqueID: "+ iBeacons.get(i).getUniqueId()+
//                  " Distace: "+ iBeacons.get(i).getDistance()+
//                  " TimeStamp: "+ iBeacons.get(i).getTimestamp());
//        objJSON = gson.toJson(iBeacons.get(i));
//       // objJSON +=iBeacons.get(i).getRssi();
//            final String finalObjJSON = objJSON;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mSocket.emit("message"  ,""+ finalObjJSON);
//                }
//            });
//
//            Toast.makeText(getApplicationContext(),
//                    "Message ", Toast.LENGTH_LONG).show();

        }
//        String text = ""+iBeacons.get(0).getAddress()+ " "+iBeacons.get(0).getUniqueId()+ " "+ iBeacons.get(0).getDistance()+"\n" +
//                ""+iBeacons.get(1).getAddress()+ " "+iBeacons.get(1).getUniqueId()+ " "+ iBeacons.get(1).getDistance()+"\n" +
//                ""+iBeacons.get(2).getAddress()+ " "+iBeacons.get(2).getUniqueId()+ " "+ iBeacons.get(2).getDistance()+"\n";
      //  textView.append(text);
        objJSON = gson.toJson(iBeacons);
          // objJSON +=iBeacons.get(i).getRssi();
          final String finalObjJSON = objJSON;
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  mSocket.emit("message"  ,""+ finalObjJSON);
              }
          });

          Toast.makeText(getApplicationContext(),
                  "Message ", Toast.LENGTH_LONG).show();


      }

      @Override
      public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
        Log.e(TAG, "onIBeaconLost: " + iBeacon.toString());
      }
    };
  }
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {

                        Toast.makeText(getApplicationContext(),
                                "Connected", Toast.LENGTH_LONG).show();
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "diconnected");
                    isConnected = false;
                    Toast.makeText(getApplicationContext(),
                            "Disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Error connecting");
                    Toast.makeText(getApplicationContext(),
                            "Error", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
 /*   private  Emitter.Listener sendMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    };*/
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //JSONObject data = (JSONObject) args[0];



                    try {

                        Object a = args[0];
                        Log.i("Output",""+a.toString());

                    } catch (Exception e) {
                        Log.e("err",e.getMessage());
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                }
            });
        }
    };

  private EddystoneListener createEddystoneListener() {
    return new EddystoneListener() {
      @Override
      public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
        Log.i(TAG, "onEddystoneDiscovered: " + eddystone.toString());
      }

      @Override
      public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
        Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
      }

      @Override
      public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
        Log.e(TAG, "onEddystoneLost: " + eddystone.toString());
      }
    };
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start_scan_button:
        startScanning();
        break;
      case R.id.stop_scan_button:
        stopScanning();
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onStop() {
    //Stop scanning when leaving screen.
    stopScanning();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    //Remember to disconnect when finished.
    proximityManager.disconnect();
    super.onDestroy();
  }
  /*  private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("wss://websocket-server-2018.herokuapp.com/socket.io/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message) {

        mWebSocketClient.send(message);

    }*/
}
