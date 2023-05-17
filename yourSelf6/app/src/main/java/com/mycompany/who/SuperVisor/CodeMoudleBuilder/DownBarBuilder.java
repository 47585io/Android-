package com.mycompany.who.SuperVisor.CodeMoudleBuilder;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;

public abstract class DownBarBuilder implements DownBarBuilderInterface
{

	@Override
	public void ConfigSelf(DownBar target)
	{
		PageList pages = (PageList) target.getVector();
		if(pages==null){
			pages = new PageList(target.getContext());
			target.setVector(pages);
		}
		loadPages(pages);
	}
	
	public abstract void loadPages(PageList pages)

}
