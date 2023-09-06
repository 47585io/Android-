package com.editor.text2.builder.listener.base;

import android.graphics.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import com.editor.text2.base.*;
import java.util.*;

public abstract class EditDrawerListener 
{

	private List<wordIndex> addNodes;
	private List<Object> removeNodes;

	public EditDrawerListener(){
		addNodes = Collections.synchronizedList(new ArrayList<>());
		removeNodes = Collections.synchronizedList(new ArrayList<>());
	}

	public List<wordIndex> getDrawNodes(){
		return addNodes;
	}
	public List<Object> getRemoveNodes(){
		return removeNodes;
	}

	abstract protected void OnFindWord(List<DoAnyThing> totalList);
	//这是查找的第一步，您可以配合WordLib将查找单词的方案装入totalList

	abstract protected void OnFindNodes(List<DoAnyThing> totalList);
	//这是查找的第二步，您可以清除查找后WordLib中的错误单词

	abstract protected void OnClearFindWord();
	//这是查找的第三步，您可以配合WordLib将查找nodes的方案装入totalList

	abstract protected void OnClearFindNodes(int start,int end,CharSequence text,List<wordIndex> nodes);
	//这是查找的第四步，您可以清除查找后nodes中的错误node


	/*  LetMeFind函数返回原生nodes，即在start~end之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的  */
	public void onFindNodes(int start, int end, String text)
	{
		List<DoAnyThing> totalList =new LinkedList<>();
		//为每一个listener分配一个totalList

		OnFindWord(totalList); 
		startFind(start,end,text,totalList,addNodes);
		totalList.clear();
		addNodes.clear();
		OnClearFindWord();

		OnFindNodes(totalList);
		startFind(start,end,text,totalList,addNodes);
		OnClearFindNodes(start, end, text, addNodes);	
	}


	/* 查找nodes 

	 * 调度一组total对一段文本进行查找，找到的node由total自已装入

	 * 查找时，为了保留换行空格等，只replace单词本身，而不是src文本

	 * 要查找的文本只能是普通文本，不可以是Span文本，因为Span文本的比较与Span也有关系

	 * 防止重复（覆盖），只遍历一次
	 */
	final public static void startFind(int start, int end, String src, List<DoAnyThing> totalList, List<wordIndex> nodes)
	{
		StringBuilder nowWord = new StringBuilder();
		for(;start<end;++start)
	    {
			nowWord.append(src.charAt(start));
			//每次追加一个字符，交给totalList中的任务过滤
			//注意是先追加，index后++		
			for(DoAnyThing total:totalList)
			{
				try
			    {
				    int index = total.dothing(src,nowWord,start,nodes);
				    if(index>=start)
					{
				        //单词已经找到了，不用找了
						//如果本次想放弃totalList中的后续任务，可以返回一个大于或等于传入的start的值，并且这个值还会直接设置nowIndex
						start=index;
						break;
					}
				}
				catch(Exception e){
					Log.e("StartFind Don't know！","The total name is"+ total.toString()+"  Has Error "+e.toString());
				}
			}
		}
	}

	/* 清除优先级低且位置重复的node */
	final public static void clearRepeatNode(List<wordIndex> nodes)
	{		
		if(nodes==null){
			return;
		}

		int i,j;
		for(i=0;i<nodes.size();++i)
	    {
			wordIndex now = nodes.get(i);
			if(now.start==now.end)
			{
				//起始位置和末尾位置重复了
				nodes.remove(i--);
				continue;
			}
			for(j=i+1;j<nodes.size();++j)
		    {
				if(nodes.get(j).equals(now)){
					//范围相同的两个node，必然会覆盖，移除更前面的
					nodes.remove(j--);
				}
			}
		}
	}

	/* 将所有node偏移一个start */
	final public static void offsetNode(List<wordIndex> nodes,int start)
	{
		if(nodes==null){
			return;
		}

		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}

	/* DoAnyThing，用于找nodes */
	public static interface DoAnyThing
	{
		public abstract int dothing(String src,StringBuilder nowWord,int nowIndex,List<wordIndex> nodes);
		
	}


	/*

	 用找到的nodes染色

	 start和end分别表示本次要染色的text文本的开头和结尾，nodes存储刚刚Finder找到的单词，您可以在editor中将nodes设置上去

	 注意Finder返回的是原生单词，即在start~end文本之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的

	 为什么这样，因为原生单词可以直接应用至start~end文本中，甚至是切割出来也可以

	 */

	public abstract void onDrawNodes(int start, int end, Spannable editor)

	/* 必须使用List存储nodes，否则无法制作HTML文本 */
	public String makeHTML(){
		return "";
	}

	public String getHTML(Spanned b){
		return null;
	}

}
