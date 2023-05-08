package com.mycompany.who.Edit.EditBuilder;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;

/* 
 如果你是EditBuilder的拥有者，

 需要管理监听器和单词，加载它们，切换它们
 */
public abstract interface EditBuilderUser extends EditListenerInfoUser,WordsUser
{
	public abstract void setEditBuilder(EditBuilder builder)
	
	public abstract EditBuilder getEditBuilder()
}
