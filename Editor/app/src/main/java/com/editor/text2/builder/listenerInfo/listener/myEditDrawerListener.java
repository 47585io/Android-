package com.editor.text2.builder.listenerInfo.listener;

import android.text.*;
import com.editor.text.*;
import com.editor.text2.base.share1.*;
import com.editor.text2.base.share4.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import com.editor.text2.builder.words.*;
import java.util.*;
import android.text.style.*;
import com.editor.text.base.*;
import android.util.*;
import android.graphics.*;


public abstract class myEditDrawerListener extends myEditListener implements EditDrawerListener
{

	public myEditDrawerListener()
	{
		mNodes = new NodePool();
		if((IdCount+1) > (SPAN_ID_MASK>>SPAN_ID_SHIFT)){
			IdCount = 0;
		}
		mListenerId = (++IdCount)<<SPAN_ID_SHIFT;
	}
	
	@Override
	public wordIndex[] onFindNodes(int start, int end, CharSequence text, Words lib)
	{
		mNodes.start();
		wordIndex[] nodes = howToFindNodes(start,end,text,lib);
		nodes = replaceSpans(start,end,text,nodes);
		return nodes;
	}
	
	public abstract wordIndex[] howToFindNodes(int start, int end, CharSequence text, Words lib)
	
	//保留范围相同的span，去除未找到的span
	private wordIndex[] replaceSpans(int start, int end, CharSequence text, wordIndex[] nodes)
	{
		//获取span
		Object[] spans = SpanUtils.getSpans(text,start,end,Object.class);
		final int spanCount = spans.length;
		final int nodeCount = nodes.length;
		if(spanCount==0){
			//如果没有span，则node全都要设置
			for(int i=0;i<nodeCount;++i){
				nodes[i].flags |= SPAN_SET | mListenerId;
			}
			return nodes;
		}
		
		//即使没有node，但也要检查要移除的span
		Spanned sp = (Spanned) text;
		int[] spanStarts = new int[spanCount];
		int[] spanEnds = new int[spanCount];
		int[] spanFlags = new int[spanCount];
		for(int i=0;i<spanCount;++i){
			spanStarts[i] = sp.getSpanStart(spans[i]);
			spanEnds[i] = sp.getSpanEnd(spans[i]);
			spanFlags[i] = sp.getSpanFlags(spans[i]);
		}
		
		//设置标志
		boolean[] hasSpans = new boolean[spanCount];
		for(int i=0;i<nodeCount;++i)
		{
			wordIndex node = nodes[i];
			Object span = node.span;
			int st = node.start;
			int en = node.end;
			int fl = node.flags;
			int j;
			for(j=0;j<spanCount;++j)
			{
				if(spanStarts[j]==st && spanEnds[j]==en &&
				   (spanFlags[j] &~ SPAN_ID_MASK)==fl && spans[j].equals(span)){
					//如果该node的span已存在于文本中的相同位置，则不用再次设置
					//同时，下标为j的span(也就是正处于文本中的该span)不用移除
					node.flags &= ~SPAN_SET_REMOVE_MASK;
					hasSpans[j] = true;
					break;
				}
			}
			if(j==spanCount){
				//如果该node的span不存在于文本中的相同位置，则设置span
				node.flags |= SPAN_SET | mListenerId;
			}
		}
		
		//添加要移除的nodes
		List<wordIndex> removeNodes = new ArrayList<>();
		for(int i=0;i<spanCount;++i)
		{
			if(!hasSpans[i] && spanStarts[i]>=start && spanEnds[i]<=end &&
			   (spanFlags[i] & SPAN_ID_MASK) == mListenerId){
				//下标为i的span不存在于所有nodes之中，意味着它不应存在于当前文本中，它应该被移除
				//前提是它应完全在此范围内(才能被找到)，并且不应移除别人设置的span
				wordIndex node = obtainNode(0,0,spans[i],SPAN_REMOVE);
				removeNodes.add(node);
			}
		}
		final int removeCount = removeNodes.size();
		if(removeCount>0){
			wordIndex[] newNodes = new wordIndex[nodeCount+removeCount];
			removeNodes.toArray(newNodes);
			System.arraycopy(nodes,0,newNodes,removeCount,nodeCount);
			nodes = newNodes;
		}
		return nodes;
	}

