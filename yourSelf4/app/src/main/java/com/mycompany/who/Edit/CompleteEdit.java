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

public class CompleteEdit extends FormatEdit
{

	public static boolean Enabled_Complete=false;
	private int Search_Bit=0xefffffff;
	private ArrayList<EditListener> mlistenerCS;
	
	public CompleteEdit(Context cont)
	{
		super(cont);	
		mlistenerCS = new ArrayList<>();
		mlistenerCS.add(new DefaultCompletListener());
	}
	public CompleteEdit(Context cont,CompleteEdit Edit){
		super(cont,Edit);
		mlistenerCS=Edit.mlistenerCS;
		Search_Bit=Edit.Search_Bit;
	}
    public CompleteEdit(Context cont,AttributeSet set){
		super(cont,set);
	}
	@Override
	public void reSet()
	{
		super.reSet();
		mlistenerCS.add(new DefaultCompletListener());
	}
	
	@Override
	public void setLuagua(String name)
	{
		super.setLuagua(name);
		switch(name){
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
				Search_Bit=0;
				break;
		}
	}
	
	
	public ArrayList<EditListener> getCompletorList()
	{
		return mlistenerCS;
	}
	public void clearListener()
	{
		getCompletorList().clear();
		super.clearListener();
	}

	//多线程

	public void openWindow(ListView Window, int index, ThreadPoolExecutor pool)
	{
		if (!Enabled_Complete)
			return;
		String wantBefore= getWord(index);
		String wantAfter = getAfterWord(index);
		//获得光标前后的单词，并开始查找
		ArrayList<Icon> Icons = new ArrayList<>();

		try
		{
			SearchWords(Icons, wantBefore, wantAfter, 0, wantBefore.length(), pool);
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}

		WordAdpter adapter = new WordAdpter(Window.getContext(), Icons, R.layout.WordIcon);
		Window.setAdapter(adapter);

	}

	protected void SearchWords(ArrayList<Icon> adapter, String wantBefore, String wantAfter, int before, int after, ThreadPoolExecutor pool) throws InterruptedException, ExecutionException
	{
		ArrayList<ArrayList<String>> words;
		ArrayList<ArrayList<String>> words2;
		ArrayList<Collection<String>> libs = new ArrayList<>();
		ArrayList<String[]> libs2 = new ArrayList<>();

		for(EditListener li:getCompletorList()){
			if(li!=null)
			    ((EditCompletorListener)li).onBeforeSearchWord(libs,libs2);
		}

		if(getPool()==null){
			words = NoPoolA(wantBefore, wantAfter, before, after, libs);
			words2 = NoPoolA(wantBefore, wantAfter, before, after, libs2);
		}
		else{
		    words = poolA(wantBefore, wantAfter, before, after, getPool(), libs);
		    words2 = poolA(wantBefore, wantAfter, before, after, getPool(), libs2);
		}
		
		for(EditListener li:mlistenerCS){
			if(li!=null)
			    ((EditCompletorListener)li).onFinishSearchWord(words,words2,adapter);
		}
	}
	
	protected ArrayList< ArrayList<String>> NoPoolA(final String wantBefore, final String wantAfter, final int before, final int after, ArrayList<Collection<String>> libs)
	{
		ArrayList< ArrayList<String>> words=new ArrayList<>();
		for (Collection<String> lib:libs)
		{
			ArrayList<String> word = SearchOnce(wantBefore, wantAfter, lib, before, after);
			if (word != null)
			{
				Array_Splitor.sort(word);
				Array_Splitor.sort2(word);
			}
			words.add(word);
		}
		return words;
	}
	protected ArrayList< ArrayList<String>> NoPoolA(final String wantBefore, final String wantAfter, final int before, final int after, Collection<String[]> libs)
	{
		//这里最后一个参数为什么不用ArrayList，因为如果与上个类型重复了，构不成重载
		ArrayList< ArrayList<String>> words=new ArrayList<>();
		for (String[] lib:libs)
		{
			ArrayList<String> word = SearchOnce(wantBefore, wantAfter, lib, before, after);
			if (word != null)
			{
				Array_Splitor.sort(word);
				Array_Splitor.sort2(word);
			}
			words.add(word);
		}
		return words;
	}
	protected ArrayList< ArrayList<String>> poolA(final String wantBefore, final String wantAfter, final int before, final int after, ThreadPoolExecutor pool, ArrayList< Collection<String>> libs)
	{
		//利用多线程同时在不同集合中找单词
		ArrayList<Future<ArrayList<String>>> results=new ArrayList<>();
		for (final Collection<String> lib:libs)
		{
			Callable<ArrayList<String>> ca=new Callable<ArrayList<String>>(){

				@Override
				public ArrayList<String> call() throws Exception
				{
					ArrayList<String> words;
					words = SearchOnce(wantBefore, wantAfter, lib, before, after);
					if (words != null)
					{
						Array_Splitor.sort(words);
						Array_Splitor.sort2(words);
					}
					return words;
				}
			};
			results.add(pool.submit(ca));
			//每次把一个任务加进池子，然后得到Future
		}

		return FuturePool. FutureGet(results);
	}

