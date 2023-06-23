package com.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import android.text.style.*;
import android.util.*;
import com.who.Edit.Base.Share.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;


/*
 在文本中查找nodes

  start和end分别表示本次要染色text文本的开头和结尾，WordLib存储查找单词，nodes存储找到的要染色的范围和颜色

  每次传入的totalList，都是希望让您加入DoAnyThing方案，然后我们将会去执行查找，再在后面把结果发送给您

  由于Find函数允许重写，所以以下代码并不唯一，但在重写时尽量保证调用上面的四个抽象方法
 
*/
public abstract class myEditDrawerListener extends myEditListener implements EditDrawerListener
{
	
	private List<wordIndex> addNodes;
	private List<Object> removeNodes;
	
	public myEditDrawerListener(){
		addNodes = Collections.synchronizedList(new LinkedList<>());
		removeNodes = Collections.synchronizedList(new LinkedList<>());
	}
	
	public List<wordIndex> getDrawNodes(){
		return addNodes;
	}
	public List<Object> getRemoveNodes(){
		return removeNodes;
	}
	
	abstract protected void OnFindWord(List<DoAnyThing> totalList,Words WordLib);
	//这是查找的第一步，您可以配合WordLib将查找单词的方案装入totalList

	abstract protected void OnFindNodes(List<DoAnyThing> totalList,Words WordLib);
	//这是查找的第二步，您可以清除查找后WordLib中的错误单词

	abstract protected void OnClearFindWord(Words WordLib);
	//这是查找的第三步，您可以配合WordLib将查找nodes的方案装入totalList

	abstract protected void OnClearFindNodes(int start,int end,CharSequence text,Words WordLib,List<wordIndex> nodes);
	//这是查找的第四步，您可以清除查找后nodes中的错误node


	/*  LetMeFind函数返回原生nodes，即在start~end之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的  */
	@Override
	public void onFindNodes(int start, int end, CharSequence text, Words WordLib)
	{
		List<wordIndex> nodes=new ArrayList<>();
		List<DoAnyThing> totalList =new LinkedList<>();
		//为每一个listener分配一个nodes和totalList

		OnFindWord(totalList, WordLib); 
		startFind(start,end,text,totalList,nodes);
		totalList.clear();
		nodes.clear();
		OnClearFindWord(WordLib);

		OnFindNodes(totalList,WordLib);
		startFind(start,end,text,totalList,nodes);
		OnClearFindNodes(start, end, text, WordLib, nodes);	
	}


	/* 查找nodes 

	 * 调度一组total对一段文本进行查找，找到的node由total自已装入

	 * 查找时，为了保留换行空格等，只replace单词本身，而不是src文本

	 * 要查找的文本只能是普通文本，不可以是Span文本，因为Span文本的比较与Span也有关系

	 * 防止重复（覆盖），只遍历一次
	 */
	final public static void startFind(int start, int end, CharSequence src, List<DoAnyThing> totalList, List<wordIndex> nodes)
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
		public abstract int dothing(CharSequence src,StringBuilder nowWord,int nowIndex,List<wordIndex> nodes);
		
		public abstract Words getWords()
	}
	
	
/*

 用找到的nodes染色

 start和end分别表示本次要染色的text文本的开头和结尾，nodes存储刚刚Finder找到的单词，您可以在editor中将nodes设置上去
 
 注意Finder返回的是原生单词，即在start~end文本之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的

 为什么这样，因为原生单词可以直接应用至start~end文本中，甚至是切割出来也可以
	 
*/
	
	@Override
	public abstract void onDrawNodes(int start, int end, Spannable editor)

	/* 必须使用List存储nodes，否则无法制作HTML文本 */
	@Override
	public String makeHTML(){
		return "";
	}
	
	public String getHTML(Spanned b){
		return getHTML(b,null);
	}
	
	
	/* 中间函数，通过nodes制作对应的HTML文本 */
    final public static String getHTML(List<wordIndex> nodes,String text,Colors.ByteToColor2 Color)
	{
		if(nodes==null||text==null){
			return "";
		}
		if(Color==null){
			Color = new Colors.myColor2();
		}
		
		StringBuilder arr = new StringBuilder();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Color.getDefultS()+";background-color: "+Color.getDefultBgS()+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		//经典开头
		
		//遍历node，将范围内的文本混合颜色制作成小段HTML文本，追加在大段文本之后
		for(wordIndex node:nodes)
		{
			String color = Color.getDefultS();
			String nodeStr = text.substring(node.start,node.end);
			
			if(node.start>index){
			    //如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
				arr.append(Colors.textForeColor(text.substring(index,node.start),color));
			}
			
			if(node.span instanceof ForegroundColorSpan)
			{
				//如果span是一个ForegroundColorSpan，就用指定的颜色染色范围内的文本
			    color = Colors.toString(((ForegroundColorSpan)node. span).getForegroundColor());
			    nodeStr = Colors.textForeColor(nodeStr,color);
			}
			else if(node.span instanceof BackgroundColorSpan)
			{
				//如果span是一个BackgroundColorSpan，就用指定的颜色染色范围内的背景
				color = Colors.toString(((BackgroundColorSpan)node. span).getBackgroundColor());
				nodeStr = Colors.textBackColor(nodeStr,color);
			}
			else{
				//否则就用默认的颜色染色范围内的文本
				nodeStr = Colors.textForeColor(nodeStr,color);
			}
			
			arr.append(nodeStr);
			index=node.end;
		}
		
		if(index<text.length()){
	    	//如果在最后有空缺的未染色部分，在html文本中也要用默认的颜色染色
			arr.append(Colors.textForeColor(text.substring(index,text.length()),Color.getDefultS()));
		}
		arr.append("<br><br><br><hr><br><br></body></html>");
		//经典结尾
		return arr.toString();
	}
	
	/* 
	   中间函数，用Span文本生成HTML文本
	
	   * Spanned是最基本的存储附有Span文本的容器，只能获取Span
	   
	   * Spannable继承了Spanned，增加了设置Span功能
	  
	   * Editable继承了Spannable，增加了修改文本功能
	 
	   * SpannableStringBuilder实现了Editable，以区间树的形式存储Span文本
	   
	*/
	final public static String getHTML(Spanned b,Colors.ByteToColor2 Color)
	{
		//用Spanned容器中的Span，获取范围和颜色，然后制作成HTML文本
		wordIndex[] nodes = Colors.subSpans(0,b.length(),b,Colors.ForeSpanType);
		String text = b.toString();
		return getHTML(Arrays.asList(nodes),text,Color);
	}
	
}
