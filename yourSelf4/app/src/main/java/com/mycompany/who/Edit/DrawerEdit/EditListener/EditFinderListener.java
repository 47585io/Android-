package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector);
	abstract public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector);
	abstract public void OnClearFindWord(TreeSet<String> vector);
	abstract public void OnClearDrawWord(int start,int end,String text,ArrayList<wordIndex> nodes);
}
