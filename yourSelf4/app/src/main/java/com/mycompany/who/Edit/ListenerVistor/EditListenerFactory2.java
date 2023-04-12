package com.mycompany.who.Edit.ListenerVistor;

import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share1.*;
import com.mycompany.who.Edit.Share.Share2.*;
import com.mycompany.who.Edit.Share.Share3.*;
import java.util.*;


/*
  各种Listener的工厂，可以直接get使用，或者继承，全部都是static

  若没有特殊情况，Extension关我什么事
  
*/
public class EditListenerFactory2 implements EditListenerFactory
{

	@Override
	public void clearListener(EditText Edit)
	{
		CodeEdit E = (CodeEdit) Edit;
		E.getFinderList().clear();
		E.setDrawer(null);
		E.setFormator(null);
		E.getInsertorList().clear();
		E.getCompletorList().clear();
		E.getCanvaserList().clear();
	}


	@Override
	public void trimListener(EditText Edit)
	{
		CodeEdit E = (CodeEdit) Edit;
		E. setDrawer(EditListenerFactory2.DrawerFactory.getDefaultDrawer());
		E.getCanvaserList().add(EditListenerFactory2.CanvaserFactory.getDefultCanvaser());
		E.setFormator(EditListenerFactory2.FormatorFactory.getJavaFormator());
		E.getInsertorList().add((EditListenerFactory2.InsertorFactory.getDefultInsertor()));
		//E.getFinderList().add((EditListenerFactory2.FinderFactory.getTextFinder()));
		List<EditListener> cs=E.getCompletorList();
		cs.add(EditListenerFactory2.EditCompletorBoxes.getVillBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getObjectBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getFuncBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getTypeBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getDefaultBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getKeyBox());
		cs.add(EditListenerFactory2.EditCompletorBoxes.getTagBox());
	}
	

	@Override
	public void SwitchLuagua(EditText self, String Lua)
	{
		getFinderFactory().SwitchListener(self,Lua);
		getDrawerDactory().SwitchListener(self,Lua);
		getFormatorFactory().SwitchListener(self,Lua);
		getInsertorFactory().SwitchListener(self,Lua);
		getCompletorBox().SwitchListener(self,Lua);
		getCanvaserFactory().SwitchListener(self,Lua);
	}

/*
    ___________________________________________________________________________________________________________________________
    
	DrawerFactory
	
	->  DefaultDrawer

	___________________________________________________________________________________________________________________________
	
*/
	public static class DrawerFactory implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			return getDefaultDrawer();
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
		}

		public static EditListener getDefaultDrawer()
		{
			return new DefaultDrawerListener();
		}


		public static class DefaultDrawerListener extends EditDrawerListener
		{
			@Override
			public void onDraw(final int start, final int end,String src, List<wordIndex> nodes,  EditText self)
			{
				CodeEdit E = (CodeEdit)self;
				E.clearSpan(start,end,Colors.SpanType);
				wordIndex[] tmp = nodes.toArray(new wordIndex[nodes.size()]);
				Colors.ForeColorText(self.getText(),tmp,start,null);
				//清理旧的Span，设置新的Span
			}

		}

	}


/*	___________________________________________________________________________________________________________________________

    CanvaserFactory
	
	->  DefaultCanvaser
	
	___________________________________________________________________________________________________________________________
*/
	
	public static class CanvaserFactory implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			return getDefultCanvaser();
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
			// TODO: Implement this method
		}

		public static EditListener getDefultCanvaser()
		{
			return new DefaultCanvaser();
		}


		public static class DefaultCanvaser extends EditCanvaserListener
		{

			@Override
			public void afterDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
			{
				// TODO: Implement this method
			}


			@Override
			public void onDraw(EditText self, Canvas canvas, TextPaint paint, Rect bounds)
			{
				//设置画笔的描边宽度值
				paint.setStrokeWidth(0.2f);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);

				//任何修改都会触发重绘，这里在光标位置画矩形

				paint.setColor(CodeEdit.CursorRect_Color);
				canvas.drawRect(bounds, paint);
			}
		}

	}


