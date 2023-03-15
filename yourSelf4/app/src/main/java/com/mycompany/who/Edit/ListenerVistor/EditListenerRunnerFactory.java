package com.mycompany.who.Edit.ListenerVistor;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;


public class EditListenerRunnerFactory
{
	//获取安全的Runner
	public static EditListenerRunner getDrawerRunner(){
		return new DR();
	}
	public static EditListenerRunner getFormatRunner(){
		return new MR();
	}
	public static EditListenerRunner getCompleteRunner(){
		return new CR();
	}
	public static EditListenerRunner getCanvasRunner(){
		return new VR();
	}
	
	public static class BR implements EditListenerRunner
	{

		@Override
		public List<wordIndex> FindForLi(int start, int end, String text, Words WorLib, EditFinderListener li)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public void DrawingForLi(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder, Editable editor, EditDrawerListener li)
		{
			// TODO: Implement this method
		}

		@Override
		public String FormatForLi(int start, int end,String src, EditFormatorListener li)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public int InsertForLi(Editable editor, int nowIndex, EditInsertorListener li)
		{
			// TODO: Implement this method
			return nowIndex;
		}

		@Override
		public List<Icon> CompeletForLi(String wantBefore, String wantAfter, int before, int after,Words lib,EditCompletorListener li)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public void CanvaserForLi(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds, EditCanvaserListener li)
		{
			// TODO: Implement this method
		}
	}
	
	public static class DR extends BR
	{
		@Override
		public List<wordIndex> FindForLi(int start, int end, String text, Words WordLib,  EditFinderListener li)
		{
			if (!EditListener.Enabled(li))
				return null;
			
			List<BaseEdit. DoAnyThing> totalList =new ArrayList<>() ;
			List<wordIndex> nodes=new ArrayList<>();
			
			try
			{
				li.OnFindWord(totalList, WordLib);
				BaseEdit. startFind(text, totalList,nodes);
				totalList.clear();
				li.OnClearFindWord(WordLib);
				li.OnFindNodes(totalList,WordLib);
				BaseEdit. startFind(text, totalList,nodes);
				li.OnClearFindNodes(start, end, text, nodes);
			}
			catch (Exception e)
			{
				Log.e("Finding Error", li.toString()+" "+e.toString());
				return null;
			}
			return nodes;
		}

		@Override
		public void DrawingForLi(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder, Editable editor, EditDrawerListener li)
		{
			try
			{
				if (EditListener.Enabled(li))
			        li.onDraw(start, end, nodes,builder, editor);
			}
			catch (Exception e)
			{
				Log.e("Drawing Error", li.toString()+" "+e.toString());
			}
		}
	}
	
    public static class MR extends BR
	{

		@Override
		public String FormatForLi(int start, int end, String src, EditFormatorListener total)
		{
			if (!EditListener.Enabled(total))
				return src.substring(start, end);

			EditFormatorListener.ModifyBuffer buffer=new EditFormatorListener.ModifyBuffer(start, src, src.substring(start, end));
			
			int beforeIndex = 0;
			int nowIndex=start;
			try
			{
			    nowIndex = total.dothing_Start(buffer, nowIndex, start, end);
			
				for (;nowIndex < end && nowIndex != -1;)
				{
					beforeIndex = nowIndex;
					nowIndex = total.dothing_Run(buffer, nowIndex);
				}
			    nowIndex =  total.dothing_End(buffer, beforeIndex, start, end);		
			}
			catch (IndexOutOfBoundsException e)
			{
				Log.e("Formating Error", total.toString()+" "+e.toString());
				return src.substring(start, end);
				//格式化的过程中出现了问题，返回原字符串
			}
			return buffer.toString();
		}

		@Override
		public int InsertForLi(Editable editor, int nowIndex, EditInsertorListener total)
		{
			int newIndex = 0;
			try
			{
				if (EditListener.Enabled(total))
					newIndex= total.dothing_insert(editor, nowIndex);
			}
			catch (IndexOutOfBoundsException e)
			{
				Log.e("Inserting Error", total.toString()+" "+e.toString());
				return nowIndex;
			}
			return newIndex;
		}
		
	}
	
	public static class CR extends BR
	{

		@Override
		public List<Icon> CompeletForLi(String wantBefore,String wantAfter,int before,int after,Words Wordlib,EditCompletorListener li)
		{
			Collection<String> lib;
			List<String> words = null;
			List<Icon> Adapter=new ArrayList<>();
			if(!EditListener.Enabled(li))
				return Adapter;

			try{
				lib = li.onBeforeSearchWord(Wordlib);
				if (lib != null && lib.size() != 0)
				{
					words =CodeEdit. SearchOnce(wantBefore, wantAfter, lib, before, after);
				}
				li.onFinishSearchWord(words, Adapter);
			}catch(Exception e){
				Log.e("Completing Error", li.toString()+" "+e.toString());
			}
			return Adapter;
		}

	}
	
	public static class VR extends BR
	{

		@Override
		public void CanvaserForLi(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds, EditCanvaserListener li)
		{
			try{
				if(EditListener.Enabled(li))
					li.onDraw(self,canvas,paint,Cursor_bounds);
			}catch(Exception e){
				Log.e("Canvaser Error", li.toString()+" "+e.toString());
			}
		}
	}
	
	
}
