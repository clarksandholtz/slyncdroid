package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms;

import android.content.ContentResolver;
import android.content.Context;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
/**
 * Created by nsshurtz on 4/5/18.
 */

public class SmsListener implements ISmsListener
{
    private Context context;

    public SmsListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void onSmsSent(SlyncyMessage sms)
    {
        if (sms != null)
        {
//                    showSmsSToast(sms);
            ClientCommunicator.uploadSingleMessage(sms, context);
        }
    }

    @Override
    public void onSmsReceived(SlyncyMessage sms)
    {
        if (sms != null)
        {
//                    showSmsRToast(sms);
            ClientCommunicator.uploadSingleMessage(sms, context);
        }
    }
}