/*	___________________________________________________________________________________________________________________________

	FormatorFactory
	
	->  DefaultFormator
	 
	___________________________________________________________________________________________________________________________
*/

	public static class FormatorFactory implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			if(Lua.equals("java")||Lua.equals("css")||Lua.equals("html"))
				return getJavaFormator();
			return null;
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
			EditListener li = ToLisrener(Lua);
			((CodeEdit)Edit).setFormator(li);
		}

		public static EditListener getJavaFormator()
		{
			return new DefaultFormatorListener();
		}


		public static class DefaultFormatorListener extends EditFormatorListener
		{

			public String START="{";
			public String END="}";
			public String SPILT="\n";
			public String INSERT=" ";
			public int CaCa=4;

			public  int dothing_Run(EditText self, int nowIndex)
			{
				Editable editor = self.getText();
				String src=editor.toString();
				int nextIndex= src.indexOf(SPILT, nowIndex + 1);
				//从上次的\n接着往后找一个\n

				//如果到了另一个代码块，不直接缩进
				int start_bindow = src.indexOf(START, nowIndex + 1);
				int end_bindow=src.indexOf(END, nowIndex + 1);

				if (nowIndex == -1 || nextIndex == -1)
					return -1;

				int nowCount,nextCount;
				nowCount = String_Splitor. calaN(src, nowIndex + 1);
				nextCount = String_Splitor. calaN(src, nextIndex + 1);
				//统计\n之后的分隔符数量

				String is= src.substring(CodeEdit.tryLine_Start(src, nextIndex + 1), CodeEdit. tryLine_End(src, nextIndex + 1));
				//如果下个的分隔符数量小于当前的，缩进至与当前的相同的位置
				if (nowCount >= nextCount && is.indexOf(START) == -1)
				{
					if (end_bindow < nextIndex && end_bindow != -1)
					{
						//如果当前的nextindex出了代码块，将}设为前面的代码块中与{相同位置
						int index= String_Splitor.getBeforeBindow(src, end_bindow , START, END);
						if (index == -1)
							return nextIndex;
						int linestart=CodeEdit.tryLine_Start(src, index);
						int noline= CodeEdit.tryAfterIndex(src, linestart);
						int bindowstart=CodeEdit.tryLine_Start(src, end_bindow);
						int nobindow=CodeEdit.tryAfterIndex(src, bindowstart);
						if (nobindow - bindowstart != noline - linestart)
						{		
							editor.replace(bindowstart, nobindow, String_Splitor.getNStr(INSERT, noline - linestart));
							return nextIndex + (noline - linestart) - (nobindow - bindowstart);
						}
						editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, noline - linestart - CaCa));
						return nextIndex;
					}
					if (start_bindow < nextIndex && start_bindow != -1)
					{
						//如果它是{之内的，并且缩进位置小于{，则将其缩进至{内
						editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount + CaCa));
						return nextIndex;
					}

					editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount));
					return nextIndex;
				}

				return nextIndex;
				//下次从这个\n开始

			}

			@Override
			public int dothing_Start(EditText editor, int nowIndex, int start, int end)
			{
				((CodeEdit)editor). reSAll(start,end,"\t", "    ");
				String src= editor.toString();
				nowIndex = src.lastIndexOf(SPILT, nowIndex - 1);
				if (nowIndex == -1)
					nowIndex = src.indexOf(SPILT);
				return nowIndex;
				//返回now之前的\n
			}

			@Override
			public int dothing_End(EditText editor, int beforeIndex, int start, int end)
			{
				return -1;
			}

		}

	}


/*	___________________________________________________________________________________________________________________________

	//InsertorFactory
	
	  ->  DefaultInsertor
	
	  ->  XMLInsertor
	  
	___________________________________________________________________________________________________________________________
	
*/

	public static class InsertorFactory implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			if(Lua.equals("xml")||Lua.equals("html"))
				return getXMLInsertor();
			return getDefultInsertor();
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
			EditListener li = ToLisrener(Lua);
			((CodeEdit)Edit).getInsertorList().clear();
			((CodeEdit)Edit).getInsertorList().add(li);
		}

		public static EditListener getDefultInsertor()
		{
			return new DefaultInsertorListener();
		}
		public static EditListener getXMLInsertor()
		{
			return new XMLInsertorListener();
		}

		public static class DefaultInsertorListener extends EditInsertorListener
		{

			@Override
			public void putWords(HashMap<CharSequence, CharSequence> words)
			{
				// TODO: Implement this method
			}

			public int dothing_insert(EditText self, int nowIndex)
			{
				Editable editor = self.getText();
				String src=editor.toString();
				char c = src.charAt(nowIndex);
				char nc = src.charAt(nowIndex+1);
				
				switch (c)
				{
					case '{':
						if(nc!='}')
						    editor.insert(nowIndex + 1, "}");
						return nowIndex+1;
					case '(':
						if(nc!=')')
						    editor.insert(nowIndex + 1, ")");
						return nowIndex+1;
					case '[':
						if(nc!=']')
						    editor.insert(nowIndex + 1, "]");
						return nowIndex+1;
					case '\'':
						if(nc!='\'')
						    editor.insert(nowIndex + 1, "'");
						return nowIndex+1;
					case '"':
						if(nc!='"')
						    editor.insert(nowIndex + 1, "\"");
						return nowIndex+1;
					case '\n':
						int index = CodeEdit.tryLine_Start(src, nowIndex);
						int count = String_Splitor.calaN(src, index);
						editor.insert(nowIndex + 1, String_Splitor.getNStr(" ", count));
						break;
				}

				return -1;
			}
		}
		
		public static class XMLInsertorListener extends DefaultInsertorListener
		{

			@Override
			public int dothing_insert(EditText self, int nowIndex)
			{
				Editable editor = self.getText();
				String src=editor.toString();
				char c = src.charAt(nowIndex);
				
				if (src.charAt(nowIndex - 1) == '<' && c=='/')
				{
					int index= String_Splitor.getBeforeBindow(src, nowIndex - 1, "<", "</");
					wordIndex j=CodeEdit. tryWordAfter(src, index);
					editor.insert(nowIndex + 1, src.substring(j.start, j.end) + ">");					
					return j.end + 1;
				}
				return super.dothing_insert(self, nowIndex);
			}
			
		}
	}


