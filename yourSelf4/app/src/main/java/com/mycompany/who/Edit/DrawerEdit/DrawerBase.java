package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import java.util.concurrent.*;


public abstract class DrawerBase extends Edit
{
	
	//一百行代码实现代码染色
	protected OtherWords WordLib;
	protected boolean isDraw=false;
	protected int IsModify;
	protected boolean IsModify2;
	//你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	//IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	//IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	//这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true
	public static boolean Enabled_Drawer=false;
	public static boolean Enabled_MakeHTML=false;
	
	protected ThreadPoolExecutor pool;
	public int tryLines=1;
	public String laugua;
	
	/* tmp */
	String HTML;
	SpannableStringBuilder buider;
	
	DrawerBase(Context cont){
	 	super(cont);
		WordLib=new OtherWords(6);
	}
	DrawerBase(Context cont,DrawerBase Edit){
		super(cont,Edit);
		pool = Edit. pool;
		this.WordLib=Edit.WordLib;	
	}

	abstract protected void FindFor(int start,int end,String text,List<wordIndex> nodes,SpannableStringBuilder builder)
	abstract protected void Drawing(int start,int end,List<wordIndex> nodes,SpannableStringBuilder builder)
	

	final public static void startFind(String src,List<DoAnyThing> totalList,List<wordIndex> nodes){
		//开始查找，为了保留换行空格等，只replace单词本身，而不是src文本
		//Spanned本质是用html样式替换原字符串
		//html中，多个连续空格会压缩成一个，换行会替换成空格
		//防止重复（覆盖），只遍历一次
		StringBuffer nowWord = new StringBuffer();
		int nowIndex;
		for(nowIndex=0;nowIndex<src.length();nowIndex++){
			nowWord.append(src.charAt(nowIndex));
			//每次追加一个字符，交给totalList中的任务过滤
			//注意是先追加，index后++

			//如果是其它的，可以使用用户过滤方案
			for(DoAnyThing total:totalList){
				try{
				    int index= total.dothing(src,nowWord,nowIndex,nodes);
				    if(index>=nowIndex){
				        //单词已经找到了，不用找了
						nowIndex=index;
						break;
					}
				}catch(Exception e){}
			}
		}
	}

	final public void Draw(int start,int end,List<wordIndex> nodes){
		//反向染色，前面不受后面已有Spanned影响
		IsModify++;
		isDraw=true;

		Editable editor=getText();
		String text = editor.toString().substring(start,end);
		
		try{
		    SpannableStringBuilder builder= Colors.ForeColorText(text,nodes);
			//在Edit中的真实下标开始，将范围内的单词染色
			editor.replace(start,end,builder);
	
		}catch(Exception e){}
		isDraw=false;
		IsModify--;
	}
	
