package ua.com.anyapps.smstrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class _SMSReceiver extends BroadcastReceiver {

    //interface
    private static _SMSListener mListener;
    private static final String TAG = "debapp";
    private static long lastMsgId;
    private static long currentTimeMills;
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
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
                        mListener.messageReceived(msgBody, msg_from, currentTimeMills);
                    }
                    lastMsgId = tmpMsgId;
                }catch(Exception e){
                    Log.e(TAG, e.getMessage());
                    mListener.messageReceived(msgBody, msg_from, currentTimeMills);
                }
            }
        }
    }

    public static void bindListener(_SMSListener listener) {
        mListener = listener;
    }
}
