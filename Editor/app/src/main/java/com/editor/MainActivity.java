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
	
	private ThreadPoolExecutor mPool;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0xff222222));
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        runOnUiThread(this);
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
				//E.Redo();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void run()
	{
		LinkedBlockingQueue queue = new LinkedBlockingQueue();
		mPool = new ThreadPoolExecutor(5, 1000, 0, TimeUnit.SECONDS, queue);
		loadFileInThread("/storage/emulated/0/Linux/2.java");
		//test();
		//test2("/storage/emulated/0/Linux/2.java");
		//test3();
	}
	
	public void loadFileInThread(final String path)
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				myReader reader = new myReader(path);
				final CodeEdit E = new CodeEdit(MainActivity.this);
				String text = reader.r("UTF-8");
				long last = System.currentTimeMillis();
				E.setText(text,0,text.length());
				long now = System.currentTimeMillis();
				Log.w("SetText",""+(now-last));
				E.setPool(mPool);
				
				Runnable run2 = new Runnable()
				{
					@Override
					public void run()
					{
						setContentView(E);
						E.getLayoutParams().height=2180;
						E.reDrawTextS(0,E.getText().length());
					}
				};
				runOnUiThread(run2);
			}
		};
		mPool.execute(run);
		
		/*Random rand = new Random();
		for(int i = 0;i<1000;i+=1){
			E.getText().setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+10,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			E.getText().setSpan(new BackgroundColorSpan(rand.nextInt()),i,i+10,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}*/
		//E.getText().setSpan(new ForegroundColorSpan(0xff98c379),0,822,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	public void test()
	{
		SpannableStringBuilderLite li = new SpannableStringBuilderLite("0123456789");
		Random rand = new Random();
		for(int i = 9;i>=0;i-=1){
			li.setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		Object span = new ForegroundColorSpan(rand.nextInt());
		li.setSpan(span,0,6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		li.setSpan(span,0,6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		try{
			SpannableStringBuilderLite li2 = new SpannableStringBuilderLite("");
			li2.replace(0,0,li,0,8);
			for(int i = 0;i<10;i+=1){
				li2.setSpan(new ForegroundColorSpan(rand.nextInt()),i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			li.replace(5,10,li,0,li.length());
			li.getSpans(0,9,Object.class);
		}catch(Exception e){}	
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
	
	public void test3()
	{
		Edit E = new Edit(this);
		E.setText("012345",0,6);
		setContentView(E);
		E.getLayoutParams().height=2180;
	}
	
}