	final public static String getHTML(List<wordIndex> nodes,String text){
		//中间函数，用于生成HTML文本
		StringBuffer arr = new StringBuffer();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: #abb2bf;background-color: rgb(28, 32, 37);font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		for(wordIndex node:nodes){
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textForeColor(text.substring(index,node.start),Colors. Default_));
			arr.append(Colors.  textForeColor(text.substring(node.start,node.end),Colors.fromByteToColorS(node.b)));
			index=node.end;
		}
		if(index<text.length())
			arr.append(Colors.  textForeColor(text.substring(index,text.length()),Colors.Default_));
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}

	public static void clearRepeatNode(List<wordIndex> nodes){
		//清除优先级低且位置重复的node
		int i,j;
		for(i=0;i<nodes.size();i++){
			wordIndex now = nodes.get(i);
			if(now.start==now.end){
				nodes.remove(i--);
				continue;
			}
			for(j=i+1;j<nodes.size();j++){
				if( nodes.get(j).equals(now)){
					nodes.remove(j--);
				}
			}
		}
	}
	final public static void offsetNode(List<wordIndex> nodes,int start){
		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}
	
	final public String reDraw(final int start,final int end){
		//立即进行一次默认的完整的染色	
		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				final String text = getText().subSequence(start,end).toString();
				final List<wordIndex> nodes=new ArrayList<>();
				final SpannableStringBuilder builder = new SpannableStringBuilder();
				if(text==null)
				    return;
				try{
				    FindFor(start,end,text,nodes,builder);//寻找nodes
					if(nodes.size()!=0){
					    Drawing(start,end,nodes,builder);//染色
					}
				}catch(Exception e){}
			}
		};
		if(pool!=null)
		    pool.submit(run);
		else
			run.run();
			//如果有pool，在子线程中执行
			//否则直接执行
		return "";
	}
	
	final public void prepare(final int start,final int end,final String text){
		//准备指定文本的颜料
		Runnable run = new Runnable(){

			@Override
			public void run()
			{
				final List<wordIndex> nodes=new ArrayList<>();
				final SpannableStringBuilder builder = new SpannableStringBuilder();
				FindFor(start,end,text.substring(start,end),nodes,builder);
				DrawerBase. this.buider=builder;
				HTML= getHTML(nodes,text.substring(start,end));
			}
		};
		if(pool!=null)
		    pool.submit(run);
		else
			run.run();
	}
	public void GetString(StringBuilder HTML,SpannableStringBuilder builder){
		//获取准备好了的文本
		if(this.HTML!=null){
		    HTML.append(this.HTML);
			this.HTML=null;
		}
		if(this.buider!=null){
		    builder.append(this.buider);
			this.buider=null;
		}
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		if(IsModify!=0||IsModify2){
			//如果正被修改，不允许再次修改
			return;
		}
		if (lengthAfter != 0)
		{
			//如果没有输入，则不用染色
		    
			//是否启用自动染色
			if(!Enabled_Drawer)
				return;

			IsModify2=true;
			//试探起始行和之前之后的tryLines行，并染色
			wordIndex tmp=new wordIndex(0,0,(byte)0);
			tmp.start=tryLine_Start(text.toString(),start);
			tmp.end=tryLine_End(text.toString(),start+lengthAfter);
			for(int i=1;i<tryLines;i++){
				tmp.start=tryLine_Start(text.toString(),tmp.start-1);
				tmp.end=tryLine_End(text.toString(),tmp.end+1);
			}
			reDraw(tmp.start,tmp.end);
			IsModify2=false;
		}
		
		super.onTextChanged(text, start, lengthBefore, lengthAfter);

	}
	
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
	}
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	
	
	final public static wordIndex tryWord(String src,int index){
		//试探前面的单词
		wordIndex tmp = Ep.get();
		try{
			while(Array_Splitor. indexOf(src.charAt(index),Words.fuhao)!=-1)
				index--;
			tmp.end=index+1;
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index--;
			tmp.start=index+1;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	
	final public static wordIndex tryWordAfter(String src,int index){
		//试探后面的单词
		wordIndex tmp = Ep.get();
		try{
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)!=-1)
				index++;
			tmp.start=index;
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	final public static int tryAfterIndex(String src,int index){
		//试探后面的下一个非分隔符
		while(index<src.length()
			  &&src.charAt(index)!='<'
			  &&src.charAt(index)!='>'
			  &&Array_Splitor.indexOf(src.charAt(index),Words.spilt)!=-1){
			index++;
		}
		return index;
	}
	final public static int tryLine_Start(String src,int index){
		//试探当前下标所在行的起始
		int start= src.lastIndexOf('\n',index-1);	
		if(start==-1)
			start=0;
	    else
			start+=1;
		return start;
	}
	final public static int tryLine_End(String src,int index){
		//试探当前下标所在行的末尾
		int end=src.indexOf('\n',index);
		if(end==-1)
			end=src.length();
		return end;
	}
	
	final public static wordIndex tryWordSplit(String src,int nowIndex){
		//试探纯单词
		int index=nowIndex-1;
	    wordIndex tmp = Ep.get();
		try{
			while(index>-1&&Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index--;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	final public static wordIndex tryWordSplitAfter(String src,int index){
		//试探纯单词
	    wordIndex tmp = Ep.get();
		try{
			tmp.start=index;
			while(index<src.length()&&Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	
	public static interface DoAnyThing{
		public abstract int dothing(String src,StringBuffer nowWord,int nowIndex,List<wordIndex> nodes);
		//修饰符非常重要，之前没写public，总是会函数执行异常
	}
	
	
	//根据不同情况，返回不同的单词
	
	public String[] getKeyword(){
		return WordLib.keyword;
	}
	public String[] getConstword(){
		return WordLib.constword;
	}
	public String[] getIknowtag(){
		return WordLib.IknowTag;
	}
	public char[] getFuhao(){
		return WordLib.fuhao;
	}
	public char[] getSpilt(){
		return WordLib.spilt;
	}
	public HashMap<String,String> get_zhu(){
		return WordLib.zhu_key_value;
	}
	public TreeSet<String> getLastfunc(){
		return WordLib.mdates.get(0);
	}
	public TreeSet<String> getHistoryVillber(){
		return WordLib.mdates.get(1);
	}
	public TreeSet<String> getThoseObject(){
		return WordLib.mdates.get(2);
	}
	public TreeSet<String> getBeforetype(){
		return WordLib.mdates.get(3);
	}

	public TreeSet<String> getTag(){
		return WordLib.mdates.get(4);
	}
	public TreeSet<String> getAttribute(){
		return WordLib.mdates.get(5);
	}
	
}
