package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms;

import android.content.Context;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
/**
 * Created by nsshurtz on 4/5/18.
 */

public class MmsListener implements IMmsListener
{
    Context context;

    public MmsListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void onMmsSent(SlyncyMessage mms)
    {
        if (mms != null)
        {
//                    showSmsSToast(mms);
            ClientCommunicator.uploadSingleMessage(mms, context);
        }
    }

    @Override
    public void onMmsReceived(SlyncyMessage mms)
    {
        if (mms != null)
        {
//                    showSmsRToast(mms);
            ClientCommunicator.uploadSingleMessage(mms, context);
        }
    }
}
