package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.get_slyncy.slyncy.Model.Util.SettingsDb;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.Model.Util.StringsHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nsshurtz on 2/15/18.
 */

public class NotificationFilterActivity extends Activity
{
    private SettingsDb settingsDb;

    static class AppListInfo {
        String pkg;
        String name;
        Drawable icon;
        boolean isEnabled;
    }

    private AppListInfo[] apps;


    class AppListAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return apps.length;
        }

        @Override
        public AppListInfo getItem(int position) {
            return apps[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null, true);
            }
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedTextView.setText(apps[position].name);
            checkedTextView.setCompoundDrawablesWithIntrinsicBounds(apps[position].icon, null, null, null);
            checkedTextView.setCompoundDrawablePadding((int) (8 * getResources().getDisplayMetrics().density));

            return view;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_filter);
        settingsDb = new SettingsDb(NotificationFilterActivity.this);

        new Thread(new Runnable() {
                @Override
                public void run()
                {
                PackageManager packageManager = getPackageManager();
                List<ApplicationInfo> appList = packageManager.getInstalledApplications(0);
                int count = appList.size();

                apps = new AppListInfo[count];
                settingsDb.open();
                for (int i = 0; i < count; i++) {
                    ApplicationInfo appInfo = appList.get(i);
                    apps[i] = new AppListInfo();
                    apps[i].pkg = appInfo.packageName;
                    apps[i].name = appInfo.loadLabel(packageManager).toString();
                    apps[i].icon = resizeIcon(appInfo.loadIcon(packageManager), 48);
                    apps[i].isEnabled = settingsDb.isEnabled(appInfo.packageName);
                }
                settingsDb.close();

                Arrays.sort(apps, new Comparator<AppListInfo>() {
                    @Override
                    public int compare(AppListInfo lhs, AppListInfo rhs) {
                        return StringsHelper.compare(lhs.name, rhs.name);
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayAppList();
                    }
                });
            }
        }).start();
    }

    private void displayAppList() {

        final ListView listView = (ListView) findViewById(R.id.lvFilterApps);
        AppListAdapter adapter = new AppListAdapter();
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                boolean checked = listView.isItemChecked(i);
                settingsDb.open();
                settingsDb.setEnabled(apps[i].pkg, checked);
                settingsDb.close();
                apps[i].isEnabled = checked;
            }
        });

        for (int i = 0; i < apps.length; i++) {
            listView.setItemChecked(i, apps[i].isEnabled);
        }

        listView.setVisibility(View.VISIBLE);
        findViewById(R.id.spinner).setVisibility(View.GONE);
    }


    private Drawable resizeIcon(Drawable icon, int maxSize) {
        Resources res = getResources();

        //Convert to display pixels
        maxSize = (int) (maxSize * res.getDisplayMetrics().density);

        Bitmap bitmap = Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);

        return new BitmapDrawable(res, bitmap);
    }
}
