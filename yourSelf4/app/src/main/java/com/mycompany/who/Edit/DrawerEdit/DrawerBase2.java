package com.mycompany.who.Edit.DrawerEdit;
import android.content.*;
import android.os.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class DrawerBase2 extends DrawerBase
{
	public static int Delayed_Draw = 0;
	
	protected EditListener mlistenerF=null;
	protected ArrayList<EditListener> mlistenerFS;
	protected EditListener mlistenerD=null;
	protected ArrayList<EditListener> mlistenerDS;
	
	protected ThreadPoolExecutor pool;
	
	public DrawerBase2(Context cont){
		super(cont);
		mlistenerD=new DefaultDrawerListener();
		mlistenerFS=new ArrayList<>();
		mlistenerDS=new ArrayList<>();
	}
	@Override
	public void reSet()
	{
		//reset listener
		super.reSet();
	}
	
	public void clearListener(){
		setDefaultFinder( null);
		setDefaultFinder( null);
		getFinderList().clear();
		getDrawerList().clear();
	}
	public void setDefaultFinder(EditListener li){
		mlistenerF=li;
	}
	public void setDefaultDrawer(EditListener li){
		mlistenerD=li;
	}
	public EditListener getDefaultFinder(){
		return mlistenerF;
	}
	public EditListener getDefaultDrawer(){
		return mlistenerD;
	}
	public ArrayList<EditListener> getFinderList(){
		return mlistenerFS;
	}
	public ArrayList<EditListener> getDrawerList(){
		return mlistenerDS;
	}
	public void setPool(ThreadPoolExecutor pool){
		this.pool=pool;
	}
	public ThreadPoolExecutor getPool(){
		return pool;
	}
	
	@Override
	protected ArrayList<wordIndex> FindFor(int start,int end,String text)
	{
		ArrayList<wordIndex> nodes = new ArrayList<>();
		//nodes.addAll( FindFor1(start,end,text));
		nodes.addAll( FindFor2(start,end,text));
		return nodes;
	}

	protected ArrayList<wordIndex> FindFor2(int start,int end,String text){
		ArrayList<EditListener> m=new ArrayList<>();
		m.add(getDefaultFinder());
		m.addAll(getFinderList());
		if(getPool()==null)
		    return FindForGroup(m,start,end,text);
		else
			return FindForGroup(m,start,end,text,getPool());
	}
	
	protected ArrayList<wordIndex> FindForGroup(ArrayList<EditListener> m,final int start,final int end,final String text){
		ArrayList<wordIndex> nodes = new ArrayList<>();
		for(final EditListener li:m){
			if(li!=null)
			    nodes.addAll( FindForLi(li,start,end,text));
		}
		return nodes;
	}
	
	protected ArrayList<wordIndex> FindForGroup(ArrayList<EditListener> m,final int start,final int end,final String text,ThreadPoolExecutor pool){
		ArrayList<wordIndex> nodes = new ArrayList<>();
		ArrayList<Future<ArrayList<wordIndex>>> results= new ArrayList<>();
		for(final EditListener li:m){
			if(li==null)
				continue;
			Callable<ArrayList<wordIndex>> ca = new Callable<ArrayList<wordIndex>>(){

				@Override
				public ArrayList<wordIndex> call() throws Exception
				{
					return FindForLi(li,start,end,text);
				}
			};
			results.add( pool.submit(ca));
		}
		try
		{
			for (Future<ArrayList<wordIndex>> result:results)
				nodes.addAll(result.get());
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return nodes;
	}
	
	protected ArrayList<wordIndex> FindForLi(EditListener li,int start,int end,String text){
		ArrayList<DoAnyThing> totalList = new ArrayList<>();
		ArrayList<wordIndex> nodes = null;
		
		((EditFinderListener)li).OnFindWord(totalList);
		startFind(text,totalList);
		totalList.clear();
		((EditFinderListener)li).OnClearFindWord(WordLib,WordLib2);
		((EditFinderListener)li).OnDrawWord(totalList);
		nodes= startFind(text,totalList);
		((EditFinderListener)li).OnClearDrawWord(start,end,text,nodes);
		return nodes;
	}
	

	@Override
	protected void Drawing( int start, int end, ArrayList<wordIndex> nodes)
	{
	    if(getDefaultDrawer()!=null)
			( (EditDrawerListener)getDefaultDrawer()).onDraw(start,end, nodes);
		if(getDrawerList()!=null){
			for(EditListener li:getDrawerList()){
				if(li!=null)
				    ( (EditDrawerListener)   li).onDraw(start,end,nodes);
	        }
		}
	}

	
	public class DefaultDrawerListener extends EditDrawerListener
	{
		@Override
		public void onDraw(final int start, final int end, final ArrayList<wordIndex> nodes)
		{
			if(Delayed_Draw!=0){
				Handler handler=new Handler();
				handler.postDelayed(new Runnable(){

						@Override
						public void run()
						{
							Draw(start,end,nodes);
						}
					}, Delayed_Draw);
				/*把一个字符的颜色先重置为白色，然后再短暂延迟后再染色，
				 就出现了高亮，就是那种会发光的感觉！*/
			}
			else
				Draw(start,end,nodes);
		}
	}
}
