package si.ox.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class TextMessageReceiver extends BroadcastReceiver {
    public TextMessageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving

        Bundle bundle=intent.getExtras();

        Object[] messages=(Object[])bundle.get("pdus");
        SmsMessage[] sms=new SmsMessage[messages.length];

        for(int n=0;n<messages.length;n++){
            sms[n]=SmsMessage.createFromPdu((byte[]) messages[n]);
        }

        for(SmsMessage msg:sms){

            //Toast.makeText(context, "SMS received", Toast.LENGTH_SHORT).show();

            forwardSMS(context, msg.getOriginatingAddress(), msg.getMessageBody());

        }
    }

    /**
     * Broadcasts debug message to the main activity.
     * It will be shown in the Debug section of settings dialog if it is currently open.
     * @param context
     * @param txt
     */
    public static void updateDebugBox(Context context, String txt){

        /*

        // broadcast the message to our main activity
        Intent intent2 = new Intent("debug");
        // You can also include some extra data.
        intent2.putExtra("txt", txt);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);

        */

    }

    /**
     * Gets called for each received SMS by TextMessageReceiver.
     * Outputs that SMS to the debug window (if currently open) and
     * forwards it to the specified number if forwarding is enabled.
     * @param context
     * @param fromNumber
     * @param msgBody
     */
    public static void forwardSMS(Context context, String fromNumber, String msgBody)
    {

        // debug
        updateDebugBox(context, "\nFrom: " + fromNumber + "\n"+ "Message: "+msgBody+"\n");

        // sanity check
        if (msgBody.length() == 0){
            return;
        }

        // see if forwarding is enabled
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPref.getBoolean("enable_forwarding", false)) {
            return;
        }

        //Toast.makeText(context, "SMS received", Toast.LENGTH_SHORT).show();

        // format the body of the forwarded SMS
        msgBody = fromNumber + ": " + msgBody;

        // get and verify forwarding number
        String fwdToNumber = sharedPref.getString("forwarding_number", "");
        if (fwdToNumber.length() == 0){
            String errMsg = "SMS Forwarder error: forwarding number is not specified.";
            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show();
            updateDebugBox(context, errMsg);
            return;
        }

        // forward the message
        SmsManager.getDefault().sendTextMessage(fwdToNumber, null, msgBody, null, null);

    }


}
