package com.editor.text2.builder.listenerInfo.listener;

import android.text.*;
import android.util.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import java.util.*;
import com.editor.text2.base.*;
import com.editor.text2.builder.words.*;
import com.editor.text2.base.share.*;
import com.editor.text.*;


public abstract class myEditDrawerListener extends myEditListener implements EditDrawerListener
{

	private EPool<wordIndex> mNodes;
	
	public myEditDrawerListener(){
		mNodes = new NodePool();
	}
	
	public wordIndex obtainNode(){
		return mNodes.get();
	}
	public wordIndex obtainNode(int start, int end, Object span)
	{
		wordIndex node = new wordIndex();
		node.set(start,end,span);
		return node;
	}
	
	public abstract wordIndex[] howToFindNodes(int start, int end, CharSequence text, Words lib)
	
	@Override
	public wordIndex[] onFindNodes(int start, int end, CharSequence text, Words lib)
	{
		mNodes.start();
		wordIndex[] nodes = howToFindNodes(start,end,text,lib);
		mNodes.stop();
		return nodes;
	}

	@Override
	public void onDrawNodes(int start, int end, Spannable editor, wordIndex[] nodes)
	{
		Object[] spans = SpanUtils.getSpans(editor,start,end,Object.class);
		for(int i=spans.length-1;i>=0;--i){
			editor.removeSpan(spans[i]);
		}
		for(int i=nodes.length-1;i>=0;--i){
			wordIndex node = nodes[i];
			editor.setSpan(node.span, node.start+start, node.end+start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	@Override
	public String makeHTML(int start, int end, CharSequence text, wordIndex[] nodes)
	{
		// TODO: Implement this method
		return null;
	}
	
	private static class NodePool extends EPool<wordIndex>
	{

		@Override
		protected wordIndex creat(){
			return new wordIndex();
		}

		@Override
		protected void resetE(wordIndex E){
			E.set(0,0,null);
		}

		@Override
		protected void init(){}

	}
	
}
