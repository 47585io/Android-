package com.mycompany.who.Edit.DrawerEdit.Base;

import android.text.*;
import android.text.style.*;

import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import android.widget.*;

public class Colors
{
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
		return null;
	}
	
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
	
	
	public static SpannableStringBuilder ForeColorText(String src,List<wordIndex> nodes){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes)
		    styled.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)), node.start, node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    return styled;	
	}
	public static void ForeColorText(Editable editor,List<wordIndex> nodes){
		for(wordIndex node:nodes)
		    editor.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)),node.start,node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	public static void ForeColorText(SpannableStringBuilder editor,List<wordIndex> nodes){
		for(wordIndex node:nodes)
		    editor.setSpan(new ForegroundColorSpan(fromByteToColor(node.b)),node.start,node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	public static Spanned ForeColorText(String text,String color) {
		//返回具有样式的Spanned
		//因效率问题，已禁用
		return Html.fromHtml("<font color='"+color+"'>"+text+"</font>",Html.FROM_HTML_OPTION_USE_CSS_COLORS);
	}
	
	public static SpannableStringBuilder BackColorText(String src,List<wordIndex> nodes){
		SpannableStringBuilder styled = new SpannableStringBuilder(src);
        //i未起始字符索引，j 为结束字符索引
		for(wordIndex node:nodes)
		    styled.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)), node.start, node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    return styled;	
	}
	public static void BackColorText(Editable editor,List<wordIndex> nodes){
		for(wordIndex node:nodes)
		    editor.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)),node.start,node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	public static void BackColorText(SpannableStringBuilder editor,List<wordIndex> nodes){
		for(wordIndex node:nodes)
		    editor.setSpan(new BackgroundColorSpan(fromByteToColor(node.b)),node.start,node.end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
}
