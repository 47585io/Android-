package com.mycompany.who.Edit.DrawerEdit;
import android.content.*;
import android.os.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import android.text.*;
import android.widget.*;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import android.graphics.*;
import com.mycompany.who.Edit.Share.*;

public abstract class DrawerBase2 extends DrawerBase
{
	public static int Delayed_Draw = 0;

	protected EditListener mlistenerF;
	protected EditListener mlistenerD;
	protected EditListenerRunner Runner;
	protected ThreadPoolExecutor pool;

	public DrawerBase2(Context cont)
	{
		super(cont);
		mlistenerD = new DefaultDrawerListener();
	}
	public DrawerBase2(Context cont, DrawerBase2 Edit)
	{
		super(cont, Edit);
		mlistenerD = new DefaultDrawerListener();
		//为了保证内存安全，listener正式分开使用
		pool = Edit. pool;
	}
	public DrawerBase2(Context cont, AttributeSet set)
	{
		super(cont, set);
		mlistenerD = new DefaultDrawerListener();
	}
	@Override
	public void reSet()
	{
		super.reSet();
		clearListener();
		mlistenerD = new DefaultDrawerListener();
	}

	public void clearListener()
	{
		mlistenerF=null;
		mlistenerD=null;
	}
	public void setFinder(EditListener li)
	{
		mlistenerF = li;
	}
	public void setDrawer(EditListener li)
	{
		mlistenerD = li;
	}
	public EditListener getFinder()
	{
		return mlistenerF;
	}
	public EditListener getDrawer()
	{
		return mlistenerD;
	}

	public void setRunner(EditListenerRunner run)
	{
		Runner = run;
	}
	public EditListenerRunner getRunner()
	{
		return Runner;
	}
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
	}
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}

	protected ArrayList<wordIndex> FindFor(int start, int end, String text)
	{
		if(Runner!=null)
		    return Runner.FindForLi(start, end, text, WordLib, WordLib2, (EditFinderListener)getFinder());
		return null;
	}

	@Override
	protected void Drawing(int start, int end, ArrayList<wordIndex> nodes)
	{
		IsModify++;
		isDraw = true;
		if(Runner!=null)
		    Runner.DrawingForLi(start, end, nodes, this, (EditDrawerListener)getDrawer());
	   	IsModify--;
		isDraw = false;
	}


	public class DefaultDrawerListener extends EditDrawerListener
	{
		@Override
		public void onDraw(final int start, final int end, final ArrayList<wordIndex> nodes, final EditText self)
		{
			if (Delayed_Draw != 0)
			{
				Handler handler=new Handler();
				handler.postDelayed(new Runnable(){

						@Override
						public void run()
						{
							((DrawerBase)self). Draw(start, end, nodes);
						}
					}, Delayed_Draw);
				/*把一个字符的颜色先重置为白色，然后再短暂延迟后再染色，
				 就出现了高亮，就是那种会发光的感觉！*/
			}
			else
				((DrawerBase)self). Draw(start, end, nodes);
		}
	}

	
	protected EditListener getDefaultDrawer(){
		return new DefaultDrawerListener();
	}
	
}
