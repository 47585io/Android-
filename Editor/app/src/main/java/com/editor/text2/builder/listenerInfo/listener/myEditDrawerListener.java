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


public abstract class myEditDrawerListener extends myEditListener implements EditDrawerListener
{

	public myEditDrawerListener(){
		mNodes = new NodePool();
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
	
	final public static void offsetNodes(wordIndex[] nodes,int start)
	{
		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}

	@Override
	public String makeHTML(int start, int end, CharSequence text, wordIndex[] nodes)
	{
		return null;
	}
	

	public wordIndex obtainNode(){
		return mNodes.get();
	}
	public wordIndex obtainNode(int start, int end, Object span)
	{
		wordIndex node = new wordIndex();
		node.set(start,end,span,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return node;
	}
	public wordIndex obtainNode(int start, int end, Object span, int flags)
	{
		wordIndex node = new wordIndex();
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
	
	private static int IdCount = 0;
	private final int mListenerId = (++IdCount)<<SPAN_ID_SHIFT;
	private EPool<wordIndex> mNodes;

	//用13~14位表示span的情况，在draw时去做判断并设置或移除span
	//用17~32位表示span的id，在remove时不要移除不是你设置的span
	public static final int SPAN_SET = 0x1000;
    public static final int SPAN_REMOVE = 0x2000;
    public static final int SPAN_SET_REMOVE_MASK = 0x3000;
	public static final int SPAN_ID_MASK = 0xFFFF0000;
	public static final int SPAN_ID_SHIFT = 16;
	
}
