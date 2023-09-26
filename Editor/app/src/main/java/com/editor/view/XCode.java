package com.editor.view;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.widget.AdapterView.*;
import com.editor.text2.*;
import java.util.concurrent.*;
import com.editor.*;
import android.util.*;
import com.editor.text.*;
import com.editor.text.base.*;
import static com.editor.text2.builder.listener.myEditCompletorListener.*;
import com.editor.text2.builder.listener.*;


public class XCode extends ViewGroup implements myEditCompletorListener.onOpenWindowLisrener
{

	@Override
	public void callOnOpenWindow(View content, int x, int y, int width, int height)
	{
		mWindow.setVisibility(VISIBLE);
		mWindow.removeAllViews();
		mWindow.addView(content);
		ViewGroup.LayoutParams parms = mWindow.getLayoutParams();
		parms.width = width;
		parms.height = height;
		mWindow.measure(width,height);
		mWindow.layout(x,y,x+width,y+height);
	}

	@Override
	public void callOnCloseWindow(){
		mWindow.setVisibility(GONE);
	}

	@Override
	public void callOnRefreshWindow(View content)
	{
		mWindow.removeAllViews();
		mWindow.addView(content);
	}
	
	private View mTitle;
	private View mPageHandler;
	private View mDownBar;
	private ViewGroup mWindow;
	private ThreadPoolExecutor mPool;
	
	
	public XCode(Context cont)
	{
		super(cont);
		mWindow = new myWindow(cont);
		mWindow.setBackgroundColor(0xff1e2126);
		//mWindow.setFocusable(false);
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
						addView(mWindow);
						E.setWindowListener(XCode.this);
						E.setPool(mPool);
						E.getLayoutParams().height=2180;
						E.reDrawTextContinuous(0,E.getText().length());
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
		if(getChildCount()==0){
			return;
		}
		getChildAt(0).layout(0,0,r-l,b-t);
	}
	
	
	private static class myWindow extends ViewGroup
	{

		public myWindow(Context cont){
			super(cont);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event){
			return false;
		}

		@Override
		protected void onLayout(boolean p1, int l, int t, int r, int b)
		{
			if(!p1 || getChildCount()==0){
				return;
			}
			getChildAt(0).layout(0,0,r-l,b-t);
		}

	}
	
}
