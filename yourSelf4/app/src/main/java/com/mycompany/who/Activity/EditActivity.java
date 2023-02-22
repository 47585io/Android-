package com.mycompany.who.Activity;


import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.SuperVisor.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.DrawerBase.*;
import com.mycompany.who.Edit.DrawerEdit.*;

public class EditActivity extends BaseActivity3
{

	protected EditList files;
	protected XCode Code;

	@Override
	public void TouchCollector()
	{
		setOnTouchListenrS(Code);
		super.TouchCollector();
	}

	@Override
	protected void initActivity()
	{
		super.initActivity();
		Code = new XCode(this);
		files=Code.getEditList();
		EditFather.addView(Code,0);
	}

	@Override
	public void configActivity()
	{
		super.configActivity();
		Code.setPool(getPool());
		Code.configWallpaper("/storage/emulated/0/DCIM/bili/f3.jpg",40);
		Code.addAExtension(new As());
	}

	protected ThreadPoolExecutor getPool()
	{
		return null;
	}


	class As extends XCode.Extension
	{
		
		class f extends DrawerBase.DoAnyThing{

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					//nodes.add(new wordIndex(0,src.length(),Colors.color_attr));
					return src.length();
				}
			}

		@Override
		public EditListener getFinder()
		{
			
			return new EditFinderListener(){

				@Override
				public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList)
				{
					// TODO: Implement this method
				}

				@Override
				public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList)
				{
					totalList.add(new f());
				}

				@Override
				public void OnClearFindWord(Words words, OtherWords twords)
				{
					// TODO: Implement this method
				}

				@Override
				public void OnClearDrawWord(int start, int end, String text, ArrayList<wordIndex> nodes)
				{
					// TODO: Implement this method
				}
			};
		}

		@Override
		public EditListener getDrawer()
		{
			
			return null;
		}

		@Override
		public EditListener getFormator()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getInsertor()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getCompletor()
		{
			
			return new EditCompletorListener(){

				@Override
				public void onBeforeSearchWord(ArrayList<Collection<String>> libs1, ArrayList<String[]> libs2)
				{
					// TODO: Implement this method
				}

				@Override
				public void onFinishSearchWord(ArrayList<ArrayList<String>> words1, ArrayList<ArrayList<String>> words2, ArrayList<Icon> adpter)
				{
					adpter.add(new Icon(R.drawable.file_type_pic,"hello world!"));
				}
			};
		}

		@Override
		public EditListener getCanvaser()
		{
			// TODO: Implement this method
			return null;
		}
	}
	
	

}
