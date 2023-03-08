package com.mycompany.who.Edit.DrawerEdit;
import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.DrawerBase.*;
import android.text.*;

abstract public class DrawerBaseForLuagua extends DrawerBaseForAnyThing
{
	//所有语言工厂
	DrawerBaseForLuagua(Context cont){
		super(cont);
	}
	DrawerBaseForLuagua(Context cont,DrawerBaseForLuagua Edit){
		super(cont,Edit);
	}
	
	public class FinderText extends EditFinderListener
	{

		@Override
		public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{

		}

		@Override
		public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList, Words WordLib)
		{
			// TODO: Implement this method
			AnyThingForText AllThings = new AnyThingForText();
			totalList.add(AllThings.getGoTo_zhuShi());
			totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getNoSans_Char());
		}

		@Override
		public void OnClearFindWord(Words WordLib)
		{

		}

		@Override
		public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
		{

		}
	}
	
	public class FinderXML extends EditFinderListener
	{

		@Override
		public void OnClearFindNodes(int start, int end, String text, List<wordIndex> nodes)
		{
			// TODO: Implement this method
		}


		@Override
		public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{

		}

		@Override
		public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{
			AnyThingForXML AllThings = new AnyThingForXML();

			totalList.clear();
			totalList.add(AllThings.getGoTo_zhuShi());	
			totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getDraw_Tag());

			totalList.add(AllThings.getDraw_Attribute());	

			totalList.add(AllThings.getNoSans_Char());
		}

		@Override
		public void OnClearFindWord(Words WordLib)
		{

		}
	}
	
	
	final public class FinderJava extends EditFinderListener
	{

		@Override
		public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{
			AnyThingForJava AllThings = new AnyThingForJava();

			totalList.add(AllThings.getSans_TryFunc());	
			totalList.add(AllThings.getSans_TryVillber());
			totalList.add(AllThings.getSans_TryType());
			totalList.add(AllThings.getSans_TryObject());
			totalList.add(AllThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词

		}

		@Override
		public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{
			AnyThingForJava AllThings = new AnyThingForJava();

		    totalList.add(AllThings.getGoTo_zhuShi());
		    totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getNoSans_Keyword());
		    totalList.add(AllThings.getNoSans_Func());
		    totalList.add(AllThings.getNoSans_Villber());
			totalList.add(AllThings.getNoSans_Object());
		    totalList.add(AllThings.getNoSans_Type());

			totalList.add(AllThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词

		}

		@Override
		public void OnClearFindWord(Words WordLib)
		{
			Array_Splitor. delSame(getLastfunc(),getKeyword());
			//函数名不可是关键字，但可以和变量或类型重名	
			Array_Splitor.delSame(getLastfunc(),getKeyword());
			//类型不可是关键字
			Array_Splitor.delSame(getBeforetype(),getHistoryVillber());
			//类型不可是变量，类型可以和函数重名
			Array_Splitor.delSame(getBeforetype(),getConstword());
			//类型不可是保留字
			Array_Splitor. delSame(getHistoryVillber(),getKeyword());
			Array_Splitor. delSame(getThoseObject(),getKeyword());
			//变量不可是关键字
			Array_Splitor. delSame(getThoseObject(),getConstword());
			Array_Splitor.delSame(getHistoryVillber(),getConstword());
			//变量不可是保留字
			Array_Splitor.delNumber(getBeforetype());
			Array_Splitor.delNumber(getHistoryVillber());
			Array_Splitor.delNumber(getLastfunc());
			Array_Splitor.delNumber(getThoseObject());
			//去掉数字
		}

		@Override
		public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
		{
			clearRepeatNode(nodes);	
		}
	}
	
	
	
	final public class FinderCSS extends EditFinderListener
	{

		@Override
		public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{

		}

		@Override
		public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{
			AnyThingForCSS CSSThings = new AnyThingForCSS();

			totalList.add(CSSThings.getGoTo_zhuShi());	
			totalList.add(CSSThings.getGoTo_Str());
		    totalList.add(new AnyThingForquick().getNoSans_Func());
			totalList.add(CSSThings.getCSSDrawer());
			totalList.add(CSSThings.getCSSChecker());


			totalList.add(CSSThings.getDraw_Attribute());	

			totalList.add(CSSThings.getNoSans_Tag());

			totalList.add(CSSThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词

		}

		@Override
		public void OnClearFindWord(Words WordLib)
		{

		}

		@Override
		public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
		{
			clearRepeatNodeForCSS(text,nodes);
		}
		
		final public void clearRepeatNodeForCSS(String src,List<wordIndex> nodes){
			//清除优先级低且位置重复的node
			int i;
			for(i=0;i<nodes.size();i++){
				wordIndex now = nodes.get(i);
				if(src.substring(now.start,now.end).equals("-")){
					nodes.remove(i);
					i--;
				}
			}
		}
	}
	
	final public class FinderHTML extends EditFinderListener
	{

		@Override
		public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList, Words WordLib)
		{
			// TODO: Implement this method
		}


		@Override
		public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib)
		{

		}

		@Override
		public void OnClearFindWord(Words WordLib)
		{

		}

		@Override
		public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
		{
		    reDrawHTML(start,end,text,nodes);
		}
		
		
		final protected List<wordIndex> getNodes(String text, String Lua, int now)
		{
			String L = laugua;
			setLuagua(Lua);
			List<wordIndex> tmp = new ArrayList<>();
			FindFor(0,0,text,tmp,new SpannableStringBuilder());
			offsetNode(tmp, now);
			setLuagua(L);
			return tmp;
		}

		final protected List<wordIndex> reDrawHTML(int start,int end,String text,List<wordIndex>nodes)
		{
			List<wordIndex> tmp=new ArrayList<>();
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

	}

}
