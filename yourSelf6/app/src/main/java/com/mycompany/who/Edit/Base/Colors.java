package com.mycompany.who.Edit.Base;

import android.graphics.*;
import android.text.*;
import android.text.style.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import java.util.*;


/*
 EditText管理文本数据，也管理文本绘制，文本数据由其内部SpannableStringBuilder容器管理，而文本绘制是用TextPaint画的
 
 EditText在onDraw中可以将画笔分享给别人，这个人就是Span，所以自定义Span可以将文本绘制成任何样子

 每次onDraw，EditText遍历每个Span，并调用它们的方法，至于如何绘制这段文本由Span自己决定
 
 (另外的，文本起始和末尾的位置是由EditText内部的Layout测量后决定的)

 您只要使用支持设置Span的文本容器的setSpan方法就可以设置一个Span
 
 setSpan 内部会判断 Span 对象是否之前设置过了，如果是同一个对象，会修改它的位置和flag值

 但单个Span可以应用到多个Span容器中，这样可以节省内存

 */
public class Colors
{
	//所有颜色
	public static int Default =0xffabb2bf;//灰白
	public static int zhuShi =0xff585f65;//深灰
	public static int Str=0xff98c379;//青草奶油
	public static int FuHao =0xff99c8ea;//蓝
	public static int Number=0xffff9090;//橙红柚子
	public static int KeyWord=0xffcc80a9;//桃红乌龙
	public static int Const =0xffcd9861;//枯叶黄
	public static int Villber =0xffff9090;
	public static int Function =0xff99c8ea;
	public static int Type=0xffd4b876;
	public static int Object =0xffd4b876;//金币
	public static int Attribute=0xffcd9861;//枣
	public static int Tag=0xffde6868;
	public static int CSS_id=0xff99c8ea;
	public static int CSS_class=0xffd4b876;
	public static int CSS_felement=0xff57adbb;//淡湖绿
	public static int Bg = 0xff222222;
	public static int Bg2 = 0xff1e2126;
	
	public static String Default_ ="#abb2bf";//灰白
	public static String zhuShi_ ="#585f65";//深灰
	public static String Str_="#98c379";//青草奶油
	public static String FuHao_ ="#99c8ea";//蓝
	public static String Number_="#ff9090";//橙红柚子
	public static String KeyWord_="#cc80a9";//桃红乌龙
	public static String Const_ ="#cd9861";//枯叶黄
	public static String Villber_ ="#ff9090";
	public static String Function_ ="#99c8ea";
	public static String Type_="#d4b876";
	public static String Attribute_="#cd9861";//枣
	public static String Tag_="#de6868";
	public static String Object_ ="#d4b876";//金币
	public static String CSS_id_="#99c8ea";
	public static String CSS_class_="#d4b876";
	public static String CSS_felement_="#57adbb";//淡湖绿
	public static String Bg_ = "#222222";
	public static String Bg2_ = "#1e2126";
	
	public final static byte color_white=0;
	public final static byte color_zhu=1;
	public final static byte color_str=2;
	public final static byte color_fuhao=3;
	public final static byte color_number=4;
	public final static byte color_key=5;
	public final static byte color_const=6;
	public final static byte color_villber=7;
	public final static byte color_func=8;
	public final static byte color_type=9;
	public final static byte color_obj=10;
	public final static byte color_tag=11;
	public final static byte color_attr=12;
	public final static byte color_cssid=13;
	public final static byte color_csscla=14;
	public final static byte color_cssfl=15;
	
	public static int fromByteToColor(byte b){
		switch(b){
			case 0: return Default;
			case 1: return zhuShi;
			case 2: return Str;
			case 3: return FuHao;
			case 4: return Number;
			case 5: return KeyWord;
			case 6: return Const;
			case 7: return Villber;
			case 8: return Function;
			case 9: return Type;
			case 10:return Object;
			case 11: return Tag;
			case 12: return Attribute;		
			case 13:return CSS_id;
			case 14:return CSS_class;
			case 15:return CSS_felement;
		}
		return 0;
	}

	public static String fromByteToColorS(byte b){
		switch(b){
			case 0: return Default_;
			case 1: return zhuShi_;
			case 2: return Str_;
			case 3: return FuHao_;
			case 4: return Number_;
			case 5: return KeyWord_;
			case 6: return Const_;
			case 7: return Villber_;
			case 8: return Function_;
			case 9: return Type_;
			case 10:return Object_;
			case 11: return Tag_;
			case 12: return Attribute_;
			case 13:return CSS_id_;
			case 14:return CSS_class_;
			case 15:return CSS_felement_;
		}
		return "";
	}
	
