package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import com.mycompany.who.Edit.DrawerEdit.DrawerBase.*;

public class DrawerHTML extends DrawerCSS
{

	public DrawerHTML(Context cont)
	{
		super(cont);
	}
	public DrawerHTML(Context cont,DrawerHTML Edit)
	{
		super(cont,Edit);
	}
	public DrawerHTML(Context cont,AttributeSet set){
		super(cont,set);
	}

	@Override
	public void setLuagua(String name)
	{
		if (name.equals("html"))
			setFinder(new FinderHTML());
		super.setLuagua(name);
	}

	protected ArrayList<wordIndex> getNodes(String text, String Lua, int now)
	{
		EditListener finder=getFinder();
		setLuagua(Lua);
		ArrayList<wordIndex> tmp= FindFor(0,0,text);
		offsetNode(tmp, now);
		setFinder(finder);
		return tmp;
	}

	protected ArrayList<wordIndex> reDrawHTML(int start,int end,String text)
	{

		ArrayList<wordIndex> nodes=new ArrayList<>();
		ArrayList<wordIndex> tmp;
		int now=0,css=-1,js=-1,css_end=-1,js_end=-1;
		try
		{
			while (now != -1)
			{
				css = text.indexOf("<style", now);
				js = text.indexOf("<script", now);
				css_end = text.indexOf("</style", now);
				js_end = text.indexOf("</script", now);
				int min = Array_Splitor.getmin(0, text.length(), css, js, css_end, js_end);
				//找到符合条件的最近tag位置

				if (min == -1)
				{
					break;
					//范围内没有tag了
				}	
				else if (css == min)
				{
					css += 7;
					tmp = getNodes(text.substring(now, css), "xml", now);
					nodes.addAll(tmp);
					now = css;
					//如果是css起始tag，将之前的html染色
				}
				else if (js == min)
				{
					js += 8;
					tmp =  getNodes(text.substring(now, js), "xml", now);
					nodes.addAll(tmp);
					now = js;
					//如果是js起始tag，将之前的html染色
				}
				else if (css_end == min)
				{
					css_end += 8;
					tmp =	getNodes(text.substring(now, css_end), "css", now);
					nodes.addAll(tmp);
					now = css_end;
					//如果是css结束tag，将之间的CSS染色
				}
				else if (js_end == min)
				{
					js_end += 9;
					tmp =	getNodes(text.substring(now, js_end), "java", now);
					nodes.addAll(tmp);
					now = js_end;
					//如果是js结束tag，将之间的js染色
				}
			}

		}
		catch (Exception e)
		{}
		//那最后一段在哪个tag内呢？
		//只要看下个tag
		String s=getText().toString();
		css = s.indexOf("<style", now+start);
		js = s.indexOf("<script", now+start);
		css_end = s.indexOf("</style", now+start);
		js_end = s.indexOf("</script", now+start);
		
		int min = Array_Splitor.getmin(0,s.length(), css, js, css_end, js_end);
		try
		{
			if (min == -1)
			{
				tmp = getNodes(text.substring(now, text.length()), "xml", now);
				nodes.addAll(tmp);
				//范围内没有tag了
			}	
			else if (css == min)
			{
				tmp = getNodes(text.substring(now, text.length()), "xml", now);
				nodes.addAll(tmp);
				//如果是css起始tag，将之前的xml染色
			}
			else if (js == min)
			{
				tmp = getNodes(text.substring(now, text.length()), "xml", now);
				nodes.addAll(tmp);
				//如果是js起始tag，将之前的xml染色
			}
			else if (css_end == min)
			{
				tmp = getNodes(text.substring(now, text.length()), "css", now);
				nodes.addAll(tmp);
				//如果是css结束tag，将之前的css染色
			}
			else if (js_end == min)
			{
				tmp = getNodes(text.substring(now, text.length()), "java", now);
				nodes.addAll(tmp);
				//如果是js结束tag，将之前的js染色
			}
		}
		catch (Exception e)
		{}
		setLuagua("html");
		return nodes;
	}

	class FinderHTML extends EditFinderListener
	{

		@Override
		public void OnFindNodes(ArrayList<DrawerBase.DoAnyThing> totalList, Words WordLib, OtherWords WordLib2)
		{
			// TODO: Implement this method
		}
		

		@Override
		public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,Words WordLib,OtherWords WordLib2)
		{

		}

		@Override
		public void OnClearFindWord(Words WordLib,OtherWords WordLib2)
		{

		}

		@Override
		public void OnClearFindNodes(int start,int end,String text, ArrayList<wordIndex> nodes)
		{
			nodes.addAll(reDrawHTML(start,end,text));
		}


	}

}
