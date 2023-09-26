package com.editor.text2.builder.listener;

import android.util.*;
import android.widget.*;
import com.editor.text2.builder.listener.baselistener.*;


public abstract class myEditRunnarListener extends myEditListener implements EditRunnarListener
{
	
	public static class Shell
	{
		private Runnar block;
		private Getter getter;
		
		public Shell(Runnar block,Getter getter){
			this.block=block;
			this.getter=getter;
		}

		public int Run(String com) throws Exception
		{
			//解析字符串，并把参数交给函数
			String[] args=getter.spiltArgs(com);
			if(args.length==0){
				return 0;
			}
			Object[] objs = getter.decodeArags(args);	
			return block.Run(objs);
		}

		public static abstract interface Getter
		{
			public abstract String[] spiltArgs(String com)

			public abstract Object[] decodeArags(String[] args)
		}

		public static abstract interface Runnar
		{
			public int Run(Object[] args) throws Exception
		}

	}

}
