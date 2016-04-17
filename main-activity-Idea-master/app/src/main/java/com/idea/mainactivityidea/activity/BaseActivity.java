package com.idea.mainactivityidea.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    ProgressDialog pDialog;

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
