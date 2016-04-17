package com.idea.mainactivityidea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.idea.mainactivityidea.activity.BaseActivity;
import com.idea.mainactivityidea.constants.AppConstants;
import com.idea.mainactivityidea.utils.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    int[] image_ids = { R.drawable.icon172/*, R.drawable.icon272, R.drawable.icon372,
            R.drawable.icon472, R.drawable.icon572, R.drawable.icon672*/ };
    String[] device_list = { "Table Lamp"/*, "Geyzer", "MicrowaveOwen", "Refrigerator", "Televison",
            "Personal Computer"*/};
    ListView devices_list;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Activity currentActivity;
    Devices_adapter adapter;
    boolean isRelayOn = false;
    boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentActivity = MainActivity.this;
        preferences = getSharedPreferences(AppConstants.KEY_APPLICATION_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        devices_list = (ListView)findViewById(R.id.Devices_list);

        String toggleVal = getIntent().getStringExtra(AppConstants.TOGGLE_VALUE);

        /*if (toggleVal != null) {
            isRelayOn = true;
        } else if (preferences.getBoolean(AppConstants.KEY_IS_RELAY_ON, false)) {
            isRelayOn = true;
        } else {
            isRelayOn = false;
        }
*/
        if (toggleVal != null) {
            if (toggleVal.equals(AppConstants.ON)) {
                isRelayOn = true;
                isEnabled = true;

            } else if (toggleVal.equals(AppConstants.OFF)) {
                isRelayOn = false;
                isEnabled = true;

            } else if (toggleVal.equals(AppConstants.NO_RESPONSE)) {
                isRelayOn = false;
                isEnabled = false;
            }

            adapter = new Devices_adapter(this, image_ids, device_list, isRelayOn, isEnabled);
            devices_list.setAdapter(adapter);

        } else {

            if (Validator.isNetworkAvailable(currentActivity)) {
                if (!preferences.getString(AppConstants.KEY_IP_ADDRESS, "").equals("")) {

                    ToggleDeviceStatus toggleStatus = new ToggleDeviceStatus();
                    toggleStatus.execute();
                } else {
                    adapter = new Devices_adapter(this, image_ids, device_list, false, false);
                    devices_list.setAdapter(adapter);
                }
            } else {
                adapter = new Devices_adapter(this, image_ids, device_list, false, false);
                devices_list.setAdapter(adapter);

                alert(currentActivity, "", getString(R.string.alert_no_network), getString(R.string.button_ok), getString(R.string.button_cancel), false, false);
            }
        }

        devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "item " + String.valueOf(position),
                        Toast.LENGTH_SHORT).show();
                devices_list.setSelection(position);
                devices_list.setItemChecked(position, true);
                view.setEnabled(true);
                view.setSelected(true);
            }
        });

        init();

    }

    @SuppressWarnings("ConstantConditions")
    private void init() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView titleSplashTv = (TextView) findViewById(R.id.titleTv);
        titleSplashTv.setText(titleSplashTv.getText().toString().toUpperCase());
        titleSplashTv.setTypeface(BaseApplication.getTypeFaceTitle());

        // buttons
        findViewById(R.id.settingsImBtn).setOnClickListener(this);
        findViewById(R.id.fab).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingsImBtn:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                //Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab:
                Snackbar.make(v, "Add", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                break;
        }
    }


    private class ToggleDeviceStatus extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog(currentActivity, getString(R.string.loading), getString(R.string.please_wait), false, false);
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

            if (strResult == null) {
                isEnabled = false;
                isRelayOn = false;

            } else if (strResult != null && strResult.equals(AppConstants.ON)) {
                //editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, true);
                isEnabled = true;
                isRelayOn = true;

            } else if (strResult != null && strResult.equals(AppConstants.ON)) {
                //editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, false);
                isEnabled = true;
                isRelayOn = false;
            }

            adapter = new Devices_adapter(currentActivity, image_ids, device_list, isRelayOn, isEnabled);
            devices_list.setAdapter(adapter);
            //editor.apply();
        }

    }


}
