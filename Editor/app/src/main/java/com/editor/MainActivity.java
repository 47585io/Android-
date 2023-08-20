package com.editor;

import android.app.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import com.editor.text.*;
import java.io.*;
import android.text.*;
import android.text.style.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements Runnable
{
	
	public static Handler mHamdler;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0xff222222));
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        mHamdler.postDelayed(this,50);
    }

	@Override
	public void run()
	{
		loadFileInThread("/storage/emulated/0/Linux/1.java");
		mHamdler = new Handler();
	}
	
	public void loadFileInThread(String path)
	{
		myReader reader = new myReader(path);
		String text = reader.r("UTF-8");
	    Edit E = new Edit(this);
		E.setText(text,0,text.length());
		setContentView(E);
		E.getLayoutParams().height=2180;
		
		Random rand = new Random();
		Editable editor = E.getText();
		for(int i=0;i<10000;++i){
			editor.setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+1,0);
		}
		//E.scrollTo(0,(int)E.getVScrollRange());
	}
	
}
