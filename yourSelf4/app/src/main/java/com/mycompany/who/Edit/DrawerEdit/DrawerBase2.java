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

/*
   在基类上开一些接口，另外的，复杂的函数我都设置成了final
   
   从现在开始，所有被调函数，例如Drawing，必须自己管理好线程和IsModify安全，然后将真正操作交给另一个函数
   
   在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份
*/
public abstract class DrawerBase2 extends DrawerBase
{
	
	public static int Delayed_Draw = 0;
	
	protected EditListener mlistenerF;
	protected EditListener mlistenerD;
	protected EditListenerRunner Runner;
	
	DrawerBase2(Context cont)
	{
		super(cont);
		mlistenerD = new DefaultDrawerListener();
	}
	DrawerBase2(Context cont, DrawerBase2 Edit)
	{
		super(cont, Edit);
		mlistenerF=Edit.getFinder();
		mlistenerD=Edit.getDrawer();
		Runner=Edit.getRunner();
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
	

	protected final void FindFor(int start, int end, String text,List<wordIndex>nodes,SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		Ep.start(); //开始记录
		onFindNodes(start,end,text,nodes,builder);
	}
	protected void onFindNodes(int start, int end, String text,List<wordIndex>nodes,SpannableStringBuilder builder){
		if(Runner!=null)
		    Runner.FindForLi(start, end, text, WordLib, nodes,builder, (EditFinderListener)mlistenerF);
	}

	@Override
	protected final void Drawing(final int start, final int end, final List<wordIndex> nodes,final SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		Runnable run= new Runnable(){

			@Override
			public void run()
			{
				IsModify++;
				isDraw = true; //会修改文本，isModify
				onDrawNodes(start,end,nodes,builder);
				isDraw = false;
				IsModify--;
				Ep.stop(); //Draw完后回收nodes
			}
		};
		if(Delayed_Draw==0)
			post(run);
		else
			postDelayed(run,Delayed_Draw);
		//为了线程安全，涉及UI操作必须抛到主线程	
	}
	protected void onDrawNodes(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder){
		//应该重写这个
		if(Runner!=null)
		    Runner.DrawingForLi(start, end, nodes,builder, getText(),(EditDrawerListener)getDrawer());
	}
	

	abstract public void setLuagua(String Lua);

	
	public static class DefaultDrawerListener extends EditDrawerListener
	{

		@Override
		public void onDraw(final int start, final int end, List<wordIndex> nodes, SpannableStringBuilder builder, Editable editor)
		{
			editor.replace(start, end, builder);
		}
	
	}

	
	protected static EditListener getDefaultDrawer(){
		return new DefaultDrawerListener();
	}
	
}
