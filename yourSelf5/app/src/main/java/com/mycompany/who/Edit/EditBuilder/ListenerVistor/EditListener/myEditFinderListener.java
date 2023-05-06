package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.util.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;


/*
  在文本中查找nodes

  start和end分别表示本次要染色text文本的开头和结尾，WordLib存储查找单词，nodes存储找到的要染色的范围和颜色
 
  每次传入的totalList，都是希望让您加入DoAnyThing方案，然后我们将会去执行查找，再在后面把结果发送给您
 
  由于Find函数允许重写，所以以下代码并不唯一，但在重写时尽量保证调用上面的四个抽象方法
  
*/
public abstract class myEditFinderListener extends myEditListener implements EditFinderListener
{
	
	abstract protected void OnFindWord(List<DoAnyThing> totalList,Words WordLib);
	//这是查找的第一步，您可以配合WordLib将查找单词的方案装入totalList
	
	abstract protected void OnFindNodes(List<DoAnyThing> totalList,Words WordLib);
	//这是查找的第二步，您可以清除查找后WordLib中的错误单词
	
	abstract protected void OnClearFindWord(Words WordLib);
	//这是查找的第三步，您可以配合WordLib将查找nodes的方案装入totalList
	
	abstract protected void OnClearFindNodes(int start,int end,String text,Words WordLib,List<wordIndex> nodes);
	//这是查找的第四步，您可以清除查找后nodes中的错误node
	
	
	/*  所有Listener都会将繁琐的判断放在LetMeDo中，将主要执行过程写在Do函数中，LetMeDo函数不允许重写，但Do可以重写  */
	final public List<wordIndex> LetMeFind(int start, int end,String text,Words WordLib)
	{
		List<wordIndex> nodes = null;
		try{
			if(Enabled())
		        nodes = Find(start,end,text,WordLib);
		}
		catch (Exception e){
			Log.e("Finding Error", toString()+" "+e.toString());
		}
		return nodes;
	}

	/*  Find函数返回原生nodes，即在start~end之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的  */
	protected List<wordIndex> Find(int start, int end, String text,Words WordLib)
	{	
	    String subStr = text.substring(start,end);
		List<wordIndex> nodes=new ArrayList<>();
		List<DoAnyThing> totalList =new LinkedList<>();
		//为每一个listener分配一个nodes和totalList
		
		OnFindWord(totalList, WordLib); 
		startFind(subStr,totalList,nodes);
		totalList.clear();
		nodes.clear();
		OnClearFindWord(WordLib);
		
		OnFindNodes(totalList,WordLib);
		startFind(subStr,totalList,nodes);
		OnClearFindNodes(start, end, text, WordLib, nodes);	
		return nodes;
	}
	
	
	/* 查找nodes 
	   
	   * 调度一组total对一段文本进行查找，找到的node由total自已装入
	
	   * 查找时，为了保留换行空格等，只replace单词本身，而不是src文本
	   
	   * 要查找的文本只能是普通文本，不可以是Span文本，因为Span文本的比较与Span也有关系
	  
	   * 防止重复（覆盖），只遍历一次
	*/
	final public static void startFind(String src,List<DoAnyThing> totalList,List<wordIndex> nodes)
	{
		StringBuffer nowWord = new StringBuffer();
		int nowIndex;
		for(nowIndex=0;nowIndex<src.length();nowIndex++){
			nowWord.append(src.charAt(nowIndex));
			//每次追加一个字符，交给totalList中的任务过滤
			//注意是先追加，index后++
			
			for(DoAnyThing total:totalList){
				try{
				    int index= total.dothing(src,nowWord,nowIndex,nodes);
				    if(index>=nowIndex){
				        //单词已经找到了，不用找了
						//如果本次想放弃totalList中的后续任务，可以返回一个大于或等于传入的nowIndex的值，并且这个值还会直接设置nowIndex
						nowIndex=index;
						break;
					}
				}catch(Exception e){
					Log.e("StartFind Don't know！","The total name is"+total.toString()+"  Has Error "+e.toString());
				}
			}
		}
	}
	
	/* 清除优先级低且位置重复的node */
	final public static void clearRepeatNode(List<wordIndex> nodes)
	{		
		if(nodes==null)
			return;

		int i,j;
		for(i=0;i<nodes.size();i++){
			wordIndex now = nodes.get(i);
			if(now.start==now.end){
				//起始位置和末尾位置重复了
				nodes.remove(i--);
				continue;
			}
			for(j=i+1;j<nodes.size();j++){
				if( nodes.get(j).equals(now)){
					//范围相同的两个node，必然会覆盖，移除更前面的
					nodes.remove(j--);
				}
			}
		}
	}
	
	/* 将所有node偏移一个start */
	final public static void offsetNode(List<wordIndex> nodes,int start)
	{
		if(nodes==null)
			return;

		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}
	
	/* DoAnyThing，用于找nodes */
	public static interface DoAnyThing{
		
		public abstract int dothing(String src,StringBuffer nowWord,int nowIndex,List<wordIndex> nodes);
		//修饰符非常重要，之前没写public，总是会函数执行异常
	}
	
}
