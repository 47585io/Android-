package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import android.util.*;

public class DrawerText extends DrawerBase2
{
	public DrawerText(Context cont){
		super(cont);
	}
	public DrawerText(Context cont,DrawerText Edit)
	{
		super(cont,Edit);
	}
	public DrawerText(Context cont,AttributeSet set){
		super(cont,set);
	}
	public void setLuagua(String name){
		laugua=name;
		if(name.equals("text"))
		    setDefaultFinder(new FinderText());
	}
	
	class AnyThingForText{
		//勇往直前的GoTo块，会突进一大段并阻拦其它块
		public DoAnyThing getGoTo_zhuShi(){
			//获取注释
			return new DoAnyThing(){		
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					String key = String_Splitor.indexOfKey(src,nowIndex,get_zhu());
					if(key!=null){
						//如果它是一个任意的注释，找到对应的另一个，并把它们之间染色
						String value= get_zhu().get(key);
						int nextindex = src.indexOf(value,nowIndex+key.length());

						if(nextindex!=-1){
							saveChar(src,nowIndex,nextindex+value.length(),Colors.color_zhu,nodes);
						    nowIndex= nextindex+ value.length()-1;
						}
						else{
							//如果找不到默认认为到达了末尾
							saveChar(src,nowIndex,src.length(),Colors.color_zhu,nodes);
							nowIndex=src.length()-1;
						}
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}

					return -1;
				}
			};
		}	
		public DoAnyThing getGoTo_Str(){
			//获取字符串
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					if(src.charAt(nowIndex)=='"'){
						//如果它是一个"，一直找到对应的"
						int endIndex = src.indexOf('"',nowIndex+1);
						if(endIndex!=-1){
							saveChar(src,nowIndex,endIndex+1,Colors.color_str,nodes);
							nowIndex=endIndex;
						}
						else{
							//如果找不到默认认为到达了末尾
							saveChar(src,nowIndex,src.length(),Colors.color_str,nodes);
							nowIndex=src.length()-1;
						}
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}		
					else if(src.charAt(nowIndex)=='\''){
						//如果它是'字符，将之后的字符加进来
						if(src.charAt(nowIndex+1)=='\\'){
							nodes.add(new wordIndex(nowIndex,nowIndex+4,Colors.color_str));
							nowIndex+=3;	
						}

						else{		
						    int endIndex = src.indexOf('\'',nowIndex+1);
							saveChar(src,nowIndex,endIndex+1,Colors.color_str,nodes);
							nowIndex=endIndex;
						}
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}	

					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Char(){
			//获取字符
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					if(String_Splitor. indexOfNumber(src.charAt(nowIndex))){
						//否则如果当前的字符是一个数字，就把它加进nodes
						//由于关键字和保留字一定没有数字，所以可以清空之前的字符串
						nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_number));
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}	
					else if(Array_Splitor.indexOf(src.charAt(nowIndex),getFuhao())!=-1){	
						//否则如果它是一个特殊字符，就更不可能了，清空之前累计的字符串
						if(Array_Splitor.indexOf(src.charAt(nowIndex),getSpilt())==-1)
						//如果它不是会被html文本压缩的字符，将它自己加进nodes
						//这是为了保留换行空格等
							nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_fuhao));

						nowWord.replace(0,nowWord.length(),"");
						//清空之前累计的字符串
						return nowIndex;
					}
					return -1;
				}
			};
		}
		
		public DoAnyThing getCanTo(){
		
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					//如果后面还有英文字符，它应该不是任意单词
					//为了节省时间，将更简单的条件放前面，触发断言机制
					if(String_Splitor.IsAtoz(src.charAt(nowIndex+1)))
						return nowIndex;
					return -1;
				}
			};
		}
		
		public void saveChar(String src,int nowIndex,int nextindex,byte wantColor,ArrayList<wordIndex> nodes){
			int startindex=nowIndex;
			for(;nowIndex<nextindex;nowIndex++){
				//保留特殊字符
				if(Array_Splitor.indexOf(src.charAt(nowIndex),getSpilt())!=-1){
					nodes.add(new wordIndex(startindex,nowIndex,wantColor));
					startindex=nowIndex+1;
				}
			}
			nodes.add(new wordIndex(startindex,nextindex,wantColor));

		}
	}
	
	class FinderText extends EditFinderListener
	{

		@Override
		public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
	   	{
			AnyThingForText AllThings = new AnyThingForText();
			totalList.add(AllThings.getGoTo_zhuShi());
			totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getNoSans_Char());
		}

		@Override
		public void OnClearFindWord(TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnClearDrawWord(int start,int end,String text, ArrayList<wordIndex> nodes)
		{
			
		}
	}
	
	
	
	
}
