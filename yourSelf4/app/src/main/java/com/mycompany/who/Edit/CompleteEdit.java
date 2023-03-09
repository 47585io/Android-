package com.mycompany.who.Edit;

import android.content.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.R;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import android.graphics.*;

public class CompleteEdit extends FormatEdit
{
	private final EditCompletorBoxes boxes=new EditCompletorBoxes();
	public static EPool3 Epp;
	public static boolean Enabled_Complete=false;
	private static int Search_Bit=0xefffffff;
	protected List<EditListener> mlistenerCS;
	
	static{
		Epp=new EPool3();
	}
	
	public CompleteEdit(Context cont)
	{
		super(cont);	
		mlistenerCS = new ArrayList<>();
		trimListener();
	}
	public CompleteEdit(Context cont, CompleteEdit Edit)
	{
		super(cont, Edit);
		mlistenerCS=Edit.getCompletorList();
		Search_Bit = Edit.Search_Bit;
	}
    
	public void trimListener()
	{
		mlistenerCS.clear();
		mlistenerCS.add(boxes.getVillBox());
		mlistenerCS.add(boxes.getObjectBox());
		mlistenerCS.add(boxes.getFuncBox());
		mlistenerCS.add(boxes.getTypeBox());
		mlistenerCS.add(boxes.getKeyBox());
		mlistenerCS.add(boxes.getTagBox());
		mlistenerCS.add(boxes.getDefaultBox());
	}