	@Override
	public void onDrawNodes(int start, int end, Spannable editor, wordIndex[] nodes)
	{
		int nodeCount = nodes.length;
		for(int i=0;i<nodeCount;++i)
		{
			wordIndex node = nodes[i];
			int flag = node.flags & SPAN_SET_REMOVE_MASK;
			if(flag==SPAN_SET){
				node.flags &= ~SPAN_SET_REMOVE_MASK;
				editor.setSpan(node.span,node.start,node.end,node.flags);
			}
			else if(flag==SPAN_REMOVE){
				editor.removeSpan(node.span);
			}
		}
		mNodes.stop();
	}
	
	@Override
	public String makeHTML(int start, int end, CharSequence text, wordIndex[] nodes)
	{
		StringBuilder builder = new StringBuilder();
		int index=0;
		builder.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Colors.Foreground+";background-color: "+Colors.Background+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		//经典开头

		char[] arr = EmptyArray.CHAR;
		//遍历node，将范围内的文本混合颜色制作成小段HTML文本，追加在大段文本之后
		for(wordIndex node:nodes)
		{
			String color = "";
			int len = node.end-node.start;
			if(arr.length<len){
				arr = ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(len));
			}
			TextUtils.getChars(text,node.start,node.end,arr,0);
			String nodeStr = String.valueOf(arr,0,len);
			
			if(node.start>index){
			    //如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
				len = node.start-index;
				if(arr.length<len){
					arr = ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(len));
				}
				TextUtils.getChars(text,index,node.start,arr,0);
				String subStr = String.valueOf(arr,0,len);
				builder.append(Colors.textForeColor(subStr,color));
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

			builder.append(nodeStr);
			index=node.end;
		}

