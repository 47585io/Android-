package com.mycompany.who.SuperVisor.CodeMoudleBuilder;

import com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import android.widget.*;
import android.view.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import android.widget.AdapterView.*;

public abstract class TitleBuilder extends senderObject implements TitleBuilderInterface
{

	@Override
	public void ConfigSelf(Title target)
	{
		ReSpinner EditList = target.getReSpinner();
		ReSpinner More = target.getMore();
		LinearLayout ButtonBar = target.getButtonBar();
		
		EditList.setOnItemSelectedListener(this);
		loadButton(ButtonBar);
		onMenuCreat(More);
		More.setOnItemSelectedListener(new onMenuSelected());
	}
	
	class onMenuSelected implements OnItemSelectedListener
	{

		@Override
		public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
		{
			onMenuItemSelected(p1,p3);
		}

		@Override
		public void onNothingSelected(AdapterView<?> p1)
		{
			// TODO: Implement this method
		}
	}
	
}
