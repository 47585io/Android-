package com.who.Edit;

import android.text.*;
import com.who.Edit.Base.Share.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.EditBuilder.*;
import com.who.Edit.EditBuilder.ListenerVistor.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

import static com.who.Edit.EditBuilder.ListenerVistor.EditListenerInfo.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.myEditDrawerListener.*;
import android.text.style.*;


public class CodeEditBuilder implements EditBuilder
{

	@Override
	public void SwitchLuagua(Object O, String Lua)
	{
		// TODO: Implement this method
	}

	@Override
	public void trimListener(EditListenerInfo Info)
	{
		Info.addAListener(new DrawerFactory.DefaultDrawer());
	}

	@Override
	public void loadWords(Words Lib)
	{
		// TODO: Implement this method
	}
	
	
	public static class DrawerFactory implements ListenerFactory
	{

		@Override
		public void SwitchListener(EditListenerInfo Info, String Lua)
		{
			
		}

		@Override
		public EditListener ToLisrener(String Lua){
			return null;
		}
		
		public static EditListener getDefualtDrawer(){
			return new DefaultDrawer();
		}
		
		
		public static class DefaultDrawer extends myEditDrawerListener
		{

			@Override
			protected void OnFindWord(List<myEditDrawerListener.DoAnyThing> totalList, Words WordLib){}

			@Override
			protected void OnFindNodes(List<myEditDrawerListener.DoAnyThing> totalList, Words WordLib)
			{
				final Random rand = new Random();
				totalList.add(new DoAnyThing(){

						@Override
						public int dothing(CharSequence src, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							nodes.add(new wordIndex(nowIndex,nowIndex+1,new ForegroundColorSpan(rand.nextInt())));
							return nowIndex;
						}

						@Override
						public Words getWords()
						{
							// TODO: Implement this method
							return null;
						}
					});
			}

			@Override
			protected void OnClearFindWord(Words WordLib){}

			@Override
			protected void OnClearFindNodes(int start, int end, CharSequence text, Words WordLib, List<wordIndex> nodes){}
			
			@Override
			public void onDrawNodes(int start, int end, Spannable editor)
			{
				clearSpan(start,end,editor);
				setSpan(start,end,editor,getDrawNodes());	
				//清理旧的Span，设置新的Span
			}

			public Colors.ByteToColor2 BToC = null;

			public Colors.ByteToColor2 getByteToColor(){
				return BToC;
			}
			public void setSpan(int start,int end,Spannable b,List<wordIndex> nodes){
				Colors.setSpans(0,b,nodes);
			}
			public void clearSpan(int start,int end,Spannable b){
				Colors.clearSpan(start,end,b,Colors.ForeSpanType);
			}	
		}
		
		
		public static class DoAnyThingFactory
		{
		
		}
		
	}
	
	public static ListenerFactory getDrawerFacrory()
	{
		return new DrawerFactory();
	}
	
}