	@Override
	public void setLuagua(String name)
	{
		super.setLuagua(name);
		switch (name)
		{
			case "xml":
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_key, Colors.color_const, Colors.color_func, Colors.color_villber, Colors.color_obj, Colors.color_type);
				break;
			case "java":
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_tag, Colors.color_attr);
				break;
			case "css":
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_key, Colors.color_const, Colors.color_obj);
				break;
			case "html":
				Search_Bit = 0xefffffff;
				break;
			case "text":
				Search_Bit = 0;
				break;
		}
	}


	public List<EditListener> getCompletorList()
	{
		return mlistenerCS;
	}
	
	public void clearListener()
	{
		getCompletorList().clear();
		super.clearListener();
	}

	protected void callOnopenWindow(ListView Window){
	}
	
	//多线程
	final public void openWindow(final ListView Window, int index, final ThreadPoolExecutor pool)
	{
		if (!Enabled_Complete)
			return;
		final String wantBefore= getWord(index);
		final String wantAfter = getAfterWord(index);
		//获得光标前后的单词，并开始查找
		
		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				Epp.start();//开始存储
				List<Icon> Icons = SearchInGroup(wantBefore,wantAfter,0,wantBefore.length(),mlistenerCS);
				//经过一次查找，Icons里装满了单词
				if(Icons!=null){
					final WordAdpter adapter = new WordAdpter(Window.getContext(), Icons, R.layout.WordIcon);
					Runnable run2=new Runnable(){

						@Override
						public void run()
						{
							Window.setAdapter(adapter);
							callOnopenWindow(Window);
							//将单词设置到Window后回收单词
							Epp.stop();
						}
					};
					post(run2);//将UI任务交给主线程
				}
			}
		};
		if(pool!=null)
		    pool.submit(run);//因为含有阻塞，所以将任务交给池子
		else
			run.run();
		
	}
	final private List<Icon> SearchInGroup(final String wantBefore,final String wantAfter,final int before,final int after,Collection<EditListener> Group){
		//用多线程在不同集合中找单词
		if(Runner==null)
			return null;
		
		EditListenerItrator.RunLi<List<Icon>> run = new EditListenerItrator.RunLi<List<Icon>>(){

			@Override
			public List<Icon> run(EditListener li)
			{
				return Runner.CompeletForLi(wantBefore,wantAfter,before,after,(EditCompletorListener)li);
			}
		};
		if(pool==null)
			return EditListenerItrator.foreach(Group,run);
		return EditListenerItrator.foreach(Group,run,pool);
		//阻塞以获得所有单词
	}
	
	final public static List<String> SearchOnce(String wantBefore, String wantAfter, String[] target, int before, int after)
	{
		List<String> words=null;
		Array_Splitor. Idea ino = Array_Splitor.getNo();
		Array_Splitor. Idea iyes = Array_Splitor.getyes();
		if (!wantBefore.equals(""))
		//如果前字符串不为空，则搜索
		    words = Array_Splitor.indexsOf(wantBefore, target, before, ino);
		if (!wantAfter.equals("") && words != null)
		//如果前字符串搜索结果不为空并且后字符串不为空，就从之前的搜索结果中再次搜索
		    words = Array_Splitor.indexsOf(wantAfter, words, after, iyes);
		else if (!wantAfter.equals("") && wantBefore.equals(""))
		{
			//如果前字符串为空，但后字符串不为空，则只从后字符串开始搜索
			words = Array_Splitor.indexsOf(wantAfter, target, after, iyes);
		}
		return words;
	}

	final public static List<String> SearchOnce(String wantBefore, String wantAfter, Collection<String> target, int before, int after)
	{
		//同上
		List<String> words=null;
		Array_Splitor. Idea ino = Array_Splitor.getNo();
		Array_Splitor. Idea iyes = Array_Splitor.getyes();
		if (!wantBefore.equals(""))
		    words = Array_Splitor.indexsOf(wantBefore, target, before, ino);
		if (!wantAfter.equals("") && words != null)
		    words = Array_Splitor.indexsOf(wantAfter, words, after, iyes);
		else if (!wantAfter.equals("") && wantBefore.equals(""))
		{
			words = Array_Splitor.indexsOf(wantAfter, target, after, iyes);
		}
		return words;
	}

	final public static void addSomeWord(List<String> words, List<Icon> adapter, byte flag)
	{
		//排序并添加一组的单词块
		if (words == null || words.size() == 0)
			return;
		Array_Splitor.sort(words);
		Array_Splitor.sort2(words);
		int icon = Share.getWordIcon(flag);
		for (String word: words)
		{
			Icon token = Epp.get();
			token.setIcon(icon);
			token.setName(word);
			token.setflag(flag);
		    adapter.add(token);
		}

	}


	final public String getWord(int offset)
	{
		//获得光标前的纯单词
	    wordIndex node = tryWordSplit(getText().toString(), offset);
		if (node.end == 0)
			node.end = offset;
		String want= getText().toString().substring(node.start, node.end);

		return want;
	}
	final public String getAfterWord(int offset)
	{
		//获得光标后面的纯单词
		wordIndex node = tryWordSplitAfter(getText().toString(), offset);
		if (node.end == 0)
			node.end = getText().toString().length();
		String want= getText().toString().substring(node.start, node.end);

		return want;
	}
	public void insertWord(String word, int index, int flag)
	{
		IsModify++;
		try
		{
			Editable editor = getText();
			wordIndex tmp = tryWordSplit(editor.toString(), index);
			wordIndex tmp2 = tryWordSplitAfter(getText().toString(), index);

			getText().replace(tmp.start, tmp2.end, word);
			setSelection(tmp.start + word.length());
			//把光标移动到最后

			if (flag == 3)
			{
				getText().insert(getSelectionStart(), "(");
			}
			else if (flag == 5)
			{
				if (getText().toString().charAt(tmp.start - 1) != '<')
					getText().insert(tmp.start, "<");
			}
			//函数额外插入字符
		}
		catch (Exception e)
		{}
		IsModify--;
	}


    final public class EditCompletorBoxes
	{ 
	    
	    public EditListener getKeyBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					return toColletion(getKeyword());
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_key);
				}
			};
		}
		public EditListener getDefaultBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					List<String> words=new ArrayList<>();
					if (Share.getbit(Search_Bit, Colors.color_attr))
						words.addAll(getAttribute());
					if(Share.getbit(Search_Bit, Colors.color_const))
					    words.addAll(toColletion(getConstword()));
					
					return words;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_default);
				}
			};
		}

		public EditListener getVillBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_villber))
					    return getHistoryVillber();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_villber);
				}
			};
		}


		public EditListener getFuncBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_func))
					    return getLastfunc();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_func);
				}
			};
		}


		public EditListener getObjectBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_obj))
						return getThoseObject();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_obj);
				}
			};
		}

		public EditListener getTypeBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_type))
					    return getBeforetype();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_type);
				}
			};
		}

		public EditListener getTagBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_tag))
					{
						List<String> words=new ArrayList<>();
						words.addAll(toColletion(getIknowtag()));
						words.addAll(getTag());	
						return words;
					}
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_tag);
				}
			};
		}
	}

	public EditCompletorBoxes getCompletorBox(){
		return new EditCompletorBoxes();
	}
	
	public static class EPool3 extends EPool<Icon>
	{

		@Override
		public Icon creat()
		{
			return new Icon();
		}

		@Override
		public void resetE(Icon E)
		{
		}

	}

}





