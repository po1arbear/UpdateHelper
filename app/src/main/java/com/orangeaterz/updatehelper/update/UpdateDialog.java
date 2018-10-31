package com.orangeaterz.updatehelper.update;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.orangeaterz.updatehelper.R;


public class UpdateDialog extends Dialog {

    private Callback callback;
    private TextView content;

    public UpdateDialog(Context context, Callback callback) {
        super(context, R.style.UpdateDialog);
        this.callback = callback;
        setCustomDialog();
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update, null);
        TextView tvConfirm = mView.findViewById(R.id.tv_confirm);
        TextView tvCancel = mView.findViewById(R.id.tv_cancel);
        content = mView.findViewById(R.id.dialog_confirm_title);

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callback(UpdateHelper.CONFIRM);
                UpdateDialog.this.cancel();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callback(UpdateHelper.CANCEL);
            }
        });
        super.setContentView(mView);
    }


    public UpdateDialog setContent(String s) {
        content.setText(s);
        return this;
    }


}
