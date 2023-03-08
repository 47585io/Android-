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
import com.mycompany.who.SuperVisor.*;
import android.os.*;
import android.app.*;
import java.security.cert.*;
import com.mycompany.who.Edit.Share.*;

public class MainActivity extends Activity 
{
	private EditGroup Group;
	protected ThreadPoolExecutor pool;
	private myLog log=new myLog("/storage/emulated/0/Linux/share.html");
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		pool=new ThreadPoolExecutor(2,6,1000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
		Group=new EditGroup(this);
		setContentView(Group);
		Group.AddEdit(".java");
		Group.setPool(pool);
		Group.getEditBuilder().setRunner(EditRunnerFactory.getCanvasRunner());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case 0:
				Group.getEditBuilder().Uedo();
				break;
			case 1:
				Group.getEditBuilder().Redo();
				break;
			case 2:
				Group.getEditBuilder().Format();
				break;
			case 3:
				String src= Group.getEditBuilder().reDraw();
				log.e(src,true);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,0,0,"Uedo");
		menu.add(1,1,1,"Redo");
		menu.add(2,2,2,"Format");
		menu.add(3,3,3,"reDraw");
		return super.onCreateOptionsMenu(menu);
	}
	
	
	
}







