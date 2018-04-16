package com.get_slyncy.slyncy.Model.Service.smsmmsradar;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by nsshurtz on 4/5/18.
 */

public class MarkReadJobService extends JobService
{

    private final static ArrayList<MarkReadTask> tasks = new ArrayList<>();

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
                    if (bundle.containsKey("multiple"))
                    {
                        ArrayList<Integer> threadIds = bundle.getIntegerArrayList("threadId");
                        if (threadIds != null)
                        {
                            MarkReadTask task = new MarkReadTask(UUID.randomUUID().toString(), getApplicationContext());
                            tasks.add(task);
                            task.execute(threadIds.toArray(new Integer[threadIds.size()]));
                        }
                    }
                    else
                    {
                        int threadId = bundle.getInt("threadId");
                        MarkReadTask task = new MarkReadTask(UUID.randomUUID().toString(), getApplicationContext());
                        tasks.add(task);
                        task.execute(threadId);
                    }
                }
                params.completeWork(work);

            }
        }
        else
        {
            PersistableBundle bundle = params.getExtras();
            if (bundle.containsKey("multiple"))
            {
                if (bundle.containsKey("threadId"))
                {
                    ArrayList<Integer> threadIds = new ArrayList<>();
                    for (int threadId : bundle.getIntArray("threadId"))
                    {
                        threadIds.add(threadId);
                    }
                    if (bundle.getIntArray("threadId").length > 0)
                    {
                        MarkReadTask task = new MarkReadTask(UUID.randomUUID().toString(), getApplicationContext());
                        tasks.add(task);
                        task.execute(threadIds.toArray(new Integer[threadIds.size()]));
                    }
                }
            }
            else
            {
                int threadId = bundle.getInt("threadId");
                MarkReadTask task = new MarkReadTask(UUID.randomUUID().toString(), getApplicationContext());
                tasks.add(task);
                task.execute(threadId);
            }

        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        for (MarkReadTask task : tasks)
        {
            task.cancel(false);
        }
        return tasks.size() != 0;
    }


    private static class MarkReadTask extends AsyncTask<Integer, Void, Void>
    {
        String id;
        Context context;

        MarkReadTask(String id, Context context)
        {
            this.id = id;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... integers)
        {

            for (Integer integer : integers)
            {
                if (integer != null)
                {
                    ClientCommunicator.markThreadAsRead(integer, context);
                }
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
            MarkReadTask that = (MarkReadTask) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode()
        {

            return Objects.hash(id);
        }
    }
}
