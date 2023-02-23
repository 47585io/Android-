package com.mycompany.who;

import android.content.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.mycompany.who.Activity.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Share.*;
import java.util.*;
import java.util.concurrent.*;
import android.view.animation.*;

public class MainActivity extends DownBarActivity 
{
//抱歉啊，主界面就是编辑器

	protected ThreadPoolExecutor pool;
	private myLog log;
	private Intent Setting,Viewer;
	public Toast mess;
	
	@Override
	public void TouchCollector()
	{
		super.TouchCollector();
		setOnTouchListenrS(re, Code);
	}

	protected void initActivity()
	{
		super.initActivity();
		pool=new ThreadPoolExecutor(2,6,1000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
		log=new myLog("/storage/emulated/0/Linux/share.html");	
		Setting=new Intent();
		Viewer=new Intent();
		mess=new Toast(this);
	
	}
	public void configActivity()
	{
		super.configActivity();
		TouchCollector();
		
		//ForEdit("/storage/emulated/0/Linux/share长长长长长长时间了吗丁啉.html");			
		//ForNotEdit("大猫.jpg");
		Setting.setClass(this, SetingPage.class);
		Viewer.setClass(this,Viewer.class);
	}

	@Override
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	public int getTiHeight()
	{
		return TiHeight;
	}
	

	@Override
	public void onMyMenuCreat(ArrayAdapter menu)
	{
		menu.add("寻找单词           ");
		menu.add("设置语言");
		menu.add("染色");
		menu.add("对齐文本");
		menu.add("保存文本");
		menu.add("设置               ");
		super.onMyMenuCreat(menu);
	}

	@Override
	public void onMyMenuSelected(int postion)
	{
		//任何按扭，操作的都是当前的编辑器，它们的对象是可变的，因此getNowIndex
		CodeEdit Edit = files.getEditAt(files.getNowIndex());
		if (Edit == null && postion != 6)
			return;
		String HTML=null;
	    switch (postion)
		{
			case 0: 
				portText.append(Edit.reDrawOtherText(Edit.getText().toString().substring(0,60)));
				break;
			case 1:
				break;
			case 2:
				HTML = Edit.reDraw(Edit.getSelectionStart(),Edit.getSelectionEnd());
				if(HTML!=null)
					log.e(HTML,true);
				break;
			case 3:
				Edit.startFormat(0,Edit.getText().length());
				break;
			case 4:
				break;
			case 5:
				break;
			default:
			    break;
		}
		super.onMyMenuSelected(postion);
	}

	
	public void TitleButtonBar_V(int index)
	{
		getBarButton(index).setVisibility(View.VISIBLE);
		getBarButton(index).setBackgroundResource(R.drawable.image);
		getBarButton(index).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Viewer.putExtra("url", files.getPageAt(files.getNowIndex()).getPath());
					startActivity(Viewer);
				}
			});
		messBarButton(index,"视图");
	}

	@Override
	protected void clearFloatWindow()
	{
		super.clearFloatWindow();
		
		floatWindow.setX(-9999);
		floatWindow.setY(-9999);

		
	}
	
	

}







