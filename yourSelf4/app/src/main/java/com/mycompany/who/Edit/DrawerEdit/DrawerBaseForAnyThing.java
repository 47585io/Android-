package com.mycompany.who.Edit.DrawerEdit;
import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;

public abstract class DrawerBaseForAnyThing extends DrawerBase2
{
	//所有的AnyThing工厂
	
	DrawerBaseForAnyThing(Context cont){
		super(cont);
	}
	DrawerBaseForAnyThing(Context cont,DrawerBaseForAnyThing Edit){
		super(cont,Edit);
	}
	
	//Text工厂
	 class AnyThingForText{
		//勇往直前的GoTo块，会突进一大段并阻拦其它块
		public DoAnyThing getGoTo_zhuShi(){
			//获取注释
			return new DoAnyThing(){		
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
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
						nowWord.delete(0,nowWord.length());
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
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
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
						nowWord.delete(0,nowWord.length());
						return nowIndex;
					}		
					else if(src.charAt(nowIndex)=='\''){
						//如果它是'字符，将之后的字符加进来
						if(src.charAt(nowIndex+1)=='\\'){
							wordIndex node= Ep.get();
							node.set(nowIndex,nowIndex+4,Colors.color_str);
							nodes.add(node);
							nowIndex+=3;	
						}

						else{		
						    int endIndex = src.indexOf('\'',nowIndex+1);
							saveChar(src,nowIndex,endIndex+1,Colors.color_str,nodes);
							nowIndex=endIndex;
						}
						nowWord.delete(0,nowWord.length());
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
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					if(String_Splitor. indexOfNumber(src.charAt(nowIndex))){
						//否则如果当前的字符是一个数字，就把它加进nodes
						//由于关键字和保留字一定没有数字，所以可以清空之前的字符串
						wordIndex node= Ep.get();
						node.set(nowIndex,nowIndex+1,Colors.color_number);
						nodes.add(node);
						nowWord.delete(0,nowWord.length());
						return nowIndex;
					}	
					else if(Array_Splitor.indexOf(src.charAt(nowIndex),getFuhao())!=-1){	
						//否则如果它是一个特殊字符，就更不可能了，清空之前累计的字符串
						if(Array_Splitor.indexOf(src.charAt(nowIndex),getSpilt())==-1){
						//如果它不是会被html文本压缩的字符，将它自己加进nodes
						//这是为了保留换行空格等
						    wordIndex node= Ep.get();
							node.set(nowIndex,nowIndex+1,Colors.color_fuhao);
							nodes.add(node);
						}
						nowWord.delete(0,nowWord.length());
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
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					//如果后面还有英文字符，它应该不是任意单词
					//为了节省时间，将更简单的条件放前面，触发断言机制
					if(String_Splitor.IsAtoz(src.charAt(nowIndex+1)))
						return nowIndex;
					return -1;
				}
			};
		}

		public void saveChar(String src,int nowIndex,int nextindex,byte wantColor,List<wordIndex> nodes){
			int startindex=nowIndex;
			for(;nowIndex<nextindex;nowIndex++){
				//保留特殊字符
				if(Array_Splitor.indexOf(src.charAt(nowIndex),getSpilt())!=-1){
					wordIndex node= Ep.get();
					node.set(startindex,nowIndex,wantColor);
					nodes.add(node);
					startindex=nowIndex+1;
				}
			}
			wordIndex node= Ep.get();
			node.set(startindex,nextindex,wantColor);
			nodes.add(node);
			
		}
	}
	
	
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
	
