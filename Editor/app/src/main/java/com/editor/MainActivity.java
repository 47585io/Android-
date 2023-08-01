package com.editor;

import android.app.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import com.editor.text.*;
import java.io.*;
import android.text.*;
import android.text.style.*;

public class MainActivity extends Activity 
{
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0xff222222));
		//getWindow().setBackgroundDrawable(null);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadFileInThread("/storage/emulated/0/Linux/2.java");
    }
	
	public void loadFileInThread(String path)
	{
		myReader reader = new myReader(path);
		String text = reader.r("UTF-8");
	    Edit E = new Edit(this);
		E.setText(text,0,text.length());
		setContentView(E);
		E.getLayoutParams().height=1800;
		
		Editable editor = E.getText();
		editor.setSpan(new BackgroundColorSpan(0xffddeeff),0,editor.length(),0);
		E.scrollTo(0,100000);
	}
	
}
