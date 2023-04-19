package com.mycompany.who.Edit.Base.Share;

import java.io.*;
import com.mycompany.who.R;

public class Share
{

	public static int setbitTo_0S(int src,byte...indexs){
		for(byte index:indexs){
			src=setbitTo_0(src,index);
		}
		return src;
	}
	public static int setbitTo_1S(int src,byte...indexs){
		for(byte index:indexs){
			src=setbitTo_1(src,index);
		}
		return src;
	}

	public static int setbitTo_1(int x,byte y){
		return  x|=(1<<y);
	}
	public static int setbitTo_0(int x,byte y){
		return x&=~(1<<y);
	}
	public static boolean getbit(int x,int y){
		int tmp= ((x) >> (y)&1);
		if(tmp==0)
			return false;
		else
			return true;
	}

	public final static byte icon_key=1;
	public final static byte icon_villber=2;
	public final static byte icon_func=3;
	public final static byte icon_type=4;
	public final static byte icon_tag=5;
	public final static byte icon_obj=8;
	public final static byte icon_default=-128;


	public static int getWordIcon(byte flag){
		switch(flag){
				//关键字，变量，函数，类型，标签，默认
			case 1:
				return R.drawable.image_1;	
			case 2:
				return R.drawable.image_2;

			case 3:
				return R.drawable.image_3;
			case 4:
				return R.drawable.image_4;

			case 5:
				return R.drawable.image_5;
			case 8:
				return R.drawable.image_8;

			default:
			    return R.drawable.image_n;

		}
	}

}
