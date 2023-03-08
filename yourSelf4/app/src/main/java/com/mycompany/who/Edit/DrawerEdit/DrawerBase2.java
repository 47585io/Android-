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
   在基类上开一些接口
   另外的，复杂的函数我都设置成了final
*/
public abstract class DrawerBase2 extends DrawerBase
{
	
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
	

	protected void FindFor(int start, int end, String text,List<wordIndex>nodes,SpannableStringBuilder builder)
	{
		Ep.start();
		if(Runner!=null)
		    Runner.FindForLi(start, end, text, WordLib, nodes,builder, (EditFinderListener)mlistenerF);
	}

	@Override
	protected void Drawing(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder)
	{
		IsModify++;
		isDraw = true;
		if(Runner!=null)
		    Runner.DrawingForLi(start, end, nodes,builder, getText(),(EditDrawerListener)getDrawer());
	   	IsModify--;
		isDraw = false;
		Ep.stop();
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
