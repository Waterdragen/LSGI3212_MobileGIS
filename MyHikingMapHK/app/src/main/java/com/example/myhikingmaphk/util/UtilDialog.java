package com.example.myhikingmaphk.util;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.myhikingmaphk.R;

public class UtilDialog {
    public static AlertDialog makeDialog(Context context, String title, String htmlContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        Button dismissButton = dialogView.findViewById(R.id.buttonLayoutDialogClose);
        dismissButton.setOnClickListener(v -> dialog.dismiss());
        TextView titleView = dialogView.findViewById(R.id.textLayoutDialogTitle);
        titleView.setText(title);
        TextView dialogContent = dialogView.findViewById(R.id.textLayoutDialogContext);
        dialogContent.setText(Html.fromHtml(htmlContent, FROM_HTML_MODE_LEGACY));
        return dialog;
    }

    public static AlertDialog makeYesOrNoDialog(Context context, String title, String message,
                                                 @Nullable Runnable yesAction,
                                                 @Nullable Runnable noAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (yesAction != null) {
                        yesAction.run();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    if (noAction != null) {
                        noAction.run();
                    }
                });
        return builder.create();
    }
}
