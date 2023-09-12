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
import com.editor.text2.*;

public class MainActivity extends Activity implements Runnable
{
	
	public static final Handler mHamdler = new Handler();
	private CodeEdit E;
	
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,0,0,"Uedo");
		menu.add(1,1,1,"Redo");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		int id = item.getItemId();
		switch(id){
			case 0:
				E.Uedo();
				break;
			case 1:
				E.Redo();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void run(){
		loadFileInThread("/storage/emulated/0/Linux/3.java");
	}
	
	public void loadFileInThread(String path)
	{
		myReader reader = new myReader(path);
		String text = reader.r("UTF-8");
	    Edit E = new Edit(this);
		E.setText(text,0,text.length());
	
		setContentView(E);
		E.getLayoutParams().height=2180;
		//E.reDrawText(0,E.getText().length());
		
		//E.getText().setSpan(new ForegroundColorSpan(0xff98c379),0,822,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		Random rand = new Random();
		for(int i = 0;i<1000;i+=10){
			E.getText().setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+20,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	}
	
}
