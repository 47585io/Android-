package com.editor.view;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.widget.AdapterView.*;
import com.editor.text2.*;
import java.util.concurrent.*;
import com.editor.*;
import com.editor.text2.builder.listenerInfo.listener.*;

public class XCode extends LinearLayout implements OnItemClickListener,myEditCompletorListener.onOpenWindowLisrener
{

	@Override
	public void callOnOpenWindow(View Window, float x, float y)
	{
		Window.setLeft((int)x);
		Window.setTop((int)y);
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		lastEdit.onItemClick(p1,p2,p3,p4);
	}
	
	private AdapterView mWindow;
	private CodeEdit lastEdit;
	private ThreadPoolExecutor mPool;
	
	
	public XCode(Context cont)
	{
		super(cont);
		mWindow = new ListView(cont);
		//addView(mWindow);
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
	
}
