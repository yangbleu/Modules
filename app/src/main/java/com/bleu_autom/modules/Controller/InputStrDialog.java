package com.bleu_autom.modules.Controller;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bleu_autom.modules.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * Created by george on 2016/6/29.
 */
public class InputStrDialog implements View.OnClickListener{
    private static final String LOG_TAG = "InputStrDialog";
    public static final int TYPE_GENDER = 3331;
    public static final int TYPE_DATE_PICKER = 3332;
    public static final int TYPE_DATE_RANGE = 3333;
    public static final int TYPE_DATE_TIME_PICKER = 3334;
    public static final int TYPE_DATE_TIME_RANGE = 3335;

    //UI
    private Context context;
    private OnDialogInteractionListener mListener;
    private int vid;
    private AlertDialog dialog;
    private RelativeLayout input_str_dialog_actionbar;
    private TextView titleTv, finishTv;
    private ImageView cancelIv;
    private EditText inputEt;
    private EditText input_str_dialog_sir_maxpeople;

    //Value
    private int inputType, selecteditemId;
    private Integer actionbarColor;
    private Integer limit=null;
    private String tag="";
    private String oldStr="";
    private final String[] targetOptions = {"限本里里民","限桃園居民","不限"};
    private List<String> personLimitList;
    private static final List<String> personLimitListsir = new ArrayList<>();

    static {
        personLimitListsir.add("不限");
        personLimitListsir.add("限制");
    }
    private void initpersonLimitList(Integer limit){
        personLimitList = new ArrayList<>();
        for (int i = 0; i <= (limit==null?10:limit); i++) {
            personLimitList.add(String.valueOf((i)));
        }
    }

