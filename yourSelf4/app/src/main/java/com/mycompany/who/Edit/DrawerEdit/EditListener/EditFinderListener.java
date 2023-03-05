package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,Words WordLib,OtherWords WordLib2);
	abstract public void OnFindNodes(ArrayList<DrawerBase.DoAnyThing> totalList,Words WordLib,OtherWords WordLib2);
	abstract public void OnClearFindWord(Words WordLib,OtherWords WordLib2);
	abstract public void OnClearFindNodes(int start,int end,String text,ArrayList<wordIndex> nodes);

}
