package com.mycompany.who.View;
import android.webkit.*;
import android.view.*;
import android.content.*;

public class WebViewer extends WebView
{
	
	public WebViewer(Context cont){
		super(cont);
		WebSettings setting= getSettings();
		setting.setJavaScriptEnabled(true);

		setting.setSupportZoom(true);
		setting.setJavaScriptCanOpenWindowsAutomatically(true);
		setting.setDomStorageEnabled(true);
		setting.setAllowFileAccess(true);
		setting.setAllowFileAccessFromFileURLs(true);
		setting.setJavaScriptEnabled(true);

		setWebViewClient(new WebViewClient());
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{
		if (p2.getPointerCount() == 2 && p2.getHistorySize() != 0)
		{
			if (
				(
				Math.sqrt(
					(
					Math.pow(
						Math.abs(p2.getX(0) - p2.getX(1)), 2
					)
					+
					Math.pow(
						Math.abs(p2.getY(0) - p2.getY(1)), 2
					)
					)
				)
				>
				(
				Math.sqrt(
					Math.pow(
						Math.abs(p2.getHistoricalX(0, p2.getHistorySize() - 1) - p2.getHistoricalX(1, p2.getHistorySize() - 1)), 2
					)		
					+
					Math.pow( 
						Math.abs(p2.getHistoricalY(0, p2.getHistorySize() - 1) - p2.getHistoricalY(1, p2.getHistorySize() - 1)), 2)
				)
				)
				)
				)		
			    zoomBy((float)1.05);
			else 
				zoomBy((float)0.95);
			
				
			}

		super.onTouchEvent(p2);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==event.KEYCODE_BACK){
			if(canGoBack())
			    goBack();        
			return true;
		}
		
	    return super.onKeyDown(keyCode,event);	
	}
	
	
}
