package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;


public abstract class DrawerBase extends Edit
{
	
	//一百行代码实现代码染色
	public static Words WordLib = new Words();
	public OtherWords WordLib2;
	public boolean isDraw=false;
	public int IsModify;
	public boolean IsModify2;
	//你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	//IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	//IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	//这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true
	public static boolean Enabled_Drawer=false;
	public static boolean Enabled_MakeHTML=false;
	
	public int tryLines=2;
	public String laugua;
	
	public DrawerBase(Context cont){
	 	super(cont);
		WordLib2=new OtherWords(6);
	}
	public DrawerBase(Context cont,DrawerBase Edit){
		super(cont,Edit);
		this.WordLib2=Edit.WordLib2;	
		laugua=Edit.laugua;
	}
	public DrawerBase(Context cont,AttributeSet set){
		super(cont,set);
	}
	public void reSet(){
		config();
	}

	abstract protected ArrayList<wordIndex> FindFor(int start,int end,String text)
	abstract protected void Drawing(int start,int end,ArrayList<wordIndex> nodes)
	
	public String getToDraw(int start,int end){
		IsModify++;
		isDraw=true;
		//获取选中文本
		if(end-start==0)
			return null;
		String text = getText().toString().substring(start,end);
		if(text.length()==0)
			return null;
		getText().replace(start,end,text);
		//清除上次的颜料
		isDraw=false;
		IsModify--;
		return text;
	}

	public ArrayList<wordIndex> startFind(String src,ArrayList<DoAnyThing> totalList){
		//开始查找，为了保留换行空格等，只replace单词本身，而不是src文本
		//Spanned本质是用html样式替换原字符串
		//html中，多个连续空格会压缩成一个，换行会替换成空格
		//防止重复（覆盖），只遍历一次
		StringBuffer nowWord = new StringBuffer();
		int nowIndex;
		ArrayList<wordIndex> nodes = new ArrayList<wordIndex>();
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
		return nodes;
	}

	public void Draw(int start,int end,ArrayList<wordIndex> nodes){
		//反向染色，前面不受后面已有Spanned影响
		IsModify++;
		isDraw=true;

		Editable editor=getText();
		String text = editor.toString().substring(start,end);
		
		try{
		    SpannableStringBuilder builder= Colors.colorText(text,nodes);
			//在Edit中的真实下标开始，将范围内的单词染色
			editor.replace(start,end,builder);
	
		}catch(Exception e){}
		isDraw=false;
		IsModify--;
	}
	
	public static String getHTML(ArrayList<wordIndex> nodes,String text){
		//中间函数，用于生成HTML文本
		StringBuffer arr = new StringBuffer();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: #abb2bf;background-color: rgb(28, 32, 37);font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		for(wordIndex node:nodes){
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textColor(text.substring(index,node.start),Colors. Default_));
			arr.append(Colors.  textColor(text.substring(node.start,node.end),Colors.fromByteToColorS(node.b)));
			index=node.end;
		}
		if(index<text.length())
			arr.append(Colors.  textColor(text.substring(index,text.length()),Colors.Default_));
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}

	public void clearRepeatNode(ArrayList<wordIndex> nodes){
		//清除优先级低且位置重复的node
		int i,j;
		for(i=0;i<nodes.size();i++){
			wordIndex now = nodes.get(i);
			for(j=i+1;j<nodes.size();j++){
				if( nodes.get(j).equals(now)){
					nodes.remove(j);
					j--;
				}
			}
		}
	}
	public void offsetNode(ArrayList<wordIndex> nodes,int start){
		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}
	
	public String reDraw(final int start,final int end){
		//立即进行一次默认的完整的染色
		
		String text = getToDraw(start,end);
		final ArrayList<wordIndex> nodes;
	    String HTML = null;
		if(text==null)
			return null;

		try{
			nodes=FindFor(start,end,text);
			Drawing(start,end,nodes);
			if(Enabled_MakeHTML){
				HTML= getHTML(nodes,text);
			}
		}catch(Exception e){}

		return HTML;
	}
	
	public SpannableStringBuilder reDrawOtherText(String text){
		ArrayList<wordIndex> nodes = FindFor(0,text.length(),text);
		return Colors.colorText(text,nodes);
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
	public static wordIndex tryWord(String src,int index){
		//试探前面的单词
		wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			while(Array_Splitor. indexOf(src.charAt(index),DrawerBase. WordLib.fuhao)!=-1)
				index--;
			tmp.end=index+1;
			while(Array_Splitor.indexOf(src.charAt(index),DrawerBase.WordLib.fuhao)==-1)
				index--;
			tmp.start=index+1;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}


	public static wordIndex tryWordAfter(String src,int index){
		//试探后面的单词
		wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			while(Array_Splitor.indexOf(src.charAt(index),DrawerBase.WordLib.fuhao)!=-1)
				index++;
			tmp.start=index;
			while(Array_Splitor.indexOf(src.charAt(index),DrawerBase.WordLib.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	public static int tryAfterIndex(String src,int index){
		//试探后面的下一个非分隔符
		while(index<src.length()
			  &&src.charAt(index)!='<'
			  &&src.charAt(index)!='>'
			  &&Array_Splitor.indexOf(src.charAt(index),DrawerBase.WordLib.spilt)!=-1){
			index++;
		}
		return index;
	}
	public static int tryLine_Start(String src,int index){
		//试探当前下标所在行的起始
		int start= src.lastIndexOf('\n',index-1);	
		if(start==-1)
			start=0;
	    else
			start+=1;
		return start;
	}
	public static int tryLine_End(String src,int index){
		//试探当前下标所在行的末尾
		int end=src.indexOf('\n',index);
		if(end==-1)
			end=src.length();
		return end;
	}
	
	public static wordIndex tryWordSplit(String src,int nowIndex){
		//试探纯单词
		int index=nowIndex-1;
	    wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			while(index>-1&&Array_Splitor.indexOf(src.charAt(index),WordLib.fuhao)==-1)
				index--;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	public static wordIndex tryWordSplitAfter(String src,int index){
		//试探纯单词
	    wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			tmp.start=index;
			while(index<src.length()&&Array_Splitor.indexOf(src.charAt(index),WordLib.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	
	public static abstract class DoAnyThing{
		public abstract int dothing(String src,StringBuffer nowWord,int nowIndex,ArrayList<wordIndex> nodes);
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
		return WordLib2.mdates.get(0);
	}
	public TreeSet<String> getHistoryVillber(){
		return WordLib2.mdates.get(1);
	}
	public TreeSet<String> getThoseObject(){
		return WordLib2.mdates.get(2);
	}
	public TreeSet<String> getBeforetype(){
		return WordLib2.mdates.get(3);
	}

	public TreeSet<String> getTag(){
		return WordLib2.mdates.get(4);
	}
	public TreeSet<String> getAttribute(){
		return WordLib2.mdates.get(5);
	}
	
}
