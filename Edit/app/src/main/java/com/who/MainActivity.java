package com.who;

import android.app.*;
import android.os.*;
import android.view.*;
import com.who.Edit.Base.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
      
		View e = new Edit(this);
		e.setBackgroundColor(0xff222222);
		setContentView(e);
		Edit.openInputor(this, e);
		ViewGroup.LayoutParams pa = e.getLayoutParams();
		pa.width = 1080;
		pa.height = 2408;
    }
}



