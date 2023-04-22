package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;


/*
  找单词，转化为Icon，插入单词
  
  Wordlib表示编辑器内部的单词库，在Finder查找nodes时自动存储一些了，您可以返回其中的一个库，也可以返回外部的任意的单词库
  
  在之后，我们用SearchOnce找到当前光标位置对应的单词，然后以words发送给您，您只要把对应的Icon添加到adapter中，可以用CodeEdit.addSomeWord
  
  最后，我们会将单词放到ListView的adapter中，并为这组单词添加一个id(这个id就是当前listener的hashCode)，然后展示ListView
  
  当用户点击一个列表项，会寻找这个单词所在范围，拿到这组单词的id，遍历所有listener，找到指定hashCode的listener，并回调它的LetMeInsertWord方法
  
*/
public abstract class EditCompletorListener extends EditListener
{
	
	protected abstract Collection<CharSequence> onBeforeSearchWord(Words Wordlib);
	//返回任意的集合，我们帮您查找
	protected abstract void onFinishSearchWord(List<CharSequence> words,List<Icon> adapter);
	//我们把找到的单词传给您，您只要把对应的Icon添加到adapter中
	

	final public List<Icon> LetMeSearch(String text,int index,CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib)
	{
		List<Icon> Adapter = null;
		try{
			if(Enabled())
		        Adapter=Search(text,index,wantBefore,wantAfter,before,after,Wordlib);
		}catch(Exception e){
			Log.e("Completing Error", toString()+" "+e.toString());
		}
		return Adapter;
	}
	
	/*
	  text表示编辑器文本，index表示光标位置，wantBefore和wantAfter分别表示光标前后的字符串，before和after表示搜索前单词和后单词的起始下标，Wordlib为单词库
	  
	  您可以重写方法
	*/
	protected List<Icon> Search(String text,int index,CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib)
	{
		Collection<CharSequence> lib;
		List<CharSequence> words = null;
		List<Icon> Adapter = new ArrayList<>();
		//为每一个listener分配一个Adapter
		
		lib = onBeforeSearchWord(Wordlib);
		if (lib != null && lib.size() != 0)
		{
			words = SearchOnce(wantBefore, wantAfter, lib, before, after);
		}
	    onFinishSearchWord(words,Adapter);
		return Adapter;
	}
	
	final public int LetMeInsertWord(Editable editor,int index,size range,CharSequence word)
	{
		int selection = index+word.length();
		try{
			if(Enabled())
		        selection= onInsertWord(editor,index,range,word);
		}catch(Exception e){
			Log.e("Completor InsertWord Error", toString()+" "+e.toString());
		}
		return selection;
	}
	
	/* 
	  您可以重写方法，等待回调，然后根据情况插入单词 
	
	  editor表示编辑器内部的文本容器，index表示编辑器的光标位置，range表示旧单词的范围，word表示插入单词
	*/
	protected int onInsertWord(Editable editor,int index,size range,CharSequence word){
		editor.replace(range.start, range.end, word);
		return range.start + word.length();
	}
	
	
	/* 在一个库中搜索单词，支持Span文本 */
	final public static List<CharSequence> SearchOnce(CharSequence wantBefore, CharSequence wantAfter, CharSequence[] target, int before, int after)
	{
		List<CharSequence> words=null;
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

	/* 在一个库中搜索单词，支持Span文本 */
	final public static List<CharSequence> SearchOnce(CharSequence wantBefore, CharSequence wantAfter, Collection<CharSequence> target, int before, int after)
	{
		List<CharSequence> words=null;
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
	
}
