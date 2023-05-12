package com.mycompany.who;

import android.content.res.*;
import android.os.*;
import android.view.*;
import com.mycompany.who.Activity.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.SuperVisor.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;
import java.util.concurrent.*;
import android.os.Handler;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;


public class MainActivity extends BaseActivity2 implements Runnable,CodeBlock
{
	private XCode Code;
	private PageHandler handler;
	private EditGroup Group;
	protected ThreadPoolExecutor pool;
	private myLog log=new myLog("/storage/emulated/0/Linux/share.html");
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dismiss_Title(this);
		new Handler().post(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//每次从后台切回前台都会重新调用
	}
	
	@Override
	public void run()
	{
		init();
		config();
		int tmp = 0;
		if(Displaywidth>Displayheight)
			tmp=Configuration.ORIENTATION_LANDSCAPE;
		else
			tmp = Configuration.ORIENTATION_PORTRAIT;
		loadSize(Displaywidth,Displayheight,tmp);
	}
	
	public void init(){
		Code = new XCode(this);
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
				} 
				catch (Exception e) {}
			}
		};
		LinkedBlockingQueue queue = new LinkedBlockingQueue();
		pool = new ThreadPoolExecutor(5, 20, 0, TimeUnit.SECONDS, queue, rejected);
	}
	
	public void config(){
		Code.config();
		Code.setPool(pool);
		getWindow().setStatusBarColor(Colors.Bg);
		getWindow().setNavigationBarColor(Colors.Bg);
		CodeEdit.Enabled_Drawer = true;
		CodeEdit.Enabled_Complete = true;
		CodeEdit.Enabled_Format = true;
	}
	
	@Override
	public void loadSize(int width, int height, int is)
	{
		setContentView(Code);	
		Code.loadSize(width,height,is);
		//Code.addEdit("/storage/emulated/0/Linux/1.java");	
		//Code.addEdit("/storage/emulated/0/AppProjects/教程/AIDE/tmp.java");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/src/com/mycompany/mygame/MainActivity.java");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/res/layout/main.xml");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game/src/com/mycompany/mygame/MyGdxGame.java");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/AndroidManifest.xml");
		
		/*
		Group = (EditGroup) Code.getPages().getView(0);
		final EditGroup.EditBuilder b = Group.getEditBuilder();
		final List<Future> r = b.prepare(0,b.calaEditLen());
		pool.execute(new Runnable(){

				@Override
				public void run()
				{
					FuturePool.FuturePop(r);
					StringBuilder bu = new StringBuilder();
					b.GetString(bu,null);
					log.e(bu.toString(),true);
				}
			});
		*/
	}

	@Override
	public void ShiftConfig(Level Configer)
	{
		Code.ShiftConfig(Configer);
	}

	@Override
	public Config_Size getConfig()
	{
		return Code.getConfig();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		/*
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
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		Code.getConfig().change(Code,newConfig.orientation);
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE)
			dismiss_ActionBar(this);
		else if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
			restore_ActionBar(this);
		}
	}
	
}







