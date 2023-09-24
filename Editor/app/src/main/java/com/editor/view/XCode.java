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
import com.editor.text.*;
import com.editor.text.base.*;


public class XCode extends ViewGroup implements OnItemClickListener,OnItemLongClickListener,myEditCompletorListener.onOpenWindowLisrener
{

	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		return lastEdit.onItemLongClick(p1,p2,p3,p4);
	}
	

	@Override
	public void callOnCloseWindow(View self)
	{
		mWindow.setVisibility(GONE);
	}

	@Override
	public void callOnRefreshWindow(View self, Object content)
	{
		ListAdapter adapter = (ListAdapter) content;
		mWindow.setAdapter(adapter);
		
	}

	@Override
	public void callOnOpenWindow(View self, Object content)
	{
		ListAdapter adapter = (ListAdapter) content;
		mWindow.setAdapter(adapter);
		
		lastEdit = (CodeEdit)self;
		final pos p = lastEdit.getSelectionStartPos();
		int x = (int) p.x;
		int y = (int) (p.y+lastEdit.getLineHeight());

		final int width = lastEdit.getWidth();
		int wantWidth = (int)(width*0.8);
		if(p.x+wantWidth > lastEdit.getScrollX()+width){
			x = lastEdit.getScrollX()+width-wantWidth;
		}

		final int height = lastEdit.getHeight();
		int wantHeight = measureWindowHeight(mWindow,height/2);
		if(p.y+wantHeight > lastEdit.getScrollY()+height){
			y = (int)p.y-wantHeight;
		}
		
		ViewGroup.LayoutParams parms = mWindow.getLayoutParams();
		parms.width = wantWidth;
		parms.height = wantHeight;
		mWindow.measure(0,0);
		mWindow.layout(x,y,x+wantWidth,y+wantHeight);
		mWindow.setVisibility(VISIBLE);
	}
	private static int measureWindowHeight(AdapterView Window, int maxHeight)
	{
		Adapter adapter = Window.getAdapter();
		if(adapter==null){
			return 0;
		}
		int height=0;
		int count = adapter.getCount();
		for (int i=0;i<count;++i)
		{
			View view = adapter.getView(i, null, Window);
			view.measure(0, 0);
			height += view.getMeasuredHeight();
			if(height>=maxHeight){
				return maxHeight;
			}
		}
		return height;
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
		mWindow.setOnItemLongClickListener(this);
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
						E.setWindow(XCode.this);
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
