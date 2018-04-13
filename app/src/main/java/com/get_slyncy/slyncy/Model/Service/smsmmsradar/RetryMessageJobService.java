package com.get_slyncy.slyncy.Model.Service.smsmmsradar;

import static com.get_slyncy.slyncy.Model.Util.Json.fromJson;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class RetryMessageJobService extends JobService
{
    private final static ArrayList<PendingMessageTask> tasks = new ArrayList<>();
    private static ContentResolver resolver;

    public RetryMessageJobService(ContentResolver resolver)
    {
        RetryMessageJobService.resolver = resolver;
    }

    public RetryMessageJobService()
    {

    }

    @Override
    public boolean onStartJob(JobParameters params)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            JobWorkItem work;
            while ((work = params.dequeueWork()) != null)
            {
                Intent workIntent = work.getIntent();
                Bundle bundle = workIntent.getExtras();
                if (bundle != null)
                {
                    SlyncyMessage message = fromJson(bundle.getString("message"), SlyncyMessage.class);
                    if (message != null)
                    {
                        PendingMessageTask task = new PendingMessageTask(UUID.randomUUID().toString());
                        tasks.add(task);
                        task.execute(message);
                    }
                }
            }
        }
        else
        {
            PersistableBundle bundle = params.getExtras();
            SlyncyMessage message = fromJson(bundle.getString("message"), SlyncyMessage.class);
            if (message != null)
            {
                PendingMessageTask task = new PendingMessageTask(UUID.randomUUID().toString());
                tasks.add(task);
                task.execute(message);
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        return false;
    }

    private static class PendingMessageTask extends AsyncTask<SlyncyMessage,Void,Void>
    {
        String id;

        PendingMessageTask(String id)
        {
            this.id = id;
        }

        @Override
        protected Void doInBackground(SlyncyMessage... slyncyMessages)
        {
            for (SlyncyMessage slyncyMessage : slyncyMessages)
            {
                ClientCommunicator.uploadSingleMessage(slyncyMessage, resolver);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            tasks.remove(this);
            super.onPostExecute(aVoid);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PendingMessageTask that = (PendingMessageTask) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode()
        {

            return Objects.hash(id);
        }
    }
}
