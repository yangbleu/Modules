package com.bleu_autom.modules.Controller;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bleu_autom.modules.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by geoy on 2016/11/21.
 */
public class DateTimePickerDialog {

    private final int DIALOG_TIME = R.layout.vr_dialog_view6_time;
    private final int[] BTN_COLOR = {R.color.bottomBar_background, R.color.basic_gray};

    private Context context;
    private AlertDialog dialog;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private OnDateTimeSetListener onDateTimeSetListener;
    private TextView txtDate, txtTime, txtShowDate, txtShowTime;
    private Button btnOk, btnRemove;
    private RelativeLayout dialog_actionbar;
    private TextView titleTv, finishTv;
    private ImageView cancelIv;
    private int year, month,dayOfMonth;
    private int hourOfDay, minute;
    private Integer actionbarColor;

    public DateTimePickerDialog(@NonNull Context context, @Nullable OnDateTimeSetListener listener,
                                Calendar initC, @Nullable Integer actionbarColor){
        this.context = context;
        this.onDateTimeSetListener = listener;
        this.year = initC.get(Calendar.YEAR);
        this.month = initC.get(Calendar.MONTH);
        this.dayOfMonth = initC.get(Calendar.DAY_OF_MONTH);
        this.hourOfDay = initC.get(Calendar.HOUR_OF_DAY);
        this.minute = initC.get(Calendar.MINUTE);
        this.actionbarColor = actionbarColor;
        initDateTimeDialog();
    }

    private View initDateTimeDialog(){
        //get layout
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(DIALOG_TIME, null);

        //Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialog = builder.setView(view).create();

        //UI
        dialog_actionbar = (RelativeLayout) view.findViewById(R.id.dialog_view6_time_actionbar);
        if(actionbarColor!=null) dialog_actionbar.setBackgroundColor(actionbarColor);
        titleTv = (TextView) view.findViewById(R.id.dialog_view6_time_Title);
        cancelIv = (ImageView) view.findViewById(R.id.dialog_view6_time_actionBack);
        cancelIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) dialog.dismiss();
            }
        });

        txtDate = (TextView)view.findViewById(R.id.dialog_view6_time_txtDate);
        txtTime = (TextView)view.findViewById(R.id.dialog_view6_time_txtTime);
        txtShowDate = (TextView)view.findViewById(R.id.dialog_view6_time_txtShowDate);
        txtShowTime = (TextView)view.findViewById(R.id.dialog_view6_time_txtShowTime);

        final Button btnOk = (Button)view.findViewById(R.id.dialog_view6_time_btnOk);
        final Button btnRemove = (Button)view.findViewById(R.id.dialog_view6_time_btnRemove);

        datePicker = (DatePicker)view.findViewById(R.id.dialog_view6_time_datePicker);
        timePicker = (TimePicker)view.findViewById(R.id.dialog_view6_time_timePicker);
        timePicker.setIs24HourView(true);

        //initLocation date && time
        txtShowDate.setText(String.format(Locale.getDefault(), "%04d/%02d/%02d", year, (month + 1),
                dayOfMonth));
        txtShowTime.setText(String.format(Locale.getDefault(), "%2d:%2d", hourOfDay, minute));
        datePicker.init(year, month, dayOfMonth, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year1, int monthOfYear1, int dayOfMonth1) {
                year = year1;
                month = monthOfYear1;
                dayOfMonth = dayOfMonth1;
                String showDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year, (month + 1),
                        dayOfMonth);
                txtShowDate.setText(showDate);
            }
        });
        timePicker.setCurrentHour(hourOfDay);
        timePicker.setCurrentMinute(minute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay1, int minute1) {
                hourOfDay = hourOfDay1;
                minute = minute1;
                String showTime = String.format(Locale.getDefault(), "%2d:%2d", hourOfDay, minute);
                txtShowTime.setText(showTime);
            }
        });

        //切換：日期(2016-08-26)
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtDate.setBackgroundResource(BTN_COLOR[1]);
                txtTime.setBackgroundResource(BTN_COLOR[0]);
                txtDate.setTextColor(Color.WHITE);
                txtTime.setTextColor(Color.BLACK);
                datePicker.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.INVISIBLE);
            }
        });

        //切換：時間(23:59:59)
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtDate.setBackgroundResource(BTN_COLOR[0]);
                txtTime.setBackgroundResource(BTN_COLOR[1]);
                txtDate.setTextColor(Color.BLACK);
                txtTime.setTextColor(Color.WHITE);
                datePicker.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
            }
        });

        //確定
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onDateTimeSetListener != null){
                    onDateTimeSetListener.OnDateTimeSet( new GregorianCalendar(year, month,dayOfMonth,hourOfDay,minute) );
                }
                dismiss();
            }
        });

        //清除
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDateTimeSetListener != null){
                    onDateTimeSetListener.OnDateTimeSet(null);
                }
                dismiss();
            }
        });



        return view;
    }

    public void show(){
        dialog.show();
    }
    public void hideTime(boolean isHideTime){
        txtTime.setVisibility(isHideTime? View.GONE: View.VISIBLE);
        txtShowTime.setVisibility(isHideTime? View.GONE: View.VISIBLE);
    }
    public void setTitle(String title){
        titleTv.setText(title);
    }
    public void dismiss(){
        dialog.dismiss();
        dialog.cancel();
        dialog = null;
    }

    public void setMinDate(long minDate){
        datePicker.setMinDate(minDate);
        datePicker.invalidate();
    }

    public interface OnDateTimeSetListener {
        void OnDateTimeSet(@Nullable Calendar calendar);
    }

}

