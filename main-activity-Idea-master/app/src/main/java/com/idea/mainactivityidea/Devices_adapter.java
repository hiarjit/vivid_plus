package com.idea.mainactivityidea;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.idea.mainactivityidea.constants.AppConstants;
import com.idea.mainactivityidea.utils.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Devices_adapter extends ArrayAdapter<String> {

    Activity context;
    int[] image_ids;
    String[] device_list;
    ToggleButton toggleBtn;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean isOn;
    boolean isEnabled;
    boolean bolStatus;
    ProgressDialog pDialog;

    public Devices_adapter(Activity context, int[] resource,String[] device_name, boolean isOn, boolean isEnabled) {
        super(context,R.layout.custom_list,device_name);
        this.device_list = device_name;
        this.context = context;
        this.image_ids = resource;
        this.isOn = isOn;
        this.isEnabled = isEnabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View single_row = inflater.inflate(R.layout.custom_list, null, true);

        preferences = context.getSharedPreferences(AppConstants.KEY_APPLICATION_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        final String textCont = device_list[position];

        TextView textView =(TextView)single_row.findViewById(R.id.device_list);
        ImageView indivisual_settings = (ImageView) single_row.findViewById(R.id.indivisual_settings);

        toggleBtn = (ToggleButton) single_row.findViewById(R.id.switch_btn);

        if (isOn) {
            toggleBtn.setChecked(true);
        }

        if (isEnabled) {
            toggleBtn.setEnabled(true);
        } else {
            toggleBtn.setEnabled(false);
        }

        textView.setText(device_list[position]); // .toString().toUpperCase()
        textView.setTypeface(BaseApplication.getTypeFaceRegular());
        // imageView.setImageResource(image_ids[position]);

        toggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (Validator.isNetworkAvailable(context)) {
                    if (!preferences.getString(AppConstants.KEY_IP_ADDRESS, "").equals("")) {
                        bolStatus = toggleBtn.isChecked();

                        /*if (bolStatus) {
                            editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, true);
                        } else {
                            editor.putBoolean(AppConstants.KEY_IS_RELAY_ON, false);
                        }
                        editor.apply();*/
                        ToggleDeviceStatus deviceStatus = new ToggleDeviceStatus();
                        deviceStatus.execute(bolStatus);

                    } else {
                        alert(context, "", context.getString(R.string.alert_ip_address_check), context.getString(R.string.button_ok), context.getString(R.string.button_cancel), false, false);
                    }
                } else {
                    alert(context, "", context.getString(R.string.alert_no_network), context.getString(R.string.button_ok), context.getString(R.string.button_cancel), false, false);
                }
            }
        });

        indivisual_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "settings " + textCont, Toast.LENGTH_SHORT).show();
            }
        });
        return single_row;
    }


    private class ToggleDeviceStatus extends AsyncTask<Boolean, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog(context, context.getString(R.string.loading), context.getString(R.string.please_wait), false, false);
        }

        @Override
        protected Boolean doInBackground(Boolean... objs) {
            try {

                String strUrl = AppConstants.HTTP + preferences.getString(AppConstants.KEY_IP_ADDRESS, "") + ":"
                        + preferences.getString(AppConstants.KEY_PORTNO, AppConstants.PORT_NO) + "/";
               if (bolStatus) {
                   strUrl += AppConstants.KEY_API_RELAY_ON;
               } else {
                   strUrl += AppConstants.KEY_API_RELAY_OFF;
               }

                URL url = new URL(strUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int code = urlConnection.getResponseCode();
                if (code == 200) {
                    return true;
                }

                /*StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));

                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();*/

            } catch (final Exception e) {
                e.printStackTrace();
                Log.e("ERROR_TOGGLE_DEVICE", e.toString());

                context.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context.getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                });

                return false;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean bolResult) {
            super.onPostExecute(bolResult);
            cancelProgressDialog();

            if (bolResult) {
                if (bolStatus) {
                    Toast.makeText(context, "Relay = ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Relay = OFF ", Toast.LENGTH_SHORT).show();
                }

            } else{
                toggleBtn.toggle();
            }
        }
    }

    public void progressDialog(Context context, String title, String message, boolean cancelable, boolean isTitle) {
        pDialog = new ProgressDialog(context);

        if (isTitle) {
            pDialog.setTitle(title);
        }

        pDialog.setMessage(message);
        if (!cancelable) {
            pDialog.setCancelable(false);
        }

        pDialog.show();
    }

    public void cancelProgressDialog() {
        pDialog.cancel();
    }

    public void alert(Context context, String title, String message, String positivebutton, String negativeButton, boolean isNegativeButton, boolean isTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (isTitle) {
            builder.setTitle(title);
        }

        builder.setMessage(message);
        builder.setPositiveButton(positivebutton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (isNegativeButton) {
            builder.setNegativeButton(negativeButton, null);
        }

        builder.show();
    }

}