    InputFilter emailFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; ++i)
            {
                if (!Pattern.compile("[1234567890@.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]*").matcher(String.valueOf(source.charAt(i))).matches())
                {
                    return "";
                }
            }
            return null;
        }
    };

    private Calendar oldStartC, oldStopC;

    InputStrDialog(Context context, OnDialogInteractionListener listener, int vid, int inputType, int actionbarColorId, String tag, @Nullable Calendar oldStartC, @Nullable Calendar oldStopC, @Nullable Integer limit){
        new InputStrDialog(context, listener, vid, inputType, actionbarColorId, tag, "", oldStartC, oldStopC, limit);
    }
    private InputStrDialog(Context context, OnDialogInteractionListener listener, int vid, int inputType, int actionbarColorId, String tag, String oldStr,
                           @Nullable Calendar oldStartC, @Nullable Calendar oldStopC, @Nullable Integer limit){
        this.context = context;
        this.mListener = listener;
        this.vid = vid;
        this.inputType = inputType;
        this.limit=limit;
        this.tag=tag;
        this.oldStr=oldStr;
        this.oldStartC=oldStartC;
        this.oldStopC=oldStopC;
        if (actionbarColorId!=0) this.actionbarColor= ContextCompat.getColor(context, actionbarColorId);
        switch (inputType){
            case TYPE_GENDER:
                inputGender();
                return;
            case TYPE_DATE_PICKER:
                inputDatePicker();
                return;
            case TYPE_DATE_TIME_PICKER:
                inputDateTimePicker();
                return;
            case TYPE_DATE_RANGE:
                getStartDatePickerDialog();
                return;
            case TYPE_DATE_TIME_RANGE:
                getStartDatePickerDialog();
                return;
            // Other INPUT TYPEs
            default:
                initDialog();
                break;
        }
    }

    private void initDialog(){
        Log.d(LOG_TAG,"initDialog");
        //Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //get layout
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.vr_input_str_dialog, null);

        //set builder
        builder.setView(view).setTitle("");
        dialog = builder.create();

        //UI
        input_str_dialog_actionbar= (RelativeLayout) view.findViewById(R.id.input_str_dialog_actionbar);
        if(actionbarColor!=null) input_str_dialog_actionbar.setBackgroundColor(actionbarColor);
        titleTv = (TextView) view.findViewById(R.id.input_str_dialog_Title);
        titleTv.setText("編輯 " + tag);
        cancelIv = (ImageView) view.findViewById(R.id.input_str_dialog_actionBack);
        cancelIv.setOnClickListener(this);
        finishTv = (TextView) view.findViewById(R.id.input_str_dialog_finished);
        finishTv.setOnClickListener(this);
        inputEt = (EditText) view.findViewById(R.id.input_str_dialog_et);
        inputEt.setInputType(inputType);

        if(oldStr.isEmpty()&&limit!=null)
            inputEt.setHint("限輸入"+limit+"字");
        else
            inputEt.setText(oldStr);

        if(limit!=null){
            switch (inputType){
                case (InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS):
                    inputEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit), emailFilter});
                    break;
                case InputType.TYPE_CLASS_NUMBER:
                    double limitpwr = Math.log10(limit);
                    inputEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter((int)limitpwr+1)});
                    break;
                case InputType.TYPE_CLASS_PHONE:
                default:
                    inputEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)});
                    break;
            }
        }

        if (dialog != null ) dialog.show();
    }

    private  void inputGender(){
        AlertDialog.Builder al = new AlertDialog.Builder(context);
        al.setTitle("請選擇性別：")
                .setPositiveButton("男 male", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.writeInputStr(vid, "M");
                    }
                })
                .setNeutralButton("女 female", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.writeInputStr(vid, "F");
                    }
                })
                .show();
    }

    private void inputDatePicker(){
        // 設定初始日期
        if(oldStartC==null) oldStartC = GregorianCalendar.getInstance();
        DateTimePickerDialog dpd = new DateTimePickerDialog(context, new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(@Nullable Calendar calendar) {
                if(calendar==null){
                    mListener.writeInputStr(vid, "");
                    return;
                }
                mListener.writeInputStr(vid, String.format(Locale.getDefault(), "%04d/%02d/%02d",
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)));
                mListener.writeStartStopC(vid, calendar, calendar);
            }
        }, oldStartC, actionbarColor);
        dpd.hideTime(true);
        dpd.show();
    }

    private void inputDateTimePicker(){
        // 設定初始日期
        if(oldStartC==null) oldStartC = GregorianCalendar.getInstance();
        DateTimePickerDialog dpd = new DateTimePickerDialog(context, new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(@Nullable Calendar calendar) {
                if(calendar==null){
                    mListener.writeInputStr(vid, "");
                    return;
                }
                mListener.writeInputStr(vid, String.format(Locale.getDefault(), "%04d/%02d/%02d %2d:%2d",
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)
                        , calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
                mListener.writeStartStopC(vid, calendar, calendar);
            }
        }, oldStartC, actionbarColor);
        dpd.show();
    }

    private void getStartDatePickerDialog() {
        // 設定初始日期
        if(oldStartC==null) oldStartC = GregorianCalendar.getInstance();
        DateTimePickerDialog dpd = new DateTimePickerDialog(context, new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(@Nullable Calendar calendar) {
                if(calendar==null){
                    mListener.writeStartStopC(vid, null, null);
                    return;
                }
                getStopDatePickerDialog(calendar);
            }
        }, oldStartC, actionbarColor);
        dpd.setTitle("選擇開始日期");
        dpd.show();
    }
    private void getStopDatePickerDialog(final Calendar startC) {
        Calendar mStartC =startC;
        if(oldStopC!=null) mStartC =oldStopC;
        DateTimePickerDialog dpd = new DateTimePickerDialog(context, new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(@Nullable Calendar calendar) {
                if(calendar==null){
                    mListener.writeStartStopC(vid, null, null);
                    return;
                }
                if (calendar.after(startC)){
                    mListener.writeStartStopC(vid, startC, calendar);
                } else {
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(
                            context);
                    builderInner
                            .setTitle("結束時間需在開始時間之後：")
                            .setPositiveButton("確認",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getStopDatePickerDialog(startC);
                                        }
                                    })
                            .show();
                }
            }
        }, mStartC, actionbarColor);
        if(mStartC.after(startC)) dpd.setMinDate( startC.getTimeInMillis() );
        dpd.setTitle("選擇結束日期");
        dpd.show();
    }

