package com.dojo.eightyone;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hidetchi on 2015/06/09.
 */
public class SocketClient {

    interface Callback {
        void handleSocket(int kind, String message);
    }

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_LOGGED_IN = 2;
    private static final int STATE_GAME = 3;
    public static final int CONNECTED = 0;
    public static final int LOGGED_IN = 1;

    private String _myLoginName;
    private Callback _callback;
    private String _host = "81dojo.com";
    private int _port = 4081;
    private Socket _socket = null;
    private BufferedReader _in;
    private PrintWriter _out;
    private boolean _kill = false;
    private int _state = STATE_DISCONNECTED;


    public SocketClient(Callback callback, String host, int port) {
        _callback = callback;
        _host = host;
        _port = port;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Start socket connection
                try {
                    _socket = new Socket(_host, _port);
                    _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                    _out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream())), true);
                } catch(UnknownHostException e) {
                    Log.d("ERROR", "Host not found.");
                    return;
                } catch(IOException e) {
                    Log.e("ERROR", e.getMessage());
                    return;
                }

                // Read socket loop
                try {
                    while (!_kill) {
                        String line = _in.readLine();
                        if (line != null) {
                            Log.d("TRACE", line);
                            _handleSocket(line);
                        }
                    }
                } catch(IOException e) {
                    Log.e("ERROR", e.getMessage());
                    _kill = true;
                }
            }
        }).start();
    }

    public void setMyLoginName(String str) {
        _myLoginName = str;
    }

    private void _handleSocket(String line) {
        if (_state == STATE_DISCONNECTED) {
            if (Pattern.compile("cross-domain-policy").matcher(line).find()) {
                _state = STATE_CONNECTED;
                _callback.handleSocket(CONNECTED, "OK");
            }
        } else if (_state == STATE_CONNECTED) {
            Matcher m = Pattern.compile("LOGIN:(.+) OK$").matcher(line);
            if (m.find()) {
                _state = STATE_LOGGED_IN;
                _callback.handleSocket(LOGGED_IN, m.group(1));
            }

        }
    }

    public void login(String name, String password) {
        _send("LOGIN " + name + " " + password + " x1");
    }

    private void _send(String str) {
        _out.println(str);
    }

}
