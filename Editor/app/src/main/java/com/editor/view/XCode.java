package com.editor.view;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.widget.AdapterView.*;
import com.editor.text2.*;
import java.util.concurrent.*;
import com.editor.*;
import com.editor.text2.builder.listenerInfo.listener.*;
import android.util.*;


public class XCode extends ViewGroup implements OnItemClickListener,myEditCompletorListener.onOpenWindowLisrener
{

	@Override
	public void callOnOpenWindow(View Window, int x, int y)
	{
		
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		lastEdit.onItemClick(p1,p2,p3,p4);
	}
	
	private View mTitle;
	private View mPageHandler;
	private View mDownBar;
	private ListView mWindow;
	
	private CodeEdit lastEdit;
	private ThreadPoolExecutor mPool;
	
	
	public XCode(Context cont)
	{
		super(cont);
		mWindow = new TopListView(cont);
		mWindow.setBackgroundColor(0xff1e2126);
		mWindow.setDivider(null);
		mWindow.setOnItemClickListener(this);
		mWindow.setFocusable(false);
	}
	
	public void loadFileInThread(final String path)
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				myReader reader = new myReader(path);
				final CodeEdit E = new CodeEdit(getContext());
				String text = reader.r("UTF-8");
				E.setText(text,0,text.length());
				E.setPool(mPool);

				Runnable run2 = new Runnable()
				{
					@Override
					public void run()
					{
						addView(E);
						E.setWindow(mWindow,XCode.this);
						E.setPool(mPool);
						addView(mWindow);
						lastEdit = E;
						E.getLayoutParams().height=2180;
						E.reDrawTextS(0,E.getText().length());
					}
				};
				post(run2);
			}
		};
		mPool.execute(run);
	}
	
	public void setPool(ThreadPoolExecutor pool){
		mPool = pool;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if(false){
			
		}
		mWindow.layout((int)mWindow.getLeft(),(int)mWindow.getTop(),mWindow.getRight(),mWindow.getBottom());
		if(lastEdit!=null){
			lastEdit.layout(0,0,1000,2000);
		}
	}
	
	
	private static class TopListView extends ListView
	{

		public TopListView(Context cont){
			super(cont);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event)
		{
			return false;
		}

	}
	
}