//    private void getStartDateTimePickerDialog() {
//        // 設定初始日期
//        c = GregorianCalendar.getInstance();
//        DatePickerDialog dpd = new DatePickerDialog(context,
//                new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
//                        Log.d(LOG_TAG,"DatePickerDialog view.isShown()="+view.isShown());
//                        if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && !view.isShown()) return;
//                        TimePickerDialog tpd = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                Log.d(LOG_TAG,"TimePickerDialog view.isShown()="+view.isShown());
//                                if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && !view.isShown()) return;
//                                Calendar startC = new GregorianCalendar(year,monthOfYear,dayOfMonth,hourOfDay,minute);
//                                getStopDateTimePickerDialog(startC);
//                            }
//                        }, 0, 0, true);
//                        tpd.setTitle("選擇開始時間");
//                        tpd.show();
//                    }
//                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
//        dpd.setTitle("選擇開始日期");
//        dpd.show();
//    }
//    private void getStopDateTimePickerDialog(final Calendar startC) {
//        Calendar cal = new GregorianCalendar( startC.get(Calendar.YEAR), startC.get(Calendar.MONTH), startC.get(Calendar.DAY_OF_MONTH));
//        //cal.add(Calendar.DAY_OF_MONTH,1);
//
//        DatePickerDialog dpd = new DatePickerDialog(context,
//                new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
//                        Log.d(LOG_TAG,"DatePickerDialog view.isShown()="+view.isShown());
//                        if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && !view.isShown()) return;
//                        TimePickerDialog tpd = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                Log.d(LOG_TAG,"TimePickerDialog view.isShown()="+view.isShown());
//                                if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && !view.isShown()) return;
//                                Calendar stopC = new GregorianCalendar(year,monthOfYear,dayOfMonth,hourOfDay,minute);
//                                if (stopC.after(startC)){
//                                    mListener.writeStartStopC(startC, stopC);
//                                } else {
//                                    AlertDialog.Builder builderInner = new AlertDialog.Builder(
//                                            context);
//                                    builderInner
//                                            .setTitle("結束時間需在開始時間之後：")
//                                            .setPositiveButton("確認",
//                                                    new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            getStopDateTimePickerDialog(startC);
//                                                        }
//                                                    })
//                                            .show();
//                                }
//                            }
//                        }, 0, 0, true);
//                        tpd.setTitle("選擇結束時間");
//                        tpd.show();
//                    }
//                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
//        dpd.getDatePicker().setMinDate(cal.getTimeInMillis());
//        dpd.setTitle("選擇結束日期");
//        dpd.show();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.input_str_dialog_actionBack:
                if(!oldStr.isEmpty() && !oldStr.equals(inputEt.getText().toString())){
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                            context);
                    dialogBuilder
                            .setTitle("尚未儲存您的變更。要捨棄變更嗎?")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (InputStrDialog.this.dialog != null) InputStrDialog.this.dialog.dismiss();
                                }
                            })
                            .setNeutralButton("取消", null)
                            .show();
                } else {
                    if (dialog != null) dialog.dismiss();
                }
                break;
            case R.id.input_str_dialog_finished:
                checkData();
                break;
        }
    }

    public void checkData(){
        StringBuilder str= new StringBuilder();
        Log.d(LOG_TAG,"limit="+(limit==null?"null":limit));
        switch (inputType){
            case InputType.TYPE_CLASS_NUMBER:
                if(limit!=null) str.append((getIntFromString(inputEt.getText().toString())>limit)?"數目限定最大為"+limit+"\n":"");
                break;
            case InputType.TYPE_CLASS_PHONE:
                if(limit!=null && limit>0) str.append((inputEt.length()>limit)?"電話限定最長為"+limit+"碼\n":"");
                break;
            default:
                if(limit!=null && limit>0) str.append((inputEt.length()>limit)?"長度限定"+limit+"字\n":"");
                break;
        }

        if (str.length()>0){
            AlertDialog.Builder al = new AlertDialog.Builder(context);
            al.setTitle("錯誤")
                    .setMessage(str.toString())
                    .setPositiveButton("確定", null)
                    .show();
        } else {
            switch (inputType){
                default:
                    mListener.writeInputStr(vid, inputEt.getText().toString());
                    break;
            }
        }
        if (dialog != null) dialog.dismiss();
    }

    public static int getIntFromString(String str){
        int getInt = 0;
        try {
            getInt = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"str="+str+" getInt="+getInt);
        return getInt;
    }
    public static double getDoubleFromString(String str){
        double getDouble = 0d;
        try {
            getDouble = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"str="+str+" getDouble="+getDouble);
        return getDouble;
    }

    public static void inputStrView(Context context, int vid, int inputType, int actionbarColorId, String tag, String oldStr, @Nullable Integer limit){
        OnDialogInteractionListener mListener;
        if (context instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(context, mListener, vid, inputType, actionbarColorId, tag, oldStr, null, null, limit);
    }
    public static void inputStrView(Fragment fragment, int vid, int inputType, int actionbarColorId, String tag, String oldStr, @Nullable Integer limit){
        OnDialogInteractionListener mListener;
        if (fragment instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(fragment.getActivity(), mListener, vid, inputType, actionbarColorId, tag, oldStr, null, null, limit);
    }
    public static void inputStrType(Context context, int vid, int inputType, int actionbarColorId, String tag, @Nullable Integer limit){
        OnDialogInteractionListener mListener;
        if (context instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(context, mListener, vid, inputType, actionbarColorId, tag, null, null, limit);
    }
    public static void inputStrType(Fragment fragment, int vid, int inputType, int actionbarColorId, String tag, @Nullable Integer limit){
        OnDialogInteractionListener mListener;
        if (fragment instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(fragment.getActivity(), mListener, vid, inputType, actionbarColorId, tag, null, null, limit);
    }
    public static void inputDateTimeRange(Context context, int vid, int actionbarColorId, String tag, Calendar oldStartC, Calendar oldStopC){
        OnDialogInteractionListener mListener;
        if (context instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(context, mListener, vid, InputStrDialog.TYPE_DATE_TIME_RANGE, actionbarColorId, tag, oldStartC, oldStopC, null);
    }
    public static void inputDateTimeRange(Fragment fragment, int vid, int actionbarColorId, String tag, Calendar oldStartC, Calendar oldStopC){
        OnDialogInteractionListener mListener;
        if (fragment instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(fragment.getActivity(), mListener, vid, InputStrDialog.TYPE_DATE_TIME_RANGE, actionbarColorId, tag, oldStartC, oldStopC, null);
    }
    public static void inputDate(Context context, int vid, int actionbarColorId, String tag, Calendar oldStartC){
        OnDialogInteractionListener mListener;
        if (context instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(context, mListener, vid, InputStrDialog.TYPE_DATE_PICKER, actionbarColorId, tag, oldStartC, null, null);
    }
    public static void inputDate(Fragment fragment, int vid, int actionbarColorId, String tag, Calendar oldStartC){
        OnDialogInteractionListener mListener;
        if (fragment instanceof OnDialogInteractionListener) {
            Log.d(LOG_TAG, "mListener = (OnDialogInteractionListener) context;");
            mListener = (OnDialogInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement OnDialogInteractionListener");
        }
        new InputStrDialog(fragment.getActivity(), mListener, vid, InputStrDialog.TYPE_DATE_PICKER, actionbarColorId, tag, oldStartC, null, null);
    }
    public interface OnDialogInteractionListener {
        void writeInputStr(int vid, String inputStr);
        void writeStartStopC(int vid, Calendar startC, Calendar stopC);
    }

}
