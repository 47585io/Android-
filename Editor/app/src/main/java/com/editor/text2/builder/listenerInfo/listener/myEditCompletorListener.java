package com.editor.text2.builder.listenerInfo.listener;

import android.text.*;
import android.util.*;
import java.util.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import android.view.*;
import com.editor.text2.builder.words.*;
import com.editor.text2.base.share2.*;
import com.editor.text2.base.share4.*;
import static com.editor.text2.builder.CodeEditBuilder.WordsPackets.BaseWordsPacket.fuhao;
import com.editor.text2.base.share1.*;
import com.editor.text.base.*;


/*
  找单词，转化为Icon，插入单词
  
  Wordlib表示编辑器内部的单词库，在Finder查找nodes时自动存储一些了，您可以返回其中的一个库，也可以返回外部的任意的单词库
  
  在之后，我们用SearchOnce找到当前光标位置对应的单词，然后以words发送给您，您只要把它转换为Icon即可  
  
  最后，我们会将单词放到ListView的adapter中，并为这组单词添加一个id(这个id就是当前listener的hashCode)，然后展示ListView
  
  当用户点击一个列表项，会寻找这个单词所在单词组，拿到这组单词的id，遍历所有listener，找到指定hashCode的listener，并回调它的onInsertWord方法
  
*/
public abstract class myEditCompletorListener extends myEditListener implements EditCompletorListener
{
	
	public static final int Invalid_Id = 0;
	
	public myEditCompletorListener(){
		mIcons = new IconPool();
	}

	public abstract Collection<CharSequence> beforeSearchWord(Words WordLib)

	public abstract wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
	
	@Override
	public wordIcon[] onSearchWord(CharSequence text, int index, Words WordLib)
	{
		Collection<CharSequence> lib = beforeSearchWord(WordLib);
		if(lib==null || lib.size()==0){
			return EmptyArray.emptyArray(wordIcon.class);
		}
		final CharSequence wantBefore= getWord(text,index);
		final CharSequence wantAfter = getAfterWord(text,index);
		Collection<CharSequence> words = SearchOnce(wantBefore,wantAfter,lib);
		if(words==null || words.size()==0){
			return EmptyArray.emptyArray(wordIcon.class);
		}
		return finishSearchWord(words,WordLib);
	}

	@Override
	public int onInsertWord(Editable editor, int index, CharSequence word)
	{
		final int start = tryIndex(editor,index);
		final int end = tryIndexAfter(editor,index);
		editor.replace(start,end,word,0,word.length());
		return start+word.length();
	}

	@Override
	public wordIcon[] onSearchDoc(CharSequence word, Words lib){
		return EmptyArray.emptyArray(wordIcon.class);
	}
	
	/* 试探光标命中单词的开头 */
	final public static int tryIndex(CharSequence src,int nowIndex)
	{
		int index=nowIndex-1;
		while(index>-1&&!fuhao.contains(src.charAt(index)))
			--index;
		return index+1;
	}
	/* 试探光标命中单词的末尾 */
	final public static int tryIndexAfter(CharSequence src,int index)
	{
		while(index<src.length()&&!fuhao.contains(src.charAt(index)))
			++index;
		return index;
	}
	
	/* 获取光标前的单词 */
	final public static CharSequence getWord(CharSequence src,int offset)
	{
		int index = tryIndex(src, offset);
		if (index == 0)
			index = offset;
		char[] arr = new char[offset-index];
		TextUtils.getChars(src,index,offset,arr,0);
		return String.valueOf(arr);
	}
	/* 获取光标后的单词 */
	final public static CharSequence getAfterWord(CharSequence src,int offset)
	{
		int index = tryIndexAfter(src, offset);
		if (index == 0)
			index = src.length();
		char[] arr = new char[index-offset];
		TextUtils.getChars(src,offset,index,arr,0);
		return String.valueOf(arr);
	}
	
	/* 在一个库中搜索单词 */
	final public static Collection<CharSequence> SearchOnce(CharSequence wantBefore, CharSequence wantAfter, Collection<CharSequence> target)
	{
		final int before = wantBefore.length();
		final int after = wantAfter.length();
		Collection<CharSequence> words = null;
		if (before>0 && after>0){
		    words = indexsOfBefore(wantBefore,0,target);
			words = indexsOfAfter(wantAfter,before,words);
		}
		else if (before>0){
		    words = indexsOfBefore(wantBefore,0,target);
		}
		else if (after>0){
			words = indexsOfBefore(wantAfter,0,target);
		}
		return words;
	}
	/* 单词的前半部分必须与光标命中单词的前半部分一样 */
	final public static List<CharSequence> indexsOfBefore(CharSequence str,int start,Collection<CharSequence> keyword) 
	{	
	    List<CharSequence> result = new ArrayList<>();
	    for(CharSequence word:keyword){
			if(word.toString().toLowerCase().indexOf(str.toString().toLowerCase(),start)==start){
				result.add(word);
			}
		}
		return result;
	}
	/* 单词的后半部分必须与光标命中单词的后半部分一样 */
	final public static List<CharSequence> indexsOfAfter(CharSequence str,int start,Collection<CharSequence> keyword) 
	{
		List<CharSequence> result = new ArrayList<>();
	    for(CharSequence word:keyword){
			if(word.toString().toLowerCase().indexOf(str.toString().toLowerCase(),start)>=start){
				result.add(word);
			}
		}
		return result;
	}
	
	/* 用单词制作Icon */
	final public wordIcon[] makeIcons(Collection<CharSequence> words, int icon)
	{
		int size = words.size();
		wordIcon[] Icons = new wordIcon[size];
		int i = 0;
		for (CharSequence word:words){
			wordIcon Icon = obtainIcon(word,icon);
		    Icons[i++] = Icon;
		}
		return Icons;
	}
	final public wordIcon[] makeIcons(Collection<CharSequence> words, String path)
	{
		int size = words.size();
		wordIcon[] Icons = new wordIcon[size];
		int i = 0;
		for (CharSequence word:words){
			wordIcon Icon = obtainIcon(word,path);
		    Icons[i++] = Icon;
		}
		return Icons;
	}
	
	protected EPool<wordIcon> getPool(){
		return mIcons;
	}
	protected wordIcon obtainIcon(){
		return mIcons.get();
	}
	protected wordIcon obtainIcon(CharSequence name, int icon)
	{
		wordIconX Icon = (wordIconX) mIcons.get();
		Icon.setIcon(icon);
		Icon.setName(name);
		return Icon;
	}
	protected wordIcon obtainIcon(CharSequence name, String path)
	{
		wordIconX Icon = (wordIconX) mIcons.get();
		Icon.setPath(path);
		Icon.setName(name);
		return Icon;
	}
	private static class IconPool extends EPool<wordIcon>
	{
		@Override
		protected wordIcon creat(){
			return new wordIconX();
		}

		@Override
		protected void resetE(wordIcon E){}

		@Override
		protected void init(){}
	}
	
	public static interface onOpenWindowLisrener
	{
		public void callOnOpenWindow(View self, Object content)
		
		public void callOnCloseWindow(View self)
		
		public void callOnRefreshWindow(View self, Object content)
	}
	
	private EPool<wordIcon> mIcons;
	
}
