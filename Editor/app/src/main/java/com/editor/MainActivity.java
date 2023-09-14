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
import java.util.concurrent.*;
import android.util.*;

public class MainActivity extends Activity implements Runnable
{
	
	public static final Handler mHamdler = new Handler();
	private ThreadPoolExecutor mPool;
	private CodeEdit E;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0xff222222));
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
        mHamdler.postDelayed(this,50);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,0,0,"test");
		menu.add(1,1,1,"Redo");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		int id = item.getItemId();
		switch(id){
			case 0:
				test();
				break;
			case 1:
				E.Redo();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void run()
	{
		//LinkedBlockingQueue queue = new LinkedBlockingQueue();
		//mPool = new ThreadPoolExecutor(5, 1000, 0, TimeUnit.SECONDS, queue);
		//test();
		//test2("/storage/emulated/0/Linux/2.java");
		//loadFileInThread("/storage/emulated/0/Linux/1.java");
	}
	
	public void loadFileInThread(String path)
	{
		myReader reader = new myReader(path);
		String text = reader.r("UTF-8");
	    CodeEdit E = new CodeEdit(this);
		E.setText(text,0,text.length());
	
		setContentView(E);
		E.getLayoutParams().height=2180;
		E.setPool(mPool);
		E.reDrawTextS(0,E.getText().length());
		
		//E.getText().setSpan(new ForegroundColorSpan(0xff98c379),0,822,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	public void test()
	{
		SpannableStringBuilderLite li = new SpannableStringBuilderLite("0123456789");
		Random rand = new Random();
		for(int i = 0;i<10;i+=1){
			li.setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		li.setSpan(new ForegroundColorSpan(rand.nextInt()),0,6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		SpannableStringBuilderLite li2 = new SpannableStringBuilderLite("0123456789");
		for(int i = 0;i<10;i+=1){
			li2.setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		li.replace(5,10,li,0,li.length());
	}
	
	public void test2(String path)
	{
		myReader reader = new myReader(path);
		String text = reader.r("UTF-8");
	    Edit E = new Edit(this);
		E.setText(text,0,text.length());

		setContentView(E);
		E.getLayoutParams().height=2180;
		Random rand = new Random();
		for(int i = 0;i<1000;i+=1){
			E.getText().setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+10,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			E.getText().setSpan(new BackgroundColorSpan(rand.nextInt()),i,i+10,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
	
}
