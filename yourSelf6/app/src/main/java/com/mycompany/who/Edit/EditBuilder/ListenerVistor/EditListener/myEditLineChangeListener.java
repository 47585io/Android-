package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.util.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/* 
  行数监听器
  
  此监听器多半用于EditLine中，测量行数时用于更新一些数据
  
  start表示原本的行数，before表示删除的行，after表示增加的行
  
*/
public abstract class myEditLineChangeListener extends myEditListener implements EditLineCheckerListener
{	
	@Override
	public abstract void onLineChanged(int start,int before,int after)
}
