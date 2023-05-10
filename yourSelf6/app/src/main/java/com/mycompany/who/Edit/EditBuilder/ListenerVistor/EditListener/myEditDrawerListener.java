package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import java.util.*;
import android.text.style.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/*
  用找到的nodes染色
  
  start和end分别表示本次要染色的text文本的开头和结尾，nodes存储刚刚Finder找到的单词，您可以在editor中将nodes设置上去
  
  注意Finder返回的是原生单词，即在start~end文本之间找到的nodes，这些单词不可直接使用，需要偏移一个start才是对的
  
  为什么这样，因为原生单词可以直接应用至start~end文本中，甚至是切割出来也可以
  
*/
public abstract class myEditDrawerListener extends myEditListener implements EditDrawerListener
{
	
	abstract protected void onDrawNodes(int start, int end, List<wordIndex> nodes, Spannable editor)
	//在这里为Editable染色
	
	@Override
	final public void LetMeDraw(int start, int end, List<wordIndex> nodes,Spannable editor)
	{
		try{
			if (Enabled())
				Draw(start,end,nodes,editor);
		}
		catch (Exception e){
			Log.e("Drawing Error", toString()+" "+e.toString());
		}
	}

	protected void Draw(int start, int end, List<wordIndex> nodes,Spannable editor){
		onDrawNodes(start, end, nodes, editor);
	}
	
	@Override
	public String getHTML(List<wordIndex> nodes,String text){
		return getHTML(nodes,text,null);
	}
	@Override
	public String getHTML(Spanned b){
		return getHTML(b,null);
	}
	
	/* 中间函数，通过nodes制作对应的HTML文本 */
    final public static String getHTML(List<wordIndex> nodes,String text,Colors.ByteToColor2 Color)
	{
		if(nodes==null||text==null)
			return "";
		if(Color==null)
			Color = new Colors.myColor2();
		StringBuilder arr = new StringBuilder();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Color.getDefultS()+";background-color: "+Color.getDefultBgS()+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		//经典开头
		
		//遍历node，将范围内的文本混合颜色制作成小段HTML文本，追加在大段文本之后
		for(wordIndex node:nodes){
			if(node.start>index)
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
				arr.append(Colors.textForeColor(text.substring(index,node.start),Color.getDefultS()));
			String color = Colors.toString(((ForegroundColorSpan)node. span).getForegroundColor());
			arr.append(Colors.textForeColor(text.substring(node.start,node.end),color));
			index=node.end;
		}
		
		if(index<text.length())
		//如果在最后有空缺的未染色部分，在html文本中也要用默认的颜色染色
			arr.append(Colors.textForeColor(text.substring(index,text.length()),Color.getDefultS()));
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
		if(Color==null)
			Color = new Colors.myColor2();
		
		//用Spanned容器中的Span，获取范围和颜色，然后制作成HTML文本
		size[] nodes = Colors. subSpanPos(0,b.length(),b,Colors.ForeSpanType);
		Object[] spans = b.getSpans(0,b.length(),Colors.SpanType);
		int index = 0;
		String text = b.toString();
		StringBuilder arr = new StringBuilder();
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Color.getDefultS()+";background-color: "+Color.getDefultBgS()+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");

		for(int i=0;i<spans.length;++i){
			size node = nodes[i];
			ForegroundColorSpan span = (ForegroundColorSpan) spans[i];
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textForeColor(text.substring(index,node.start),Color.getDefultS()));
			arr.append(Colors.textForeColor(text.substring(node.start,node.end),Colors.toString(span.getForegroundColor())));
			index=node.end;
		}
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}
	
}
