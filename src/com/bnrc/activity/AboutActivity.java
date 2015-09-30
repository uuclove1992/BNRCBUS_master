package com.bnrc.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ab.activity.AbActivity;
import com.bnrc.busapp.R;



public class AboutActivity extends AbActivity {
    String version = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAbContentView(R.layout.about);
        this.setTitleText(R.string.about);
        this.setLogo(R.drawable.button_selector_back);
        this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleTextMargin(10, 0, 0, 0);
	    this.setLogoLine(R.drawable.line);
	    
        logoView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        TextView version_val = ((TextView)findViewById(R.id.version_val));
        
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.andbase", PackageManager.GET_CONFIGURATIONS);
			version = pinfo.versionName;
			version_val.setText("V"+version);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
}