/*	
    ___________________________________________________________________________________________________________________________

	//EditCompletorBoxes
	
	  ->  keyBox
	
	  ->  DefultBox
	
	  ->  VillBox
	
	  ->  FuncBox
	
	  ->  ObjectBox
	
	  ->  TypeBox
	
	  ->  TagBox
	
	___________________________________________________________________________________________________________________________

*/
	public static class EditCompletorBoxes implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			return null;
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
			CodeEdit E = (CodeEdit) Edit;
			int Search_Bit = 0xefffffff;
			switch (Lua)
			{
				case "text":
					Search_Bit = 0;
					break;
				case "xml":
					Search_Bit = 0;
					Search_Bit = Share.setbitTo_1S(Search_Bit, Colors.color_tag, Colors.color_attr);
					break;
				case "java":
					Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_tag, Colors.color_attr);
					break;
				case "css":
					Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_key, Colors.color_const, Colors.color_obj);
					break;
				case "html":
					Search_Bit=0xffffffff;
					break;
				default:
					Search_Bit = 0;
			}
			E.setSearchBit(Search_Bit);
		}


	    public static EditListener getKeyBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_key))
					    return Wordlib. getKeyword();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_key,Share.getWordIcon(Share.icon_key));
				}
			};
		}
		public static EditListener getDefaultBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					List<CharSequence> words=new ArrayList<>();
					if (Share.getbit(Search_Bit, Colors.color_attr))
						words.addAll(Wordlib. getAttribute());
					if (Share.getbit(Search_Bit, Colors.color_const))
					    words.addAll(Wordlib. getConstword());

					return words;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_default,Share.getWordIcon(Share.icon_default));
				}
			};
		}

		public static EditListener getVillBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_villber))
					    return Wordlib. getHistoryVillber();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_villber,Share.getWordIcon(Share.icon_villber));
				}
			};
		}


		public static EditListener getFuncBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_func))
					    return Wordlib. getLastfunc();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit. addSomeWord(word, adpter, Share.icon_func,Share.getWordIcon(Share.icon_func));
				}
			};
		}


		public static EditListener getObjectBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_obj))
						return Wordlib. getThoseObject();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_obj,Share.getWordIcon(Share.icon_obj));
				}
			};
		}

		public static EditListener getTypeBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_type))
					    return Wordlib. getBeforetype();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_type,Share.getWordIcon(Share.icon_type));
				}
			};
		}

		public static EditListener getTagBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<CharSequence> onBeforeSearchWord(Words Wordlib, EditText self)
				{
					int Search_Bit = ((CodeEdit)self).getSearchBit();
					if (Share.getbit(Search_Bit, Colors.color_tag))
					{
						return Wordlib. getTag();
					}
					return null;
				}

				@Override
				public void onFinishSearchWord(List<CharSequence> word, List<Icon> adpter, EditText self)
				{
					CodeEdit.addSomeWord(word, adpter, Share.icon_tag,Share.getWordIcon(Share.icon_tag));
				}
			};
		}
	}

	
