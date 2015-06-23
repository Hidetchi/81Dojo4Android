package com.dojo.eightyone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class MainActivity extends ActionBarActivity implements SocketClient.Callback {

    private SocketClient _client;
    private Button _loginButton;
    private EditText _inputLoginName;
    private EditText _inputPassword;
    private TextView _textloginAlert;
    private ViewFlipper _mainViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start socket service
        _client = new SocketClient(this, "192.168.50.150", 4081);

        _loginButton = (Button)findViewById(R.id.loginButton);
        _loginButton.setOnClickListener(_handleLoginButton);
        _inputLoginName = (EditText)findViewById(R.id.inputLoginName);
        _inputPassword = (EditText)findViewById(R.id.inputPassword);
        _textloginAlert = (TextView)findViewById(R.id.textLoginAlert);
        _mainViewFlipper = (ViewFlipper)findViewById(R.id.mainViewFlipper);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleSocket(final int kind, final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (kind) {
                    case SocketClient.CONNECTED:
                        _loginButton.setEnabled(true);
                        _textloginAlert.setText("ログインできます");
                        break;
                    case SocketClient.LOGGED_IN:
                        String[] tokens = message.split(":", -1);
                        _client.setMyLoginName(tokens[0]);
                        _mainViewFlipper.setDisplayedChild(1);
                        break;
                }
            }
        });
    }

    View.OnClickListener _handleLoginButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            _client.login(_inputLoginName.getText().toString(), _inputPassword.getText().toString());
        }
    };
}
