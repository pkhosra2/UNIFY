package com.example.pouya_000.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "ad3449decada470995ccc4b3b8ac7a4f";

    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "http://example.com/callback/";

    private Player mPlayer;
    private Button connect_button;
    private volatile Socket s = null;
    private String accessToken;

    // Creating the socket to communicate with Python Server

    private static final int SERVERPORT = 5200;
    private static final String SERVER_IP = "127.0.0.1";

    // Request code that will be used to verify if the result comes from correct activity
// Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setShowDialog(true);

        builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read", "user-read-playback-state","user-read-currently-playing"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                accessToken = response.getAccessToken();
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        final MainActivity that = this;

        connect_button = (Button)findViewById(R.id.connect_button);
        connect_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = " http://4f1bd93d.ngrok.io/employees/" + accessToken;
                Log.i("url",url);
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i("Response", "Response is: " + response);
                                Intent intent  = new Intent(getApplicationContext(),UserHomeScreen.class);
                                startActivity(intent);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Response", "That didn't work!"+ error.toString());
                    }
                });

                // Add the request to the RequestQueue.
                HttpSingleton.getInstance(that).addToRequestQueue(stringRequest);
            }
        });

//        connect_button = (Button)findViewById(R.id.connect_button);
//        connect_button.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                Intent intent  = new Intent(getApplicationContext(),UserHomeScreen.class);
//                startActivity(intent);
//
//                Thread thread = new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        try {
//                            Socket s = new Socket(SERVER_IP, SERVERPORT);
//                            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
//                            //send output msg
//                            String outMsg = CLIENT_ID;
//                            out.write(outMsg);
//                            out.flush();
//
//                            Log.i("TcpClient", "sent: " + outMsg);
//                            //accept server response
//                            String inMsg = in.readLine() + System.getProperty("line.separator");
//                            Log.i("TcpClient", "received: " + inMsg);
//                            //close connection
//                            s.close();
//                        } catch (UnknownHostException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                thread.start();
//
//            }
//        });
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
}