		if(index<text.length()){
	    	//如果在最后有空缺的未染色部分，在html文本中也要用默认的颜色染色
			int len = text.length()-index;
			if(arr.length<len){
				arr = ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(len));
			}
			TextUtils.getChars(text,index,text.length(),arr,0);
			String nodeStr = String.valueOf(arr,0,len);
			builder.append(Colors.textForeColor(nodeStr,Colors.Foreground));
		}
		builder.append("<br><br><br><hr><br><br></body></html>");
		//经典结尾
		return arr.toString();
	}
	
	public static final wordIndex[] startFind(int start, int end, CharSequence text, Finder... finders)
	{
		int finderCount = finders.length;
		int len = end-start;
		char[] arr = new char[len];
		TextUtils.getChars(text,start,end,arr,0);
		String str = String.valueOf(arr);
		StringBuilder nowWord = new StringBuilder();
		List<wordIndex> nodes = new LinkedList<>();
		for(int nowIndex=0;nowIndex<len;++nowIndex)
	    {
			nowWord.append(arr[nowIndex]);
			//每次追加一个字符，交给totalList中的任务过滤
			//注意是先追加，index后++		
			for(int j=0;j<finderCount;++j)
			{
				Finder finder = finders[j];
				try{
				    int index = finder.find(str,nowWord,nowIndex,nodes);
				    if(index>=nowIndex){
				        //单词已经找到了，不用找了
						//如果本次想放弃totalList中的后续任务，可以返回一个大于或等于传入的nowIndex的值，并且这个值还会直接设置nowIndex
						nowIndex=index;
						break;
					}
				}catch(Exception e){
					Log.e("StartFind Don't know！","The total name is"+finder.toString()+"  Has Error "+e.toString());
				}
			}
		}
		wordIndex[] nodeArray = new wordIndex[nodes.size()];
		nodes.toArray(nodeArray);
		offsetNodes(nodeArray,start);
		return nodeArray;
	}
	
	final public static void offsetNodes(wordIndex[] nodes,int start)
	{
		for(int i=0;i<nodes.length;++i){
			nodes[i].start+=start;
			nodes[i].end+=start;
		}
	}
	
	public static interface Finder
	{
		public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
	}
	

	protected EPool<wordIndex> getPool(){
		return mNodes;
	}
	protected wordIndex obtainNode(){
		return mNodes.get();
	}
	protected wordIndex obtainNode(int start, int end, Object span)
	{
		wordIndex node = mNodes.get();
		node.set(start,end,span,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return node;
	}
	protected wordIndex obtainNode(int start, int end, Object span, int flags)
	{
		wordIndex node = mNodes.get();
		node.set(start,end,span,flags);
		return node;
	}
	private static class NodePool extends EPool<wordIndex>
	{
		@Override
		protected wordIndex creat(){
			return new wordIndex();
		}

		@Override
		protected void resetE(wordIndex E){
			E.set(0,0,null,0);
		}

		@Override
		protected void init(){}
	}
	
	public static class ForegroundColorSpanX extends ForegroundColorSpan
	{
		public ForegroundColorSpanX(int color){
			super(color);
		}

		@Override
		public int hashCode(){
			return getForegroundColor();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof ForegroundColorSpan && ((ForegroundColorSpan)obj).getForegroundColor()==getForegroundColor()){
				return true;
			}
			return false;
		}
	}
	public static class BackgroundColorSpanX extends BackgroundColorSpan
	{
		public BackgroundColorSpanX(int color){
			super(color);
		}

		@Override
		public int hashCode(){
			return getBackgroundColor();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof BackgroundColorSpan && ((BackgroundColorSpan)obj).getBackgroundColor()==getBackgroundColor()){
				return true;
			}
			return false;
		}
	}
	
	public static class Colors
	{
		public static String Foreground ="#abb2bf";//灰白
		public static String Background = "#222222";
		public static final int zhuShi =0xff585f65;//深灰
		public static final int Str=0xff98c379;//青草奶油
		public static final int FuHao =0xff99c8ea;//蓝
		public static final int Number=0xffff9090;//橙红柚子
		public static final int KeyWord=0xffcc80a9;//桃红乌龙
		public static final int Const =0xffcd9861;//枯叶黄
		public static final int Villber =0xffff9090;
		public static final int Function =0xff99c8ea;
		public static final int Type=0xffd4b876;
		public static final int Attribute=0xffcd9861;//枣
		public static final int Tag=0xffde6868;
		
		public static String textForeColor(String src,String fgcolor){
			src=Replace(src);
			return "<pre style='display:inline;color:"+fgcolor+";'>"+src+"</pre>";
		}
		public static String textBackColor(String src,String bgcolor){
			src=Replace(src);
			return "<pre style='display:inline;background-color:"+bgcolor+";'>"+src+"</pre>";
		}
		public static String textColor(String src,String fgcolor,String bgcolor){
			src=Replace(src);
			return "<pre style='display:inline;"+"color:"+ fgcolor+";background-color:"+bgcolor+";'>"+src+"</pre>";
		}
		private static String Replace(String src)
		{
			src=src.replaceAll("<","&lt;");
			src=src.replaceAll(">","&gt;");
			src=src.replaceAll("\t","    ");
			src=src.replaceAll(" ","&nbsp;");
			src=src.replaceAll("\n","<br/>");
			//替换被HTML解析的字符
			return src;
		}
		public static int vualeOf(String color){
			return Integer.parseInt(color,16);
		}
		public static String toString(int color)
		{
			int red = Color.red(color);
			int green = Color.green(color);
			int blue = Color.blue(color);
			int alpha = Color.alpha(color);
			StringBuilder b = new StringBuilder();
			b.append("rgba(");
			b.append(red+",");
			b.append(green+",");
			b.append(blue+",");
			b.append(alpha+")");
			return b.toString();
		}
	}
	
	private int mListenerId;
	private EPool<wordIndex> mNodes;
	private static int IdCount = 0;
	
	//用9~10位表示span的情况，在draw时去做判断并设置或移除span
	//用25~32位表示span的id，在remove时不要移除不是你设置的span
	public static final int SPAN_SET = 0x100;
    public static final int SPAN_REMOVE = 0x200;
    public static final int SPAN_SET_REMOVE_MASK = 0x300;
	public static final int SPAN_ID_MASK = 0xFF000000;
	public static final int SPAN_ID_SHIFT = 24;
	
}
