package com.mycompany.who;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;

public class Viewer extends Activity
{

	private WebViewer web;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		
		Intent date=getIntent();
		String url=date.getStringExtra("url");
		web=new WebViewer(this);
			
		setContentView(web);
		web.loadUrl(url);
		//web.loadData(url,"text/html","UTF-8");
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
			this.finish();
		return super.onKeyUp(keyCode, event);
	}
	
	
	
}