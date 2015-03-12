package com.mobsandgeeks.showcase.myilshowcase;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mobsandgeeks.myil.dashboard.CircularProgressBar;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ShowcaseActivity extends Activity {

    // UI References
    @InjectView(R.id.circularProgressBar)
    CircularProgressBar mCircularProgressBar;

    // Attributes
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showcase);
        ButterKnife.inject(this);

        mRandom = new Random();
        mCircularProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = mRandom.nextInt(mCircularProgressBar.getMax() + 1);
                mCircularProgressBar.setValue(value);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_showcase, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
