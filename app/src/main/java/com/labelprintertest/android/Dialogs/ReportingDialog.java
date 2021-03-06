package com.labelprintertest.android.Dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.labelprintertest.android.Activities.MainActivity;
import com.labelprintertest.android.Activities.SumByDayActivity;
import com.labelprintertest.android.Activities.SumByTableAvtivity;
import com.labelprintertest.android.Common.Common;
import com.labelprintertest.android.Common.LocalStorageManager;
import com.labelprintertest.android.DBManager.APIManager;
import com.labelprintertest.android.DBManager.DbHelper;
import com.labelprintertest.android.DBManager.Queries;
import com.labelprintertest.android.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.labelprintertest.android.Common.Common.cm;
import static com.labelprintertest.android.Common.Common.currentActivity;

public class ReportingDialog extends Dialog implements View.OnClickListener {

    private TextView dateTxt, infoTxt;
    private Button endCancelBtn, endByDayBtn, daySumSendBtn, daySumCancelBtn, daySumPrintBtn, SumPrintBtn;
    private Calendar nowDate;
    private boolean isByEndSum = false;
    private boolean isDaySumSend = false;
    boolean ischeckdialog = false;
    static boolean ischecksend = false;
    static boolean ischeckcancel = false;

    public ReportingDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.reporting_dialog);

        if(ischeckdialog == true){
            dismiss();
        }
        initUI();
    }

    private void initUI() {
        endByDayBtn = findViewById(R.id.endByDayBtn);
        endByDayBtn.setOnClickListener(this);
        endCancelBtn = findViewById(R.id.endCancelBtn);
        endCancelBtn.setOnClickListener(this);
        daySumSendBtn = findViewById(R.id.daySumSendBtn);
        daySumSendBtn.setOnClickListener(this);
        daySumCancelBtn = findViewById(R.id.daySumCancelBtn);
        daySumCancelBtn.setOnClickListener(this);
        daySumPrintBtn = findViewById(R.id.daySumPrintBtn);
        daySumPrintBtn.setOnClickListener(this);
        SumPrintBtn = findViewById(R.id.SumPrintBtn);
        SumPrintBtn.setOnClickListener(this);


        nowDate = Calendar.getInstance();
        dateTxt = findViewById(R.id.dateTxt);
        dateTxt.setOnClickListener(this);
        dateTxt.setText(nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DATE) + "日");

        infoTxt = findViewById(R.id.infoTxt);

        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());

        infoTxt.setText(Common.endInfoStr);

        SimpleDateFormat formatter= new SimpleDateFormat("HH");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));

        LocalStorageManager localStorageManager = new LocalStorageManager();

        if(localStorageManager.getSendData() != null){
            System.out.println(localStorageManager.getSendData());
            infoTxt.setText("締めデータ転送済み");
        }

        if(localStorageManager.getCancelData() != null){
            System.out.println(localStorageManager.getCancelData());
            infoTxt.setText("日次締済み締データ取消転送済み!");
        }

        if(localStorageManager.getEndDay() != null){
            System.out.println(localStorageManager.getEndDay());
            infoTxt.setText("日次締め済み");
        }

        if(ischecksend == true){
            ischeckcancel = false;
            infoTxt.setText("締めデータ転送済み");
        }

        if(ischeckcancel == true){
            ischecksend = false;
            infoTxt.setText("日次締済み締データ取消転送済み!");
        }

        if(Integer.parseInt(formatter.format(date)) > 23){
            localStorageManager.saveSendData(false);
            localStorageManager.saveCancelData(false);
            localStorageManager.saveEndDay(false);
            isByEndSum = !saveByDayEndData();
            ischeckcancel = false;
            ischecksend = false;
            infoTxt.setText("");
        }
    }

    /**
     * 日時を設定するデートピッカーを表示する
     */
    private void showDatePicker(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dlg = new DatePickerDialog(cm.currentActivity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(y, m, d);
                nowDate = calendar;
                dateTxt.setText(nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DATE) + "日");
            }
        }, year, month, day);
        dlg.show();
    }

    private void showInfo() {
        ischeckcancel =false;
        ischecksend = false;
        Date date = nowDate.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.JAPANESE);
        String dateStr = sdf.format(date);

        String byEndStr = " " + currentActivity.getResources().getString(R.string.btn_by_end_day) + currentActivity.getResources().getString(R.string.lb_completed);

        if (!isByEndSum) byEndStr = "";
        String daySendStr = " " + currentActivity.getResources().getString(R.string.btn_day_sum_send) + currentActivity.getResources().getString(R.string.lb_completed);
        if (!isDaySumSend) daySendStr = "";

        if (byEndStr.equals("") && daySendStr.equals("")) {
            Common.endInfoStr = "";
        }else {
            Common.endInfoStr = dateStr + byEndStr + daySendStr;
        }
        infoTxt.setText(Common.endInfoStr);
    }

    private boolean saveByDayEndData () {
        boolean isDone = true;
        DbHelper dbHelper = new DbHelper(currentActivity);
        Queries query = new Queries(null, dbHelper);
        isDone = query.saveByDayEndData(nowDate);
        return isDone;
    }

    private boolean sendDayEndSumData (){

        boolean isDone = true;
        try{
            APIManager apiManager = new APIManager();
            if (cm.hasInternetConnection() && apiManager.connectionclass() != null) {
                APIManager manager = new APIManager();
                isDone = manager.syncToServer(nowDate);
                if (isDone) {
                    LocalStorageManager localStorageManager = new LocalStorageManager();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.JAPANESE);
                    localStorageManager.saveLastSyncDate(sdf.format(Calendar.getInstance().getTime()));
                }
            }
            else {
                isDone = false;
            }
        }catch (Exception e){
            AlertDialog alertDialog = new AlertDialog.Builder(getOwnerActivity()).create();
            alertDialog.setTitle("sendDayEndSumData");
            alertDialog.setMessage("The is error :" + e.getMessage());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        return isDone;
    }

    private boolean cancelDayEndSumData () {
        boolean isDone = true;
        APIManager manager = new APIManager();
        if (cm.hasInternetConnection() && manager.connectionclass() != null) {
            isDone = manager.unSyncToServer(nowDate);
        }else {
            isDone = false;
        }
        return isDone;
    }

    private boolean deleteByDayEndData () {
        boolean isDone = true;
        DbHelper dbHelper = new DbHelper(currentActivity);
        Queries query = new Queries(null, dbHelper);
        isDone = query.deleteByDayEndData(nowDate);
        return isDone;
    }

    @Override
    public void onClick(final View v) {
        v.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                v.setEnabled(true);
            }
        }, 1000L);

        switch (v.getId()) {
            case R.id.endCancelBtn:
                isByEndSum = !deleteByDayEndData();
                showInfo();
                break;
            case R.id.endByDayBtn:
                isByEndSum = saveByDayEndData();
                LocalStorageManager localStorageManager = new LocalStorageManager();
                localStorageManager.saveEndDay(true);
                localStorageManager.saveSendData(false);
                localStorageManager.saveCancelData(false);
                showInfo();
                break;
            case R.id.daySumSendBtn:
                isDaySumSend = sendDayEndSumData();
                LocalStorageManager localStorageManagersend = new LocalStorageManager();
                localStorageManagersend.saveSendData(true);
                localStorageManagersend.saveEndDay(false);
                localStorageManagersend.saveCancelData(false);
                ischeckcancel =false;
                ischecksend = true;
                infoTxt.setText("締めデータ転送済み");
                break;
            case R.id.daySumCancelBtn:
                isDaySumSend = !cancelDayEndSumData();
                LocalStorageManager localStorageManagercancel = new LocalStorageManager();
                localStorageManagercancel.saveCancelData(true);
                localStorageManagercancel.saveSendData(false);
                localStorageManagercancel.saveEndDay(false);
                ischecksend = false;
                ischeckcancel =true;
                infoTxt.setText("日次締済み締データ取消転送済み!");
                break;
            case R.id.daySumPrintBtn:
                Intent intent = new Intent(currentActivity, SumByDayActivity.class);
                nowDate = Calendar.getInstance();
                intent.putExtra("date", nowDate);
                currentActivity.startActivity(intent);
                dismiss();
                break;
            case R.id.dateTxt:
                showDatePicker(nowDate);
                break;
//            case R.id.daySumPrintBtn:
//                Intent sumintent = new Intent(currentActivity, SumByTableAvtivity.class);
//                sumintent.putExtra("date", nowDate);
//                currentActivity.startActivity(sumintent);
//                dismiss();
//                break;
        }
    }
}