	protected  ArrayList< ArrayList<String>> poolA(final String wantBefore, final String wantAfter, final int before, final int after, ThreadPoolExecutor pool, Collection<String[]> libs)
	{
		//这里最后一个参数为什么不用ArrayList，因为如果与上个类型重复了，构不成重载
		//利用多线程同时在不同集合中找单词
		ArrayList<Future<ArrayList<String>>> results=new ArrayList<>();
		for (final String[] lib:libs)
		{
			Callable<ArrayList<String>> ca=new Callable<ArrayList<String>>(){

				@Override
				public ArrayList<String> call() throws Exception
				{
					ArrayList<String> words;
					words = SearchOnce(wantBefore, wantAfter, lib, before, after);
					if (words != null)
					{
						Array_Splitor.sort(words);
						Array_Splitor.sort2(words);
					}
					return words;
				}
			};
			results.add(pool.submit(ca));
			//每次把一个任务加进池子，然后得到Future
		}

		return FuturePool. FutureGet(results);
	}
	
	
	protected ArrayList<String> SearchOnce(String wantBefore, String wantAfter, String[] target, int before, int after)
	{
		ArrayList<String> words=null;
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

	protected ArrayList<String> SearchOnce(String wantBefore, String wantAfter, Collection<String> target, int before, int after)
	{
		//同上
		ArrayList<String> words=null;
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

	protected void addSomeWord(ArrayList<String> words, ArrayList<Icon> adapter, byte flag)
	{
		//排序并添加一组的单词块
		if (words == null || words.size() == 0)
			return;
		int icon = Share.getWordIcon(flag);
		for (String word: words)
		{
			Icon token = new Icon(icon, word);
			token.setflag(flag);
		    adapter.add(token);
		}

	}


	public String getWord(int offset)
	{
		//获得光标前的纯单词
	    wordIndex node = tryWordSplit(getText().toString(), offset);
		if (node.end == 0)
			node.end = offset;
		String want= getText().toString().substring(node.start, node.end);

		return want;
	}
	public String getAfterWord(int offset)
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

	}

	
    class DefaultCompletListener extends EditCompletorListener
	{

		@Override
		public void onBeforeSearchWord(ArrayList<Collection<String>> libs, ArrayList<String[]> libs2)
		{
			if (Share.getbit(Search_Bit, Colors.color_villber))
				libs.add(getHistoryVillber());
			if (Share.getbit(Search_Bit, Colors.color_func))
				libs.add(getLastfunc());
			if (Share.getbit(Search_Bit, Colors.color_type))
				libs.add(getBeforetype());
			if (Share.getbit(Search_Bit, Colors.color_tag))
				libs.add(getTag());
			if (Share.getbit(Search_Bit, Colors.color_attr))
				libs.add(getAttribute());
			if (Share.getbit(Search_Bit, Colors.color_obj))
				libs.add(getThoseObject());
			
			if (Share.getbit(Search_Bit, Colors.color_key))
				libs2.add(getKeyword());
			if (Share.getbit(Search_Bit, Colors.color_const))
				libs2.add(getConstword());
			if (Share.getbit(Search_Bit, Colors.color_tag))
				libs2.add(getIknowtag());
			
		}

		@Override
		public void onFinishSearchWord(ArrayList<ArrayList<String>> words1, ArrayList<ArrayList<String>> words2,ArrayList<Icon> adapter)
		{
			int i=0,j=0;
			if (Share.getbit(Search_Bit, Colors.color_villber)){
			    addSomeWord(words1.get(i), adapter, Share.icon_villber);
				i++;
			}
			if (Share.getbit(Search_Bit, Colors.color_func)){
			    addSomeWord(words1.get(i), adapter, Share.icon_func);
				i++;
			}
			if (Share.getbit(Search_Bit, Colors.color_type)){
			    addSomeWord(words1.get(i), adapter, Share.icon_type);
				i++;
			}
			if (Share.getbit(Search_Bit, Colors.color_tag)){
			    addSomeWord(words1.get(i), adapter, Share.icon_tag);
				i++;
			}
			if (Share.getbit(Search_Bit, Colors.color_attr)){
			    addSomeWord(words1.get(i), adapter, Share.icon_default);
				i++;
			}
			if (Share.getbit(Search_Bit, Colors.color_obj)){
			    addSomeWord(words1.get(i), adapter, Share.icon_obj);
				i++;
			}

			if (Share.getbit(Search_Bit, Colors.color_key)){
			    addSomeWord(words2.get(j), adapter, Share.icon_key);
				j++;
		    }
			if (Share.getbit(Search_Bit, Colors.color_const)){
			    addSomeWord(words2.get(j), adapter, Share.icon_default);
				j++;
			}
			if (Share.getbit(Search_Bit, Colors.color_tag)){
			    addSomeWord(words2.get(j), adapter, Share.icon_tag);
				j++;
			}
			
		}
	
	}

}





