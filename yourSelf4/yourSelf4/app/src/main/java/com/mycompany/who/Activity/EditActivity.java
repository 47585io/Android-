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
import android.text.*;
import android.graphics.*;

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
		String text="";
		class f extends DrawerBase.DoAnyThing{

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					nodes.add(new wordIndex(0,src.length(),Colors.color_attr));
					return src.length();
				}
			}

		@Override
		public EditListener getFinder()
		{
			
			return new EditFinderListener(){

				@Override
				public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
				{
					
					totalList.add(new DrawerBase.DoAnyThing(){

							@Override
							public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
							{
								if(nowWord.toString().equals(" <style"))
									text="OK";
								return 0;
							}
						});
					
				}

				@Override
				public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
				{
					totalList.add(new f());
					
				}

				@Override
				public void OnClearFindWord(TreeSet<String> vector)
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
			
			return new EditDrawerListener(){

				@Override
				public void onDraw(int start, int end, ArrayList<wordIndex> nodes,Editable editor)
				{
					editor.append("Test1");
					
				}
			};
		}

		@Override
		public EditListener getFormator()
		{
			
			return new EditFormatorListener(){

				@Override
				public int dothing_Run(Editable editor, int nowIndex)
				{
					editor.append("Test2");
					return nowIndex+=10;
				}

				@Override
				public int dothing_Start(Editable editor, int nowIndex)
				{
					// TODO: Implement this method
					return 0;
				}

				@Override
				public int dothing_End(Editable editor, int beforeIndex)
				{
					// TODO: Implement this method
					return 0;
				}
			};
			
			
		}

		@Override
		public EditListener getInsertor()
		{
			
			return new EditInsertorListener(){

				@Override
				public int dothing_insert(Editable editor, int nowIndex)
				{
					editor.append("Test3");
					return 0;
				}
			};
			
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
			return new EditCanvaserListener(){

				@Override
				public void onDraw(Canvas canvas, TextPaint paint, Rect bounds,wordIndex historyPos)
				{
					paint.setColor(0xffffffff);
					canvas.drawRect(historyPos.start,historyPos.end,historyPos.start+100,historyPos.end+100,paint);
					
				//test
				}
			};
		}
	}
	
	

}