	public static int vualeOf(String color){
		return Integer.parseInt(color,16);
	}
	public static String toString(int color){
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
	
	/* 自定义您的颜色  */
	public static interface ByteToColor{

		public int fromByteToColor(byte b)

		public String fromByteToColorS(byte b)

	}
	
	public static interface ByteToColor2 extends ByteToColor{
		
		public int getDefult()

		public String getDefultS();
		
		public int getDefultBg()
		
		public String getDefultBgS()
		
	}
	
	public static class myColor implements ByteToColor
	{

		@Override
		public int fromByteToColor(byte b)
		{
			return Colors. fromByteToColor(b);
		}

		@Override
		public String fromByteToColorS(byte b)
		{
			return Colors. fromByteToColorS(b);
		}
		
	}
	
	public static class myColor2 extends myColor implements ByteToColor2
	{

		@Override
		public int getDefult()
		{
			return Default;
		}
		
		@Override
		public String getDefultS()
		{
			return Default_;
		}
		
		@Override
		public int getDefultBg()
		{
			return Bg;
		}

		@Override
		public String getDefultBgS()
		{
			return Colors.toString(Bg);
		}
	}

	public static SpannableStringBuilder ForeColorText(CharSequence src,ByteToColor Colors,wordIndex... nodes){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes){
			if(Colors==null)
		        styled.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)), node.start, node.end,SpanFlag);
			else
				styled.setSpan(new ForegroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		}
	    return styled;	
	}
	public static void ForeColorText(Spannable editor,int start,ByteToColor Colors,wordIndex... nodes){
		for(wordIndex node:nodes){
			if(Colors==null)
		        editor.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
			else
				editor.setSpan(new ForegroundColorSpan(Colors. fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
		}
	}
	public static SpannableStringBuilder ForeColorText(CharSequence src,List<wordIndex> nodes,ByteToColor Colors){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes){
			if(Colors==null)
		        styled.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)), node.start, node.end,SpanFlag);
			else
				styled.setSpan(new ForegroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		}
	    return styled;	
	}
	public static void ForeColorText(Spannable editor,List<wordIndex> nodes,int start,ByteToColor Colors){
		for(wordIndex node:nodes){
			if(Colors==null)
		        editor.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
			else
				editor.setSpan(new ForegroundColorSpan(Colors. fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
		}
	}
	public static Spanned ForeColorText(CharSequence text,String color) {
		//返回具有样式的Spanned
		//因效率问题，已禁用
		return Html.fromHtml("<font color='"+color+"'>"+text+"</font>",Html.FROM_HTML_OPTION_USE_CSS_COLORS);
	}
	public static SpannableStringBuilder ForeColorText(CharSequence text,int color){
		SpannableStringBuilder b = new SpannableStringBuilder(text);
		b.setSpan(new ForegroundColorSpan(color),0,text.length(),SpanFlag);
		return b;
	}
	
	public static SpannableStringBuilder BackColorText(CharSequence src,ByteToColor Colors,wordIndex... nodes){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes){
			if(Colors==null)
		        styled.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		    else
				styled.setSpan(new BackgroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		}
	    return styled;	
	}
	public static void BackColorText(Spannable editor,int start,ByteToColor Colors,wordIndex... nodes){
		for(wordIndex node:nodes){
			if(Colors==null)
		        editor.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
			else
				editor.setSpan(new BackgroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
	    }
	}
	public static SpannableStringBuilder BackColorText(CharSequence src,List<wordIndex> nodes,ByteToColor Colors){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes){
			if(Colors==null)
		        styled.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		    else
				styled.setSpan(new BackgroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
		}
	    return styled;	
	}
	public static void BackColorText(Spannable editor,List<wordIndex> nodes,int start,ByteToColor Colors){
		for(wordIndex node:nodes){
			if(Colors==null)
		        editor.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)),node.start+start,node.end+start,SpanFlag);
			else
				editor.setSpan(new BackgroundColorSpan(Colors. fromByteToColor(node.b)), node.start, node.end,SpanFlag);
	    }
	}
	public static SpannableStringBuilder BackColorText(CharSequence text,int color){
		SpannableStringBuilder b = new SpannableStringBuilder(text);
		b.setSpan(new BackgroundColorSpan(color),0,text.length(),SpanFlag);
		return b;
	}
	
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
	protected static String Replace(String src){
		src=src.replaceAll("<","&lt;");
		src=src.replaceAll(">","&gt;");
		src=src.replaceAll("\t","    ");
		src=src.replaceAll(" ","&nbsp;");
		src=src.replaceAll("\n","<br/>");
		//替换被HTML解析的字符
		return src;
	}
	
	
	public static SpannableStringBuilder setSpans(size[] nodes,Object[] spans,CharSequence src){
		SpannableStringBuilder builder = new SpannableStringBuilder(src);
		int i;
		for(i=0;i<nodes.length;++i){
			Object span = spans[i];
			size node = nodes[i];
			builder.setSpan(span,node.start,node.end,SpanFlag);
		}
		return builder;
	}
	public static void setSpans(size[] nodes,Object[] spans,Spannable editor){
		int i;
		for(i=0;i<nodes.length;++i){
			Object span = spans[i];
			size node = nodes[i];
			editor.setSpan(span,node.start,node.end,SpanFlag);
		}
	}
	
	public static<T> size[] subSpanPos(int start,int end,Spanned editor,Class<T> type){
		Object[] spans = editor.getSpans(start,end,type);
		size[] nodes = new size[spans.length];
		
		for(int i=0;i<spans.length;++i ){
			size s = new size();
			Object span = spans[i];
			s.start= editor.getSpanStart(span);
			s.end = editor.getSpanEnd(span);
			nodes[i] = s;
		}
		return nodes;
	}
	public static<T> void clearSpan(int start,int end,Spannable editor,Class<T> type){
		Object[] spans = editor.getSpans(start,end,type);
		for(Object span: spans)
		    editor.removeSpan(span);
	}
	
	
	public static Class<ParcelableSpan> SpanType = ParcelableSpan.class;
	
	public static Class<BackgroundColorSpan> BackSpanType = BackgroundColorSpan.class;
	
	public static Class<ForegroundColorSpan> ForeSpanType = ForegroundColorSpan.class;
	
	public static int SpanFlag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
	
}