	//XML工厂
	class AnyThingForXML extends AnyThingForText{
		public DoAnyThing getDraw_Tag(){
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					//简单的一个xml方案
					wordIndex node;
					if(src.charAt(nowIndex)=='<'){
						node=tryWordAfter(src,nowIndex+1);
						node.b=Colors.color_tag;
						getTag().add(src.substring(node.start,node.end));
						nodes.add(node);
						nowIndex=node.end-1;
						return nowIndex;
					}

					return -1;
				}
			};
		}
		public DoAnyThing getDraw_Attribute(){
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='='
					   ||src.charAt(nowIndex)==':'
					   ){
						node=tryWord(src,nowIndex-1);
						node.b=Colors.color_attr;
						getAttribute().add(src.substring(node.start,node.end));
						nodes.add(node);
						nowIndex=node.end-1;
						return nowIndex;
					}
					return -1;
				}
			};
		}

	}
	
	
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
	
	//Java工厂
	final public class AnyThingForJava extends AnyThingForText{

		//不回溯的NoSans块，用已有信息完成判断
		public DoAnyThing getNoSans_Keyword(){
			//获取关键字和保留字
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					//找到一个单词 或者 未找到单词就遇到特殊字符，就把之前累计字符串清空
					//为了节省时间，将更简单的条件放前面，触发&&的断言机制
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&src.charAt(nowIndex+1)!='_'&&getKeyword().contains(nowWord.toString())){
						//如果当前累计的字符串是一个关键字并且后面没有a～z这些字符，就把它加进nodes
						wordIndex node= Ep.get();
						node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_key);
						nodes.add(node);
						nowWord.delete(0,nowWord.length());
					    return nowIndex;
					}
					else if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getConstword().contains(nowWord.toString())){
						//否则如果当前累计的字符串是一个保留字并且后面没有a～z这些字符，就把它加进nodes
						wordIndex node= Ep.get();
						node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_const);
						nodes.add(node);
						nowWord.delete(0,nowWord.length());
						return nowIndex;
					}		

					//关键字和保留字和变量不重复，所以只要中了其中一个，则就是那个
					//如果能进关键字和保留字和变量的if块，则说明当前字符一定不是特殊字符

					return -1;
				}	
			};
		}


		public DoAnyThing getNoSans_Func(){
			//获取函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					int afterIndex=tryAfterIndex(src,nowIndex+1);
					if(src.charAt(afterIndex)=='('){
					    if(getLastfunc().contains(nowWord.toString())){
							//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
							wordIndex node= Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_func);
							nodes.add(node);
						    nowWord.delete(0,nowWord.length());
						    return afterIndex-1;
						}
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Villber(){
			//获得变量
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getHistoryVillber().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个变量并且后面没有a～z和（ 这些字符，就把它加进nodes
							wordIndex node= Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_villber);
							nodes.add(node);
						    nowWord.delete(0,nowWord.length());
						    return afterIndex-1;
						}
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Object(){
			//获取对象
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getThoseObject().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个对象并且后面没有a～z和（ 这些字符，就把它加进nodes
							wordIndex node= Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_obj);
							nodes.add(node);
						    nowWord.delete(0,nowWord.length());
						    return afterIndex-1;
						}
					}
					return -1;
				}
			};
		}

		public DoAnyThing getNoSans_Type(){
			//获取类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getBeforetype().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个类型并且后面没有a～z和（ 这些字符，就把它加进nodes
							wordIndex node= Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_type);
							nodes.add(node);
						    nowWord.delete(0,nowWord.length());
						    return afterIndex-1;

						}
					}
					return -1;
				}
			};
		}


		//会回溯的Sans块，试探并记录单词
		public DoAnyThing getSans_TryFunc(){
			//试探函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='('){
						//如果它是(字符，将之前的函数名存起来
						node=tryWord(src,nowIndex-1);
						getLastfunc().add(src.substring(node.start,node.end));
						return nowIndex;
					}

					return -1;	
				}	
			};
		}
		public DoAnyThing getSans_TryVillber(){
			//试探变量和类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{					 
					wordIndex node; 
					if((src.charAt(nowIndex)=='=')){
						//如果它是.或=字符，将之前的对象名或变量存起来	
						//=前后必须是分割符或普通的英文字符，不能是任何与=结合的算术字符
						node=tryWord(src,nowIndex-1);
						if(src.charAt(nowIndex)=='='&&!getHistoryVillber().contains(src.substring(node.start,node.end))
						   &&Array_Splitor.indexOf(src.charAt(nowIndex-1),arr)==-1
						   &&Array_Splitor.indexOf(src.charAt(nowIndex+1),arr)==-1){
							//二次试探，得到类型
							//变量必须首次出现才有类型
							int nowN= tryLine_Start(src, node.start);
							wordIndex tmp = tryWord(src,node.start-1);
							if(tmp.start>nowN)
							//类型与变量必须在同一行
							    getBeforetype().add(src.substring(tmp.start,tmp.end));
						}

						getHistoryVillber().add(src.substring(node.start,node.end));
						return nowIndex;
					}
					return -1;
			    }
			};
		}
		public DoAnyThing getSans_TryObject(){
			//试探对象
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='.'&&!String_Splitor.indexOfNumber(src.charAt(nowIndex+2))){
						node=tryWord(src,nowIndex-1);
						getThoseObject().add(src.substring(node.start,node.end));
						return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getSans_TryType(){
			//试探类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{	
				   	if(!String_Splitor.IsAtoz(src.charAt(nowIndex+1))&&getKeyword().contains(nowWord.toString())){
						String Word=nowWord.toString();
					    if(Word.equals("class")
						   ||Word.equals("new")
						   ||Word.equals("extends")
						   ||Word.equals("implements")
						   ||Word.equals("interface")){
							wordIndex tmp=tryWordAfter(src,nowIndex+1);
							getBeforetype().add(src.substring(tmp.start,tmp.end));
							return tmp.end-1;
						}

					}
					return -1;
				}
			};
		}

		private char arr[]; 
		AnyThingForJava(){
			arr= new char[]{'!','~','+','-','*','/','%','^','|','&','<','>','='};
		    Arrays.sort(arr);
		}
	}
	
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
//	___________________________________________________________________________________________________________________________
	
	//CSS工厂
	final class AnyThingForCSS extends AnyThingForText
	{
		public DoAnyThing getCSSDrawer()
		{
			return new DoAnyThing(){

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					wordIndex node,node2;
					if (src.charAt(nowIndex) == '#')
					{
						node=Ep.get();
						node.set(nowIndex,nowIndex+1,Colors.color_fuhao);
						nodes.add(node);
						node2=tryWordAfterCSS(src, nowIndex + 1);
						node2.b=Colors.color_cssid;
						nodes.add(node2);
					    getHistoryVillber().add(src.substring(node2.start,node2.end));
						return node2.end - 1;

					}
					else if (src.charAt(nowIndex) == '.' && !String_Splitor.indexOfNumber(src.charAt(nowIndex - 1)) && !String_Splitor.indexOfNumber(src.charAt(nowIndex + 1)))
					{
						node=Ep.get();
						node.set(nowIndex,nowIndex+1,Colors.color_fuhao);
						nodes.add(node);
						node2=tryWordAfterCSS(src, nowIndex + 1);
						getBeforetype().add(src.substring(node2.start,node2.end));
						node2.b=Colors.color_csscla;
						nodes.add(node2);
						return node2.end - 1;
					}
					return -1;
				}
			};
		}

		public DoAnyThing getCSSChecker()
		{
			return new DoAnyThing(){

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					//为了节省时间，将更简单的条件放前面，触发&&的断言机制
					if(String_Splitor.IsAtoz(src.charAt(nowIndex + 1))&& src.charAt(tryLine_End(src, nowIndex) - 1) == '{'){
						if(getHistoryVillber().contains(nowWord.toString())){
							wordIndex node=Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_cssid);
							nodes.add(node);
							nowWord.delete(0,nowWord.length());
							return nowIndex;
						}
						else if(getBeforetype().contains(nowWord.toString())){
							wordIndex node=Ep.get();
							node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_csscla);
							nodes.add(node);
							nowWord.delete(0,nowWord.length());
							return nowIndex;
						}
					}

					return -1;
				}
			};
		}


		public DoAnyThing getNoSans_Tag()
		{
			//获取Tag
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{

					if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1))
						&& src.charAt(tryLine_End(src, nowIndex) - 1) == '{'
						&&(getTag().contains(nowWord.toString())))
					{
						//如果当前累计的字符串是一个Tag并且后面没有a～z和这些字符，就把它加进nodes
						wordIndex node=Ep.get();
						node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_tag);
						nodes.add(node);
						nowWord.delete(0,nowWord.length());
					    return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Attribute()
		{
			//获取属性
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					int afterIndex=tryAfterIndex(src, nowIndex + 1);

					if ( !String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && src.charAt(afterIndex) != '('&&getAttribute().contains(nowWord) )
					{
						//如果当前累计的字符串是一个属性并且后面没有a～z这些字符，就把它加进nodes
						wordIndex node=Ep.get();
						node.set(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_attr);
						nodes.add(node);
						nowWord.delete(0,nowWord.length());
					    return nowIndex;
					}
					return -1;
				}
			};
		}	
		public DoAnyThing getDraw_Attribute()
		{
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{
					wordIndex node;
					if (src.charAt(nowIndex) == '='
						|| src.charAt(nowIndex) == ':'
						){
						int i=src.indexOf('{',nowIndex);
						if (i<tryLine_End(src, nowIndex)&&i!=-1)
						{
							node=tryWordAfterCSS(src, nowIndex + 1);
							node.b=Colors.color_cssfl;
							getAttribute().add(src.substring(node.start, node.end));
							nodes.add(node);
							return node.end - 1;
						}
						else{
							node=tryWordForCSS(src, nowIndex - 1);
							node.b=Colors.color_attr;
							getAttribute().add(src.substring(node.start, node.end));
							nodes.add(node);
							return node.end - 1;
						}
					}
					return -1;
				}
			};
		}
		public wordIndex tryWordForCSS(String src, int index)
		{
			//试探前面的单词
			wordIndex tmp = Ep.get();
			try
			{
				while (Array_Splitor. indexOf(src.charAt(index), getFuhao()) != -1)
					index--;
				tmp.end=index + 1;
				while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
					index--;
				tmp.start=index + 1;
			}
			catch (Exception e)
			{
				return Ep.get();
			}
			return tmp;
		}

		public wordIndex tryWordAfterCSS(String src, int index)
		{
			//试探后面的单词
			wordIndex tmp = Ep.get();
			try
			{
				while (Array_Splitor.indexOf(src.charAt(index), getFuhao()) != -1)
					index++;
				tmp.start=index;
				while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
					index++;
				tmp.end=index;
			}
			catch (Exception e)
			{
				return Ep.get();
			}
			return tmp;
		}
	}

	class AnyThingForquick{
		//如果所有东西不需进行二次查找，就用这个吧
		public DoAnyThing getNoSans_Func(){
			//获取函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{

					if(src.charAt(nowIndex)=='('){
						//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
						wordIndex node = tryWord(src,nowIndex);
						node.b=Colors.color_func;
						nodes.add(node);
						wordIndex node2=Ep.get();
						node2.set(nowIndex,nowIndex+1,Colors.color_fuhao);
						nodes.add(node2);
						getLastfunc().add(src.substring(node.start,node.end));
						nowWord.delete(0,nowWord.length());
						return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Object(){
			//获取对象
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, List<wordIndex> nodes)
				{

					if(src.charAt(nowIndex)=='.'){
						//否则如果当前累计的字符串是一个对象并且后面是.字符，就把它加进nodes
						wordIndex node = tryWord(src,nowIndex);
						node.b=Colors.color_obj;
						nodes.add(node);
						wordIndex node2=Ep.get();
						node2.set(nowIndex,nowIndex+1,Colors.color_fuhao);
						nodes.add(node2);
						getThoseObject().add(src.substring(node.start,node.end));
						nowWord.delete(0,nowWord.length());
						return nowIndex;
					}
					return -1;
				}
			};
		}
	}
	
	
	
	
	
}
