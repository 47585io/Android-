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
		getWindow().setBackgroundDrawable(null);
		init();
		Group=new EditGroup(this);
		setContentView(Group);
		Group.loadSize(1000,2000,true);
		Group.AddEdit(".java");
		Group.setPool(pool);
		Group.getEditBuilder().setRunner(EditRunnerFactory.getCanvasRunner());
	}
	
	protected void init(){
		RejectedExecutionHandler rejected = new RejectedExecutionHandler(){

			@Override
			public void rejectedExecution(Runnable p1, ThreadPoolExecutor p2)
			{
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
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		// 初始化线程池
		pool = new ThreadPoolExecutor(5, 15, 0, TimeUnit.SECONDS, queue, rejected);

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







