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
import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share4.*;
import android.view.View.*;


/*
  无论如何，都要为你的工程保存一个最稳定的版本，免得之后出bug
  
  但是代码还是要大胆写的，还是那句话: bug又改不完，还不如随便写，大不了之后再改
*/

/*
 效率优化:

 1. 锁定EditText父元素宽高，避免重复测量
 2. 将Edit拆分成组，截取，均分文本
 3. dispatchTextBlock提前计算并均分文本
 4. clipRect限制Edit绘制范围，使onDraw时超出范围的绘制放弃
 5. LineGroup将Line拆分成组，防止文本过多

 6. ReDraw和openWindow使用线程查找
 7. Words使用HashSet，contians非常快
 8. maxHeight和maxWidth和行都不需要全部测量，每次文本变化时局部测量
 9. 屏蔽super.onTextChanged，禁止自动测量和滚动
 10.Ep和Epp重复利用空间
 11.设置Span而不是修改文本，使用SpannableStringBuilder而不是Spanned，效率快了几十倍

 11.EditLine的大量增删操作优化
 12.EditGroup初始加载时利用线程时差节省时间
 13.checkUnicode使用编码值判断，快了10倍
 14.quickSort快速排序
 15.Finder将StringBuffer换成StringBiilder，快了100ms
 16.优化trim延迟layout

*/

public class MainActivity extends BaseActivity2 implements Runnable,CodeBlock
{
	private XCode Code;
	protected ThreadPoolExecutor pool;
	private myLog log=new myLog("/storage/emulated/0/Linux/share.html");
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dismiss_Title(this);
		new Handler().post(this);
		View n;
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
	
	public void init()
    {
		Code = new XCode(this);
		RejectedExecutionHandler rejected = new RejectedExecutionHandler()
		{
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
	
	public void config()
	{
		Code.config();
		Code.setPool(pool);
		getWindow().setStatusBarColor(Colors.Bg);
		getWindow().setNavigationBarColor(Colors.Bg);
	}
	
	@Override
	public void loadSize(int width, int height, int is)
	{
		setContentView(Code);	
		Code.loadSize(width,height,is);
		//Code.addEdit("/storage/emulated/0/Linux/2.java");	
		//Code.addEdit("");
		Code.addEdit("/storage/emulated/0/AppProjects/教程/AIDE/tmp.java");
		//Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/src/com/mycompany/mygame/MainActivity.java");
		//Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/res/layout/main.xml");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game/src/com/mycompany/mygame/MyGdxGame.java");
		Code.addEdit("/storage/emulated/0/AppProjects/游戏/MyGame/gdx-game-android/AndroidManifest.xml");	
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
	
	public void PreAndOutput(int index)
	{
		EditGroup Group = (EditGroup) Code.getPages().getChildAt(index);
		final EditGroup.EditManipulator b = Group.getEditManipulator();
		final List<Future> r = b.prepare(0,b.calaEditLen());
		pool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				FuturePool.FuturePop(r);
				StringBuilder bu = new StringBuilder();
				b.GetString(bu,null);
				log.e(bu.toString(),true);
			}
		}); 
	}
	
}


