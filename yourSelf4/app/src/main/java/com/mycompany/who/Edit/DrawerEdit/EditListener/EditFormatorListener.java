package com.mycompany.who.Edit.DrawerEdit.EditListener;

import android.text.*;

public abstract class EditFormatorListener extends EditListener
{
	public abstract int dothing_Run(Editable editor, int nowIndex);
	//开始做事
	public abstract int dothing_Start(Editable editor, int nowIndex,int start,int end);
	//为了避免繁琐的判断，一开始就调用start方法，将事情初始化为你想要的样子
	public abstract int dothing_End(Editable editor, int beforeIndex,int start,int end);
	//收尾工作
}
