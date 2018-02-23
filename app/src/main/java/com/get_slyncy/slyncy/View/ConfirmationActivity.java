package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.get_slyncy.slyncy.R;

/**
 * Created by undermark5 on 2/22/18.
 */

public class ConfirmationActivity extends Activity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("name");
        String phone = bundle.getString("phone");
        String email = bundle.getString("email");
    }
}
