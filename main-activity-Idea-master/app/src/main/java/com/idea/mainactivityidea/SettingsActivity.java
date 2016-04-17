package com.idea.mainactivityidea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.idea.mainactivityidea.activity.BaseActivity;
import com.idea.mainactivityidea.constants.AppConstants;
import com.idea.mainactivityidea.utils.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    EditText editIPAddress;
    EditText editPortNo;
    Button btnOk;
    Activity currentActivity;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        currentActivity = SettingsActivity.this;

        preferences = getSharedPreferences(AppConstants.KEY_APPLICATION_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        editIPAddress = (EditText) findViewById(R.id.editIPAddress);
        editPortNo = (EditText) findViewById(R.id.editPortNo);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        editIPAddress.setText(preferences.getString(AppConstants.KEY_IP_ADDRESS, ""));
        editPortNo.setText(preferences.getString(AppConstants.KEY_PORTNO, "8266"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_settings, menu);
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
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnOk: {

                if (Validator.isNetworkAvailable(SettingsActivity.this)) {
                    if (Validator.isValidIPAddress(editIPAddress.getText().toString())) {
                        editor.putString(AppConstants.KEY_IP_ADDRESS, editIPAddress.getText().toString());
                        editor.putString(AppConstants.KEY_PORTNO, editPortNo.getText().toString());
                        editor.apply();

                        ToggleDeviceStatus toggleStatus = new ToggleDeviceStatus();
                        toggleStatus.execute();
                    } else {
                        alert(currentActivity, "", getString(R.string.alert_invalid_ip), getString(R.string.button_ok), getString(R.string.button_cancel), false, false);
                    }
                } else {
                    alert(currentActivity, "", getString(R.string.alert_no_network), getString(R.string.button_ok), getString(R.string.button_cancel), false, false);
                }

                break;
            }
        }
    }

    private class ToggleDeviceStatus extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog(SettingsActivity.this, getString(R.string.loading), getString(R.string.please_wait), false, false);
        }

        @Override
        protected String doInBackground(String... objs) {
            try {
                String strUrl = AppConstants.HTTP + preferences.getString(AppConstants.KEY_IP_ADDRESS, "") + ":"
                        + preferences.getString(AppConstants.KEY_PORTNO, AppConstants.PORT_NO) + "/";

                URL url = new URL(strUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));

                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                if (sb.indexOf("Relay is ON") >= 0) {
                    return AppConstants.ON;
                } else if (sb.indexOf("Relay is OFF") >= 0) {
                    return AppConstants.OFF;
                }

            } catch (final Exception e) {
                e.printStackTrace();
                Log.e("ERROR_TOGGLE_DEVICE", e.toString());

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String strResult) {
            super.onPostExecute(strResult);
            cancelProgressDialog();

            Intent i = new Intent(currentActivity, MainActivity.class);

            if (strResult == null) {
                i.putExtra(AppConstants.TOGGLE_VALUE, AppConstants.NO_RESPONSE);

            } else if (strResult.equals(AppConstants.ON)) {
                //editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, true);
                i.putExtra(AppConstants.TOGGLE_VALUE, AppConstants.ON);

            } else if (strResult.equals(AppConstants.OFF)) {
                //editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, false);
                i.putExtra(AppConstants.TOGGLE_VALUE, AppConstants.OFF);
            }

            //editor.apply();
            startActivity(i);
        }
    }

}