/*	
    ___________________________________________________________________________________________________________________________

	//FinderFactory
	
	  -> FinderText

	  -> FinderJava
 
	  -> FinderXML

	  -> FinderCSS
	 
	  -> FinderHTML
	 
	___________________________________________________________________________________________________________________________
	
*/
	
	public static class FinderFactory implements Factory
	{

		@Override
		public EditListener ToLisrener(String Lua)
		{
			switch (Lua)
			{
				case "text":
				    return getTextFinder();
				case "xml":
					return getXMLFinder();
				case "java":
				    return getJavaFinder();
				case "css":
					return getCSSFinder();
				case "html":
					return getHTMLFinder();
				default:
				    return null;
			}
		}

		@Override
		public void SwitchListener(EditText Edit, String Lua)
		{
			CodeEdit E = (CodeEdit) Edit;
			EditListener li = ToLisrener(Lua);	
			E.getFinderList().clear();
			E.getFinderList().add(li);
		}


		public static EditListener getTextFinder()
		{
			return new FinderText();
		}
		public static EditListener getXMLFinder()
		{
			return new FinderXML();
		}
		public static EditListener getJavaFinder()
		{
			return new FinderJava();
		}
		public static EditListener getCSSFinder()
		{
			return new FinderCSS();
		}
	    public static EditListener getHTMLFinder()
		{
		    return new FinderHTML();
	    }

		public static class FinderText extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{

			}

			@Override
			public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				// TODO: Implement this method
				AnyThingFactory. AnyThingForText AllThings = AnyThingFactory.getAnyThingText(WordLib);
				totalList.add(AllThings.getGoTo_zhuShi());
				totalList.add(AllThings.getGoTo_Str());
				totalList.add(AllThings.getNoSans_Char());
			}

			@Override
			public void OnClearFindWord(Words WordLib, EditText self)
			{

			}

			@Override
			public void OnClearFindNodes(int start, int end, String text, EditText self, List<wordIndex> nodes)
			{
				CodeEdit.clearRepeatNode(nodes);
			}
		}

		public static class FinderXML extends EditFinderListener
		{

			@Override
			public void OnClearFindNodes(int start, int end, String text, EditText self, List<wordIndex> nodes)
			{
				CodeEdit.clearRepeatNode(nodes);
			}


			@Override
			public void OnFindWord(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{

			}

			@Override
			public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				AnyThingFactory. AnyThingForXML AllThings = AnyThingFactory.getAnyThingXML(WordLib);

				totalList.clear();
				totalList.add(AllThings.getGoTo_zhuShi());	
				totalList.add(AllThings.getGoTo_Str());
				totalList.add(AllThings.getDraw_Tag());

				totalList.add(AllThings.getDraw_Attribute());	

				totalList.add(AllThings.getNoSans_Char());
			}

			@Override
			public void OnClearFindWord(Words WordLib, EditText self)
			{

			}
		}


		public static class FinderJava extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				AnyThingFactory. AnyThingForJava AllThings = AnyThingFactory.getAnyThingJava(WordLib);

				totalList.add(AllThings.getSans_TryFunc());	
				totalList.add(AllThings.getSans_TryVillber());
				totalList.add(AllThings.getSans_TryType());
				totalList.add(AllThings.getSans_TryObject());
				totalList.add(AllThings.getNoSans_Char());
				//请您在任何时候都加入getChar，因为它可以适时切割单词

			}

			@Override
			public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				AnyThingFactory. AnyThingForJava AllThings = AnyThingFactory.getAnyThingJava(WordLib);

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
			public void OnClearFindWord(Words WordLib, EditText self)
			{
				Array_Splitor. delSame(WordLib.getLastfunc(), WordLib.getKeyword());
				//函数名不可是关键字，但可以和变量或类型重名	
				Array_Splitor.delSame(WordLib.getLastfunc(), WordLib.getKeyword());
				//类型不可是关键字
				Array_Splitor.delSame(WordLib.getBeforetype(), WordLib.getHistoryVillber());
				//类型不可是变量，类型可以和函数重名
				Array_Splitor.delSame(WordLib.getBeforetype(), WordLib.getConstword());
				//类型不可是保留字
				Array_Splitor. delSame(WordLib.getHistoryVillber(), WordLib.getKeyword());
				Array_Splitor. delSame(WordLib.getThoseObject(), WordLib.getKeyword());
				//变量不可是关键字
				Array_Splitor. delSame(WordLib.getThoseObject(), WordLib.getConstword());
				Array_Splitor.delSame(WordLib.getHistoryVillber(), WordLib.getConstword());
				//变量不可是保留字
				Array_Splitor.delNumber(WordLib.getBeforetype());
				Array_Splitor.delNumber(WordLib.getHistoryVillber());
				Array_Splitor.delNumber(WordLib.getLastfunc());
				Array_Splitor.delNumber(WordLib.getThoseObject());
				//去掉数字
			}

			@Override
			public void OnClearFindNodes(int start, int end, String text, EditText self, List<wordIndex> nodes)
			{
				CodeEdit.clearRepeatNode(nodes);	
			}
		}



		public static class FinderCSS extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{

			}

			@Override
			public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				AnyThingFactory. AnyThingForCSS CSSThings = AnyThingFactory.getAnyThingCSS(WordLib);

				totalList.add(CSSThings.getGoTo_zhuShi());	
				totalList.add(CSSThings.getGoTo_Str());
				totalList.add(CSSThings.getNoSans_Func());
				totalList.add(CSSThings.getCSSDrawer());
				totalList.add(CSSThings.getCSSChecker());


				totalList.add(CSSThings.getDraw_Attribute());	

				totalList.add(CSSThings.getNoSans_Tag());

				totalList.add(CSSThings.getNoSans_Char());
				//请您在任何时候都加入getChar，因为它可以适时切割单词

			}

			@Override
			public void OnClearFindWord(Words WordLib, EditText self)
			{

			}

			@Override
			public void OnClearFindNodes(int start, int end, String text, EditText self, List<wordIndex> nodes)
			{
				CodeEdit.clearRepeatNode(nodes);
				clearRepeatNodeForCSS(text, nodes);
			}

			final public void clearRepeatNodeForCSS(String src, List<wordIndex> nodes)
			{
				//清除优先级低且位置重复的node
				int i;
				for (i = 0;i < nodes.size();i++)
				{
					wordIndex now = nodes.get(i);
					if (src.substring(now.start, now.end).equals("-"))
					{
						nodes.remove(i);
						i--;
					}
				}
			}
		}	


		public static class FinderHTML extends EditFinderListener
		{

			@Override
			public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{
				// TODO: Implement this method
			}


			@Override
			public void OnFindWord(List<CodeEdit.DoAnyThing> totalList, Words WordLib, EditText self)
			{

			}

			@Override
			public void OnClearFindWord(Words WordLib, EditText self)
			{

			}

			@Override
			public void OnClearFindNodes(int start, int end, String text, EditText self, List<wordIndex> nodes)
			{
				reDrawHTML(start, end, text.substring(start,end), nodes, self);
			}


			final protected List<wordIndex> getNodes(String text, String Lua, int now, EditText self)
			{
				CodeEdit E = (CodeEdit) self;
				EditFinderListener L = (EditFinderListener) getFinderFactory().ToLisrener(Lua);
				List<wordIndex> tmp = L.LetMeFind(0,text.length(),text,E.getWordLib(),E);
				CodeEdit. offsetNode(tmp, now);
				return tmp;
			}

			final protected List<wordIndex> reDrawHTML(int start, int end, String text, List<wordIndex>nodes, EditText self)
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
							tmp = getNodes(text.substring(now, css), "xml", now, self);
							nodes.addAll(tmp);
							now = css;
							//如果是css起始tag，将之前的html染色
						}
						else if (js == min)
						{
							js += 8;
							tmp =  getNodes(text.substring(now, js), "xml", now, self);
							nodes.addAll(tmp);
							now = js;
							//如果是js起始tag，将之前的html染色
						}
						else if (css_end == min)
						{
							css_end += 8;
							tmp =	getNodes(text.substring(now, css_end), "css", now, self);
							nodes.addAll(tmp);
							now = css_end;
							//如果是css结束tag，将之间的CSS染色
						}
						else if (js_end == min)
						{
							js_end += 9;
							tmp =	getNodes(text.substring(now, js_end), "java", now, self);
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
				String s=self.getText().toString();
				css = s.indexOf("<style", now + start);
				js = s.indexOf("<script", now + start);
				css_end = s.indexOf("</style", now + start);
				js_end = s.indexOf("</script", now + start);

				int min = Array_Splitor.getmin(0, s.length(), css, js, css_end, js_end);
				try
				{
					if (min == -1)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now, self);
						nodes.addAll(tmp);
						//范围内没有tag了
					}	
					else if (css == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now, self);
						nodes.addAll(tmp);
						//如果是css起始tag，将之前的xml染色
					}
					else if (js == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now, self);
						nodes.addAll(tmp);
						//如果是js起始tag，将之前的xml染色
					}
					else if (css_end == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "css", now, self);
						nodes.addAll(tmp);
						//如果是css结束tag，将之前的css染色
					}
					else if (js_end == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "java", now, self);
						nodes.addAll(tmp);
						//如果是js结束tag，将之前的js染色
					}
				}
				catch (Exception e)
				{}
				return nodes;
			}

		}

	}


	public static FinderFactory getFinderFactory()
	{
		return new FinderFactory();
	}
	public static DrawerFactory getDrawerDactory()
	{
		return new DrawerFactory();
	}
	public static FormatorFactory getFormatorFactory()
	{
		return new FormatorFactory();
	}
	public static InsertorFactory getInsertorFactory()
	{
		return new InsertorFactory();
	}
	public static EditCompletorBoxes getCompletorBox()
	{
		return new EditCompletorBoxes();
	}
	public static CanvaserFactory getCanvaserFactory()
	{
		return new CanvaserFactory();
	}

	
