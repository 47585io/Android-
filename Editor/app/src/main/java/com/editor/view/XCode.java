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
import android.graphics.*;
import android.graphics.drawable.*;
import com.editor.text.span.*;
import android.text.*;


public class XCode extends ViewGroup implements myEditCompletorListener.onOpenWindowLisrener
{
	private View mTitle;
	private PageHandler mPageHandler;
	private View mDownBar;
	private ViewGroup mWindow;
	private ThreadPoolExecutor mPool;
	
	public XCode(Context cont)
	{
		super(cont);
		init(cont);
		config();
		load();
	}
	private void init(Context cont){
		mWindow = new myWindow(cont);
		mPageHandler = new PageHandler(cont);
	}
	private void config(){
		mWindow.setBackgroundColor(0xff1e2126);
		setBackgroundDrawable(new ColorDrawable(/*0xffffffff*/0xff222222));
	}
	private void load(){
		//addView(mPageHandler);
	}
	public void setPool(ThreadPoolExecutor pool){
		mPool = pool;
	}
	
	public void loadFileInThread(final String path)
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				final CodeEdit E = new CodeEdit(getContext());
				myReader reader = new myReader(path);
				String text = reader.r("UTF-8");
				E.setText(text,0,text.length());
				int len = text.length();
				if(len<=1000000){
					E.reDrawText(0,len);
				}
				
				E.setPool(mPool);
				E.setWindowListener(XCode.this);
				Runnable run2 = new Runnable()
				{
					@Override
					public void run()
					{
						addView(E);
						//mPageHandler.addView(E,path);
						E.getLayoutParams().width=1080;
						E.getLayoutParams().height=2180;
					    int len = E.getText().length();
						if(len>1000000){
							E.reDrawTextContinuous(0,len);
						}
					}
				};
				post(run2);
			}
		};
		mPool.execute(run);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if(getChildCount()>0){
			getChildAt(0).layout(l,t,r,b);
		}
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
		protected void onLayout(boolean p1, int l, int t, int r, int b){}

	}
	
	@Override
	public void callOnOpenWindow(int x, int y, int width, int height)
	{
		mWindow.setVisibility(VISIBLE);
		ViewGroup.LayoutParams parms = mWindow.getLayoutParams();
		parms.width = width;
		parms.height = height;
		mWindow.layout(x,y,x+width,y+height);
	}

	@Override
	public void callOnCloseWindow(){
		mWindow.setVisibility(GONE);
	}

	@Override
	public void callOnRefreshWindow(View content, int l, int t, int r, int b)
	{
		mWindow.removeAllViews();
		mWindow.addView(content);
		ViewGroup.LayoutParams parms = content.getLayoutParams();
		parms.width = r-l;
		parms.height = b-t;
		content.layout(l,t,r,b);
	}
	
}
