package com.mycompany.who;

import android.app.*;
import android.os.*;
import android.view.*;
import com.mycompany.who.Share.*;
import com.mycompany.who.SuperVisor.Moudle.*;
import java.util.concurrent.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.*;
import com.mycompany.who.Activity.*;
import android.content.res.*;
import java.util.*;
import com.mycompany.who.Edit.Share.Share4.*;

public class MainActivity extends BaseActivity2 implements Runnable
{

	private XCode Code;
	private PageHandler handler;
	private EditGroup Group;
	protected ThreadPoolExecutor pool;
	private myLog log=new myLog("/storage/emulated/0/Linux/share.html");
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		can=true;
		super.onCreate(savedInstanceState);
		init();
		Code = new XCode(this);
		Code.config();
		Code.setPool(pool);
		setContentView(Code);	
		
		new Handler().postDelayed(this,50);
		
		/*
		Group = (EditGroup) Code.getPages().getView(0);
		EditGroup.EditBuilder b = Group.getEditBuilder();
		List<Future> r = b.prepare(0,b.calaEditLen());
		FuturePool.FuturePop(r);
		StringBuilder bu = new StringBuilder();
		b.GetString(bu,null);
		log.e(bu.toString(),true);
		*/
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//每次从后台切回前台都会重新调用
	}
	
	
	protected void init(){
		RejectedExecutionHandler rejected = new RejectedExecutionHandler(){

			@Override
			public void rejectedExecution(Runnable p1, ThreadPoolExecutor p2)
			{
				//制定拒绝策略，避免线程池溢出后直接丢弃任务
				try {
					// 等待1秒后，尝试将当前被拒绝的任务重新加入线程队列
					// 此时主线程是会被阻塞的
					Thread.sleep(1000);
					p2.execute(p1);
				} catch (Exception e) {
				}
			}
		};
		// 将线程池队列设置为有界队列
		LinkedBlockingQueue queue = new LinkedBlockingQueue();
		// 初始化线程池
		pool = new ThreadPoolExecutor(5, 20, 0, TimeUnit.SECONDS, queue, rejected);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{/*
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
			case 4:
				Group.scrollTo(0,0);
				break;
		}*/
		return super.onOptionsItemSelected(item);
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,0,0,"Uedo");
		menu.add(1,1,1,"Redo");
		menu.add(2,2,2,"Format");
		menu.add(3,3,3,"reDraw");
		menu.add(4,4,4,"GoBack");
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public void run()
	{
		int tmp = 0;
		if(Displaywidth>Displayheight)
			tmp=Configuration.ORIENTATION_LANDSCAPE;
		else
			tmp = Configuration.ORIENTATION_PORTRAIT;
		
		Code.loadSize(Displaywidth,Displayheight,tmp);
		Code.addEdit("/storage/emulated/0/AppProjects/教程/AIDE/tmp.java");
		//Code.addEdit("/storage/emulated/0/Linux/1.java");
	}
	
}







