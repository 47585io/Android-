package com.who;

import android.app.*;
import android.os.*;
import android.view.*;
import com.who.Edit.Base.*;
import java.util.concurrent.*;
import android.text.*;

public class MainActivity extends Activity implements Runnable
{
	
	protected ThreadPoolExecutor pool;
	protected LinkedBlockingQueue<Runnable> queue;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		runOnUiThread(this);
    }
	
	@Override
	public void run()
	{
		init();
		config();
	}

	public void init()
    {
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
		queue = new LinkedBlockingQueue<>();
		pool = new ThreadPoolExecutor(5, 20, 0, TimeUnit.SECONDS, queue, rejected);
	}
	
	public void config()
	{
		loadFileInThread("/storage/emulated/0/Linux/1.java");
	}
	
	public void loadFileInThread(String path)
	{
		myReader reader = new myReader(path);
		final String text = reader.r("UTF-8");
		reader.close();
		
		//第一次可以在子线程中加载
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				final Edit E = new Edit(MainActivity.this);
				E.setText(text);
				E.setBackgroundColor(0xff222222);
				//还没有setContentView，因此Edit未与主线程建立联系，还只是一块内存而已
				
				runOnUiThread(new Runnable()
				    {
						@Override
						public void run()
						{
							setContentView(E);
							//将Edit添加到DecorView中，并开始绘制和分发事件
							ViewGroup.LayoutParams pa = E.getLayoutParams();
							pa.width = 1080;
							pa.height = 2100;
							//在将Edit添加到DecorView中时，DecorView已经给Edit设置了一个LayoutParams
							
							//然后我们与输入法建立连接
							Edit.openInputor(MainActivity. this, E);
							float y = E.getVScrollRange();
							E.scrollTo(0,(int)y);
						}
					});
			}
		};
		pool.execute(run);
	}
	
	public void loadFile(String path)
	{
		myReader reader = new myReader(path);
		final String text = reader.r("UTF-8");
		reader.close();
		
		final Edit E = new Edit(MainActivity.this);
		E.setText(text);
		E.setBackgroundColor(0xff222222);
		
		setContentView(E);
		ViewGroup.LayoutParams pa = E.getLayoutParams();
		pa.width = 1080;
		pa.height = 2100;
		
		Edit.openInputor(MainActivity. this, E);
		float y = E.getVScrollRange();
		E.scrollTo(0,(int)y);
	}
}



