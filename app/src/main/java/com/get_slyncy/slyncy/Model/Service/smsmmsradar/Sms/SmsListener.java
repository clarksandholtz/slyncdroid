package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.RetryMessageJobService;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

import static com.get_slyncy.slyncy.Model.Util.Json.toJson;

/**
 * Created by nsshurtz on 4/5/18.
 */

public class SmsListener implements ISmsListener
{
    private ContentResolver resolver;
    private String packageName;
    private JobScheduler jobScheduler;

    public SmsListener(ContentResolver resolver, JobScheduler jobScheduler, String packageName)
    {
        this.packageName = packageName;
        this.jobScheduler = jobScheduler;
        this.resolver = resolver;
    }

    @Override
    public void onSmsSent(SlyncyMessage sms)
    {
        if (sms != null)
        {
            if (!ClientCommunicator.uploadSingleMessage(sms, resolver))
            {
//                if (jobScheduler != null)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    {
//                        jobScheduler.enqueue(new JobInfo.Builder("slyncy_sync_send_service".hashCode(), new ComponentName(packageName, RetryMessageJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build(), new JobWorkItem(new Intent().putExtra("message", toJson(sms))));
//                    }
//                    else
//                    {
//                        PersistableBundle bundle = new PersistableBundle();
//                        bundle.putString("message", toJson(sms));
//                        jobScheduler.schedule(new JobInfo.Builder("slyncy_sync_send_service".hashCode(), new ComponentName(packageName, RetryMessageJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setExtras(bundle).build());
//                    }
//                }
            }
        }
    }

    @Override
    public void onSmsReceived(SlyncyMessage sms)
    {
        if (sms != null)
        {
            if (!ClientCommunicator.uploadSingleMessage(sms, resolver))
            {
//                if (jobScheduler != null)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    {
//                        jobScheduler.enqueue(new JobInfo.Builder("slyncy_sync_send_service".hashCode(), new ComponentName(packageName, RetryMessageJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build(), new JobWorkItem(new Intent().putExtra("message", toJson(sms))));
//                    }
//                    else
//                    {
//                        PersistableBundle bundle = new PersistableBundle();
//                        bundle.putString("message", toJson(sms));
//                        jobScheduler.schedule(new JobInfo.Builder("slyncy_sync_send_service".hashCode(), new ComponentName(packageName, RetryMessageJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setExtras(bundle).build());
//                    }
//                }
            }
        }
    }
}
