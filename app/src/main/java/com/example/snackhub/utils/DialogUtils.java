package com.example.snackhub.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.snackhub.R;

public class DialogUtils {

    private static AlertDialog loadingDialog;

    public static void showLoadingDialog(Context context, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        TextView messageTextView = dialogView.findViewById(R.id.loadingMessage);
        messageTextView.setText(message);

        loadingDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        loadingDialog.show();
    }

    public static void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public static void updateLoadingMessage(Context context, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.setMessage(message);
        } else {
            showLoadingDialog(context, message);  // Si le dialog n'est pas déjà visible, on le montre avec le nouveau message
        }
    }
}