/*
     _________________________________________

	 AnyThingFactory

	   AnyThingForText ->

	   -> AnyThingForXML
 
	   -> AnyThingForJava

	   -> AnyThingForCSS

	 _________________________________________
	 
*/

    /*  Text工厂  */
	public static class AnyThingFactory
	{
		
		public static class AnyThingForText
		{

			Words WordLib;

			AnyThingForText(Words lib)
			{
				WordLib = lib;
			}

			//勇往直前的GoTo块，会突进一大段并阻拦其它块
			public CodeEdit. DoAnyThing getGoTo_zhuShi()
			{
				//获取注释
				return new CodeEdit. DoAnyThing(){		
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						CharSequence key = String_Splitor.indexOfKey(src, nowIndex, get_zhu());
						if (key != null)
						{
							//如果它是一个任意的注释，找到对应的另一个，并把它们之间染色
							CharSequence value= get_zhu().get(key);
							int nextindex = src.indexOf(value.toString(), nowIndex + key.length());

							if (nextindex != -1)
							{
								saveChar(src, nowIndex, nextindex + value.length(), Colors.color_zhu, nodes);
								nowIndex = nextindex + value.length() - 1;
							}
							else
							{
								//如果找不到默认认为到达了末尾
								saveChar(src, nowIndex, src.length(), Colors.color_zhu, nodes);
								nowIndex = src.length() - 1;
							}
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}

						return -1;
					}
				};
			}	
			public CodeEdit. DoAnyThing getGoTo_Str()
			{
				//获取字符串

				return new CodeEdit. DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						if (src.charAt(nowIndex) == '"')
						{
							//如果它是一个"，一直找到对应的"
							int endIndex = src.indexOf('"', nowIndex + 1);
							if (endIndex != -1)
							{
								saveChar(src, nowIndex, endIndex + 1, Colors.color_str, nodes);
								nowIndex = endIndex;
							}
							else
							{
								//如果找不到默认认为到达了末尾
								saveChar(src, nowIndex, src.length(), Colors.color_str, nodes);
								nowIndex = src.length() - 1;
							}
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}		
						else if (src.charAt(nowIndex) == '\'')
						{
							//如果它是'字符，将之后的字符加进来
							if (src.charAt(nowIndex + 1) == '\\')
							{
								wordIndex node= CodeEdit. Ep.get();
								node.set(nowIndex, nowIndex + 4, Colors.color_str);
								nodes.add(node);
								nowIndex += 3;	
							}

							else
							{		
								int endIndex = src.indexOf('\'', nowIndex + 1);
								saveChar(src, nowIndex, endIndex + 1, Colors.color_str, nodes);
								nowIndex = endIndex;
							}
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}	

						return -1;
					}
				};
			}
			public CodeEdit. DoAnyThing getNoSans_Char()
			{
				//获取字符
				return new CodeEdit. DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						if (String_Splitor. indexOfNumber(src.charAt(nowIndex))&&!String_Splitor.IsAtoz( src.charAt(nowIndex-1))&&!String_Splitor.IsAtoz( src.charAt(nowIndex+1)))
						{
							//否则如果当前的字符是一个数字，就把它加进nodes
							//由于关键字和保留字一定没有数字，所以可以清空之前的字符串
							wordIndex node= CodeEdit. Ep.get();
							node.set(nowIndex, nowIndex + 1, Colors.color_number);
							nodes.add(node);
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}	
						else if (Array_Splitor.indexOf(src.charAt(nowIndex), getFuhao()) != -1)
						{	
							//否则如果它是一个特殊字符，就更不可能了，清空之前累计的字符串
							if (Array_Splitor.indexOf(src.charAt(nowIndex), getSpilt()) == -1)
							{
								//如果它不是会被html文本压缩的字符，将它自己加进nodes
								//这是为了保留换行空格等
								wordIndex node=CodeEdit. Ep.get();
								node.set(nowIndex, nowIndex + 1, Colors.color_fuhao);
								nodes.add(node);
							}
							nowWord.delete(0, nowWord.length());
							//清空之前累计的字符串
							return nowIndex;
						}
						return -1;
					}
				};
			}

			public CodeEdit. DoAnyThing getCanTo()
			{

				return new CodeEdit. DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						//如果后面还有英文字符，它应该不是任意单词
						//为了节省时间，将更简单的条件放前面，触发断言机制
						if (String_Splitor.IsAtoz(src.charAt(nowIndex + 1)))
							return nowIndex;
						return -1;
					}
				};
			}

			public void saveChar(CharSequence src, int nowIndex, int nextindex, byte wantColor, List<wordIndex> nodes)
			{
				int startindex=nowIndex;
				for (;nowIndex < nextindex;nowIndex++)
				{
					//保留特殊字符
					if (Array_Splitor.indexOf(src.charAt(nowIndex), getSpilt()) != -1)
					{
						wordIndex node= CodeEdit. Ep.get();
						node.set(startindex, nowIndex, wantColor);
						nodes.add(node);
						startindex = nowIndex + 1;
					}
				}
				wordIndex node= CodeEdit. Ep.get();
				node.set(startindex, nextindex, wantColor);
				nodes.add(node);

			}

			public Collection<CharSequence> getKeyword()
			{
				return WordLib.getKeyword();
			}
			public Collection<CharSequence> getConstword()
			{
				return  WordLib.getConstword();
			}
			public char[] getFuhao()
			{
				return WordLib.fuhao;
			}
			public char[] getSpilt()
			{
				return WordLib.spilt;
			}
			public Map<CharSequence,CharSequence> get_zhu()
			{
				return WordLib.zhu_key_value;
			}
			public Collection<CharSequence> getLastfunc()
			{
				return  WordLib.getLastfunc();
			}
			public Collection<CharSequence> getHistoryVillber()
			{
				return WordLib.getHistoryVillber();
			}
			public Collection<CharSequence> getThoseObject()
			{
				return  WordLib.getThoseObject();
			}
			public Collection<CharSequence> getBeforetype()
			{
				return  WordLib.getBeforetype();
			}

			public Collection<CharSequence> getTag()
			{
				return  WordLib.getTag();
			}
			public Collection<CharSequence> getAttribute()
			{
				return WordLib.getAttribute() ;
			}

		}


       /*	XML工厂  */			
		public static class AnyThingForXML extends AnyThingForText
		{

			AnyThingForXML(Words lib)
			{
				super(lib);
			}

			public CodeEdit.DoAnyThing getDraw_Tag()
			{
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						//简单的一个xml方案
						wordIndex node;
						if (src.charAt(nowIndex) == '<')
						{
							node = CodeEdit.tryWordAfter(src, nowIndex + 1);
							node.b = Colors.color_tag;
							getTag().add(src.substring(node.start, node.end));
							nodes.add(node);
							nowIndex = node.end - 1;
							return nowIndex;
						}

						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getDraw_Attribute()
			{
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						wordIndex node;
						if (src.charAt(nowIndex) == '='
							|| src.charAt(nowIndex) == ':'
							)
						{
							node = CodeEdit.tryWord(src, nowIndex - 1);
							node.b = Colors.color_attr;
							getAttribute().add(src.substring(node.start, node.end));
							nodes.add(node);
							nowIndex = node.end - 1;
							return nowIndex;
						}
						return -1;
					}
				};
			}

		}


		/*  Java工厂  */
		public static class AnyThingForJava extends AnyThingForText
		{

			//不回溯的NoSans块，用已有信息完成判断
			public CodeEdit.DoAnyThing getNoSans_Keyword()
			{
				//获取关键字和保留字
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						//找到一个单词 或者 未找到单词就遇到特殊字符，就把之前累计字符串清空
						//为了节省时间，将更简单的条件放前面，触发&&的断言机制
						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && src.charAt(nowIndex + 1) != '_' && getKeyword().contains(nowWord.toString()))
						{
							//如果当前累计的字符串是一个关键字并且后面没有a～z这些字符，就把它加进nodes
							wordIndex node= CodeEdit.Ep.get();
							node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_key);
							nodes.add(node);
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}
						else if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && getConstword().contains(nowWord.toString()))
						{
							//否则如果当前累计的字符串是一个保留字并且后面没有a～z这些字符，就把它加进nodes
							wordIndex node= CodeEdit.Ep.get();
							node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_const);
							nodes.add(node);
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}		

						//关键字和保留字和变量不重复，所以只要中了其中一个，则就是那个
						//如果能进关键字和保留字和变量的if块，则说明当前字符一定不是特殊字符

						return -1;
					}	
				};
			}


			public CodeEdit.DoAnyThing getNoSans_Func()
			{
				//获取函数
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						int afterIndex=CodeEdit.tryAfterIndex(src, nowIndex + 1);
						if (src.charAt(afterIndex) == '(')
						{
							if (getLastfunc().contains(nowWord.toString()))
							{
								//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
								wordIndex node= CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_func);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return afterIndex - 1;
							}
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getNoSans_Villber()
			{
				//获得变量
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && getHistoryVillber().contains(nowWord.toString()))
						{
							int afterIndex=CodeEdit.tryAfterIndex(src, nowIndex + 1);
							if (src.charAt(afterIndex) != '(')
							{
								//否则如果当前累计的字符串是一个变量并且后面没有a～z和（ 这些字符，就把它加进nodes
								wordIndex node= CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_villber);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return afterIndex - 1;
							}
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getNoSans_Object()
			{
				//获取对象
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && getThoseObject().contains(nowWord.toString()))
						{
							int afterIndex=CodeEdit.tryAfterIndex(src, nowIndex + 1);
							if (src.charAt(afterIndex) != '(')
							{
								//否则如果当前累计的字符串是一个对象并且后面没有a～z和（ 这些字符，就把它加进nodes
								wordIndex node= CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_obj);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return afterIndex - 1;
							}
						}
						return -1;
					}
				};
			}

			public CodeEdit. DoAnyThing getNoSans_Type()
			{
				//获取类型
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && getBeforetype().contains(nowWord.toString()))
						{
							int afterIndex=CodeEdit.tryAfterIndex(src, nowIndex + 1);
							if (src.charAt(afterIndex) != '(')
							{
								//否则如果当前累计的字符串是一个类型并且后面没有a～z和（ 这些字符，就把它加进nodes
								wordIndex node= CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_type);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return afterIndex - 1;

							}
						}
						return -1;
					}
				};
			}


			//会回溯的Sans块，试探并记录单词
			public CodeEdit.DoAnyThing getSans_TryFunc()
			{
				//试探函数
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						wordIndex node;
						if (src.charAt(nowIndex) == '(')
						{
							//如果它是(字符，将之前的函数名存起来
							node = CodeEdit.tryWord(src, nowIndex - 1);
							getLastfunc().add(src.substring(node.start, node.end));
							return nowIndex;
						}

						return -1;	
					}	
				};
			}
			public CodeEdit.DoAnyThing getSans_TryVillber()
			{
				//试探变量和类型
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{					 
						wordIndex node; 
						if ((src.charAt(nowIndex) == '='))
						{
							//如果它是.或=字符，将之前的对象名或变量存起来	
							//=前后必须是分割符或普通的英文字符，不能是任何与=结合的算术字符
							node = CodeEdit.tryWord(src, nowIndex - 1);
							if (src.charAt(nowIndex) == '=' && !getHistoryVillber().contains(src.substring(node.start, node.end))
								&& Array_Splitor.indexOf(src.charAt(nowIndex - 1), arr) == -1
								&& Array_Splitor.indexOf(src.charAt(nowIndex + 1), arr) == -1)
							{
								//二次试探，得到类型
								//变量必须首次出现才有类型
								int nowN= CodeEdit.tryLine_Start(src, node.start);
								wordIndex tmp = CodeEdit.tryWord(src, node.start - 1);
								if (tmp.start > nowN)
								//类型与变量必须在同一行
									getBeforetype().add(src.substring(tmp.start, tmp.end));
							}

							getHistoryVillber().add(src.substring(node.start, node.end));
							return nowIndex;
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getSans_TryObject()
			{
				//试探对象
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						wordIndex node;
						if (src.charAt(nowIndex) == '.' && !String_Splitor.indexOfNumber(src.charAt(nowIndex + 2)))
						{
							node = CodeEdit.tryWord(src, nowIndex - 1);
							getThoseObject().add(src.substring(node.start, node.end));
							return nowIndex;
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getSans_TryType()
			{
				//试探类型
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{	
						if (!String_Splitor.IsAtoz(src.charAt(nowIndex + 1)) && getKeyword().contains(nowWord.toString()))
						{
							String Word=nowWord.toString();
							if (Word.equals("class")
								|| Word.equals("new")
								|| Word.equals("extends")
								|| Word.equals("implements")
								|| Word.equals("interface"))
							{
								wordIndex tmp=CodeEdit.tryWordAfter(src, nowIndex + 1);
								getBeforetype().add(src.substring(tmp.start, tmp.end));
								return tmp.end - 1;
							}

						}
						return -1;
					}
				};
			}

			private char arr[]; 
			AnyThingForJava(Words lib)
			{
				super(lib);
				arr = new char[]{'!','~','+','-','*','/','%','^','|','&','<','>','='};
				Arrays.sort(arr);
			}
		}


		/*  CSS工厂  */
		public static class AnyThingForCSS extends AnyThingForText
		{

			AnyThingForCSS(Words lib)
			{
				super(lib);
			}

			//如果所有东西不需进行二次查找，就用这个吧
			public CodeEdit.DoAnyThing getNoSans_Func()
			{
				//获取函数
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{

						if (src.charAt(nowIndex) == '(')
						{
							//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
							wordIndex node = CodeEdit.tryWord(src, nowIndex);
							node.b = Colors.color_func;
							nodes.add(node);
							wordIndex node2=CodeEdit.Ep.get();
							node2.set(nowIndex, nowIndex + 1, Colors.color_fuhao);
							nodes.add(node2);
							getLastfunc().add(src.substring(node.start, node.end));
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getNoSans_Object()
			{
				//获取对象
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{

						if (src.charAt(nowIndex) == '.')
						{
							//否则如果当前累计的字符串是一个对象并且后面是.字符，就把它加进nodes
							wordIndex node =CodeEdit. tryWord(src, nowIndex);
							node.b = Colors.color_obj;
							nodes.add(node);
							wordIndex node2=CodeEdit.Ep.get();
							node2.set(nowIndex, nowIndex + 1, Colors.color_fuhao);
							nodes.add(node2);
							getThoseObject().add(src.substring(node.start, node.end));
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}
						return -1;
					}
				};
			}

			public CodeEdit.DoAnyThing getCSSDrawer()
			{
				return new CodeEdit.DoAnyThing(){

					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						wordIndex node,node2;
						if (src.charAt(nowIndex) == '#')
						{
							node = CodeEdit.Ep.get();
							node.set(nowIndex, nowIndex + 1, Colors.color_fuhao);
							nodes.add(node);
							node2 = tryWordAfterCSS(src, nowIndex + 1);
							node2.b = Colors.color_cssid;
							nodes.add(node2);
							getHistoryVillber().add(src.substring(node2.start, node2.end));
							return node2.end - 1;

						}
						else if (src.charAt(nowIndex) == '.' && !String_Splitor.indexOfNumber(src.charAt(nowIndex - 1)) && !String_Splitor.indexOfNumber(src.charAt(nowIndex + 1)))
						{
							node = CodeEdit.Ep.get();
							node.set(nowIndex, nowIndex + 1, Colors.color_fuhao);
							nodes.add(node);
							node2 = tryWordAfterCSS(src, nowIndex + 1);
							getBeforetype().add(src.substring(node2.start, node2.end));
							node2.b = Colors.color_csscla;
							nodes.add(node2);
							return node2.end - 1;
						}
						return -1;
					}
				};
			}

			public CodeEdit.DoAnyThing getCSSChecker()
			{
				return new CodeEdit.DoAnyThing(){

					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						//为了节省时间，将更简单的条件放前面，触发&&的断言机制
						if (String_Splitor.IsAtoz(src.charAt(nowIndex + 1)) && src.charAt(CodeEdit.tryLine_End(src, nowIndex) - 1) == '{')
						{
							if (getHistoryVillber().contains(nowWord.toString()))
							{
								wordIndex node=CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_cssid);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}
							else if (getBeforetype().contains(nowWord.toString()))
							{
								wordIndex node=CodeEdit.Ep.get();
								node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_csscla);
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}
						}

						return -1;
					}
				};
			}


			public CodeEdit.DoAnyThing getNoSans_Tag()
			{
				//获取Tag
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{

						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1))
							&& src.charAt(CodeEdit.tryLine_End(src, nowIndex) - 1) == '{'
							&& (getTag().contains(nowWord.toString())))
						{
							//如果当前累计的字符串是一个Tag并且后面没有a～z和这些字符，就把它加进nodes
							wordIndex node=CodeEdit.Ep.get();
							node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_tag);
							nodes.add(node);
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}
						return -1;
					}
				};
			}
			public CodeEdit.DoAnyThing getNoSans_Attribute()
			{
				//获取属性
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						int afterIndex=CodeEdit.tryAfterIndex(src, nowIndex + 1);

						if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && src.charAt(afterIndex) != '(' && getAttribute().contains(nowWord))
						{
							//如果当前累计的字符串是一个属性并且后面没有a～z这些字符，就把它加进nodes
							wordIndex node=CodeEdit.Ep.get();
							node.set(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_attr);
							nodes.add(node);
							nowWord.delete(0, nowWord.length());
							return nowIndex;
						}
						return -1;
					}
				};
			}	
			public CodeEdit.DoAnyThing getDraw_Attribute()
			{
				return new CodeEdit.DoAnyThing(){
					@Override
					public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
					{
						wordIndex node;
						if (src.charAt(nowIndex) == '='
							|| src.charAt(nowIndex) == ':'
							)
						{
							int i=src.indexOf('{', nowIndex);
							if (i < CodeEdit.tryLine_End(src, nowIndex) && i != -1)
							{
								node = tryWordAfterCSS(src, nowIndex + 1);
								node.b = Colors.color_cssfl;
								getAttribute().add(src.substring(node.start, node.end));
								nodes.add(node);
								return node.end - 1;
							}
							else
							{
								node = tryWordForCSS(src, nowIndex - 1);
								node.b = Colors.color_attr;
								getAttribute().add(src.substring(node.start, node.end));
								nodes.add(node);
								return node.end - 1;
							}
						}
						return -1;
					}
				};
			}
			public wordIndex tryWordForCSS(CharSequence src, int index)
			{
				//试探前面的单词
				wordIndex tmp = CodeEdit.Ep.get();
				try
				{
					while (Array_Splitor. indexOf(src.charAt(index), getFuhao()) != -1)
						index--;
					tmp.end = index + 1;
					while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
						index--;
					tmp.start = index + 1;
				}
				catch (Exception e)
				{
					return new wordIndex();
				}
				return tmp;
			}

			public wordIndex tryWordAfterCSS(CharSequence src, int index)
			{
				//试探后面的单词
				wordIndex tmp =CodeEdit. Ep.get();
				try
				{
					while (Array_Splitor.indexOf(src.charAt(index), getFuhao()) != -1)
						index++;
					tmp.start = index;
					while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
						index++;
					tmp.end = index;
				}
				catch (Exception e)
				{
					return new wordIndex();
				}
				return tmp;
			}
		}

		public static AnyThingForText getAnyThingText(Words lib)
		{
			return new AnyThingForText(lib);
		}
		public static AnyThingForXML getAnyThingXML(Words lib)
		{
			return new AnyThingForXML(lib);
		}
		public static AnyThingForJava getAnyThingJava(Words lib)
		{
			return new AnyThingForJava(lib);
		}
		public static AnyThingForCSS getAnyThingCSS(Words lib)
		{
			return new AnyThingForCSS(lib);
		}

	}
}
