package ua.com.anyapps.smstrip;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SendQueryCompleted{

    private static final String TAG = "debapp";
    private SharedPreferences spPreferences;

    ToggleButton tbActivationButton;
    Spinner spRequestMethod;
    EditText etPathToScript;
    EditText etShortMessage;
    EditText etSenderPhoneNumber;
    EditText etRecipientPhoneId;
    EditText etCurrentTimeMillis;
    EditText etSMSFilter;
    EditText etBasicAuthLogin;
    EditText etBasicAuthPassword;
    Boolean createCalled = false;

    public final static String BROADCAST_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    BroadcastReceiver SMSBroadcastReceiver = null;
    public static final int REQUEST_RECEIVE_SMS_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createCalled = true;

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        tbActivationButton = findViewById(R.id.tbActivationButton);
        spRequestMethod = findViewById(R.id.spRequestMethod);
        etPathToScript = findViewById(R.id.etPathToScript);
        etShortMessage = findViewById(R.id.etShortMessage);
        etSenderPhoneNumber = findViewById(R.id.etSenderPhoneNumber);
        etRecipientPhoneId = findViewById(R.id.etRecipientPhoneId);
        etCurrentTimeMillis = findViewById(R.id.etCurrentTimeMillis);
        etSMSFilter = findViewById(R.id.etSMSFilter);
        etBasicAuthLogin = findViewById(R.id.etBasicAuthLogin);
        etBasicAuthPassword = findViewById(R.id.etBasicAuthPassword);

        loadPreferences();

        /*SMSReceiver.bindListener(new SMSListener() {
            @Override
            public void messageReceived(String _messageText, String _phoneNumber, Long _currentTimeMillis) {
                String messageText = _messageText;
                String phoneNumber = _phoneNumber;
                Long currentTimeMillis = _currentTimeMillis;
                String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String requestMethod = "";
                String pathToScript = "";
                String param1 = "";
                String param2 = "";
                String param3 = "";
                String param4 = "";
                String smsFilterText = "";
                String basicAuthLogin = "";
                String basicAuthPassword = "";

                // если включено
                if(tbActivationButton.isChecked()){
                    requestMethod = spRequestMethod.getSelectedItem().toString();
                    pathToScript = etPathToScript.getText().toString();
                    param1 = etShortMessage.getText().toString();
                    param2 = etSenderPhoneNumber.getText().toString();
                    param3 = etRecipientPhoneId.getText().toString();
                    param4 = etCurrentTimeMillis.getText().toString();
                    smsFilterText = etSMSFilter.getText().toString();
                    basicAuthLogin = etBasicAuthLogin.getText().toString();
                    basicAuthPassword = etBasicAuthPassword.getText().toString();

                    // если крипт прописан и указан хотя бы 1 параметр
                    if(pathToScript.length()>0){
                        if(param1.length()>0||param2.length()>0||param3.length()>0||param4.length()>0){
                            // если установлен фильтр
                            if(smsFilterText.length()>0){// и найдено вхождение
                                if(messageText.indexOf(smsFilterText)>=0){
                                    AsyncTaskSendQuery task = new AsyncTaskSendQuery(MainActivity.this, messageText, phoneNumber, currentTimeMillis, androidId, requestMethod, pathToScript, param1, param2, param3, param4, basicAuthLogin, basicAuthPassword);
                                    task.execute();
                                }
                            }else { // фильтр не установлен
                                AsyncTaskSendQuery task = new AsyncTaskSendQuery(MainActivity.this, messageText, phoneNumber, currentTimeMillis, androidId, requestMethod, pathToScript, param1, param2, param3, param4, basicAuthLogin, basicAuthPassword);
                                task.execute();
                            }
                        }
                    }
                }
            }
        });*/
        SMSBroadcastReceiver = new BroadcastReceiver() {
            long lastMsgId;
            long currentTimeMills;
            @Override
            public void onReceive(Context context, Intent intent) {
                //

                if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){Log.d(TAG, "4444444444");
                    Bundle bundle = intent.getExtras();
                    SmsMessage[] msgs = null;
                    String msg_from = "";
                    String msgBody = "";
                    if (bundle != null){
                        try{
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            msgs = new SmsMessage[pdus.length];
                            for(int i=0; i<msgs.length; i++){
                                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                                msg_from = msgs[i].getOriginatingAddress();
                                msgBody += msgs[i].getMessageBody();
                            }

                            // предотвращение отправки дублирующихся сообщений
                            long tmpMsgId;
                            currentTimeMills = msgs[0].getTimestampMillis();
                            tmpMsgId = (msgBody+msg_from+currentTimeMills).hashCode();
                            if(lastMsgId==tmpMsgId){
                                // уже отправлено
                            }else {
                                messageReceived(msgBody, msg_from, currentTimeMills);
                            }
                            lastMsgId = tmpMsgId;
                        }catch(Exception e){
                            Log.e(TAG, e.getMessage());
                            messageReceived(msgBody, msg_from, currentTimeMills);
                        }
                    }
                }
            }
        };
        checkPermissions();
    }

    private void messageReceived(String _messageText, String _phoneNumber, Long _currentTimeMillis) {
        String messageText = _messageText;
        String phoneNumber = _phoneNumber;
        Long currentTimeMillis = _currentTimeMillis;
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String requestMethod = "";
        String pathToScript = "";
        String param1 = "";
        String param2 = "";
        String param3 = "";
        String param4 = "";
        String smsFilterText = "";
        String basicAuthLogin = "";
        String basicAuthPassword = "";

        // если включено
        if(tbActivationButton.isChecked()){
            requestMethod = spRequestMethod.getSelectedItem().toString();
            pathToScript = etPathToScript.getText().toString();
            param1 = etShortMessage.getText().toString();
            param2 = etSenderPhoneNumber.getText().toString();
            param3 = etRecipientPhoneId.getText().toString();
            param4 = etCurrentTimeMillis.getText().toString();
            smsFilterText = etSMSFilter.getText().toString();
            basicAuthLogin = etBasicAuthLogin.getText().toString();
            basicAuthPassword = etBasicAuthPassword.getText().toString();

            // если крипт прописан и указан хотя бы 1 параметр
            if(pathToScript.length()>0){
                if(param1.length()>0||param2.length()>0||param3.length()>0||param4.length()>0){
                    // если установлен фильтр
                    if(smsFilterText.length()>0){// и найдено вхождение
                        if(messageText.indexOf(smsFilterText)>=0){
                            AsyncTaskSendQuery task = new AsyncTaskSendQuery(MainActivity.this, messageText, phoneNumber, currentTimeMillis, androidId, requestMethod, pathToScript, param1, param2, param3, param4, basicAuthLogin, basicAuthPassword);
                            task.execute();
                        }
                    }else { // фильтр не установлен
                        AsyncTaskSendQuery task = new AsyncTaskSendQuery(MainActivity.this, messageText, phoneNumber, currentTimeMillis, androidId, requestMethod, pathToScript, param1, param2, param3, param4, basicAuthLogin, basicAuthPassword);
                        task.execute();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause(): ");
        savePreferences();
        // если уже показывалось активити
        if(SMSBroadcastReceiver!=null && createCalled) {
            // если зарегистрирован ресивер
            try {
                unregisterReceiver(SMSBroadcastReceiver);
            }catch (IllegalArgumentException ex){
                //
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions();
    }

    private void checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED){
            // есть разрешения
            setSMSListener();
        }else{
            // разрешения отсутствуют. запрос
            // До android 6 и выше
            if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                    // показать объяснение зачем включать и запросить разрешение
                    explanationAndPermissionRequest();
                }else{
                    // запросить разрешение
                    requestReceiveSMSPermissions();
                }
            }
        }
    }

    // объяснение для пользователя
    private AlertDialog explanationAndPermissionRequest(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.explanation_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.explanation_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        // запрос разрешения
                        requestReceiveSMSPermissions();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.explanation_negative_button), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        //dialog.cancel();
                        // закрыть приложение, если пользователь не включил gps
                        if (Build.VERSION.SDK_INT >= 16&&Build.VERSION.SDK_INT<21) {
                            finishAffinity();
                            System.exit(0);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            finishAndRemoveTask ();
                        }
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    private void setSMSListener(){
        if(SMSBroadcastReceiver!=null) {
            IntentFilter intentF = new IntentFilter(BROADCAST_ACTION);
            registerReceiver(SMSBroadcastReceiver, intentF);
        }
    }

    // запрос разрешения
    private void requestReceiveSMSPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS},
                REQUEST_RECEIVE_SMS_PERMISSION);
    }

    // результат запроса на получение прав
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RECEIVE_SMS_PERMISSION:
                if (grantResults.length == 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // права получены, установка слушателя
                        Log.d(TAG, "Права получены");
                        setSMSListener();
                    }

                } else {
                    // права не получены
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadPreferences(){
        Boolean activationButtonStatus = false;
        activationButtonStatus = spPreferences.getBoolean("activationButtonStatus", false);
        tbActivationButton.setChecked(activationButtonStatus);

        int requestMethodIndex = 0;
        requestMethodIndex = spPreferences.getInt("requestMethodIndex", 0);
        spRequestMethod.setSelection(requestMethodIndex);



        etPathToScript.setText(spPreferences.getString("pathToScript", getString(R.string.path_to_script_default_value_text)));
        etShortMessage.setText(spPreferences.getString("shortMessage", getString(R.string.data_param1_default_value_text)));
        etCurrentTimeMillis.setText(spPreferences.getString("currentTimeMillis", getString(R.string.data_param4_default_value_text)));
        etSenderPhoneNumber.setText(spPreferences.getString("senderPhoneNumber", getString(R.string.data_param2_default_value_text)));
        etRecipientPhoneId.setText(spPreferences.getString("recipientPhoneId", getString(R.string.data_param3_default_value_text)));
        etBasicAuthLogin.setText(spPreferences.getString("basicAuthLogin", getString(R.string.basic_auth_user_default_value_text)));
        etBasicAuthPassword.setText(spPreferences.getString("basicAuthPassword", getString(R.string.basic_auth_password_default_value_text)));
    }

    private void savePreferences(){
        SharedPreferences.Editor editor = spPreferences.edit();
        editor.putBoolean("activationButtonStatus", tbActivationButton.isChecked());
        editor.putInt("requestMethodIndex", spRequestMethod.getSelectedItemPosition());

        editor.putString("pathToScript", etPathToScript.getText().toString());
        editor.putString("shortMessage", etShortMessage.getText().toString());
        editor.putString("currentTimeMillis", etCurrentTimeMillis.getText().toString());
        editor.putString("senderPhoneNumber", etSenderPhoneNumber.getText().toString());
        editor.putString("recipientPhoneId", etRecipientPhoneId.getText().toString());
        editor.putString("basicAuthLogin", etBasicAuthLogin.getText().toString());
        editor.putString("basicAuthPassword", etBasicAuthPassword.getText().toString());

        editor.commit();
    }

    @Override
    public void SendQueryCompleted(int response) {
        Log.d(TAG, "RESPONSE: " + response);
    }


}
