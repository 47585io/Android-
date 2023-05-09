package com.mycompany.who.Edit.Base.Share.Share2;

import android.view.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Base.Share.Share1.*;


/*
  WordAdpter，智障的Adapter
  
  * 支持任意的xml文件，只要它拥有TextView和ImageView并设置了id
  
  * 支持让Icon来控制如何加载TextView和ImageView
 
  * 支持区间列表，可以给每一个区间的元素加上一个id
  
*/
public class WordAdpter extends BaseAdapter
{

	private Map<size,Integer> Range;
    private List<Icon> mfile;
	private int rid;

	public WordAdpter(int id) 
	{
        mfile = new ArrayList<>();
		Range = new HashMap<>();
		rid=id;	
    }
	public WordAdpter(List<Icon> file,int id,int flag)
	{
		mfile = new ArrayList<>();
		Range = new HashMap<>();
		rid=id;	
		addAll(file,flag);
	}
	public static WordAdpter getDefultAdapter()
	{
		return new WordAdpter(R.layout.WordIcon);
	}
	
	synchronized public void addAll(Collection<Icon> file,int flag)
	{
		if(file!=null){
			size range = new size(mfile.size(),mfile.size()+file.size());
		    Range.put(range,flag);
		    mfile.addAll(file);
		}
	}

    @Override
    public int getCount() 
	{
        return mfile == null ? 0 : mfile.size();
    }

    @Override
    public Icon getItem(int position)
	{
        return mfile.get(position);
    }

    @Override
    public long getItemId(int position) 
	{
        for(size pos:Range.keySet()){
			if(pos.start<=position && pos.end>position)
				return Range.get(pos);
		}
		return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
	{
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ViewHolder viewHolder = null;
        if (convertView == null) {
			//如果要创建一个新的列表项，创建并配置tag，之后在返回后ListView将tag拿出并添加到自己上
            convertView = layoutInflater.inflate(rid, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
			//否则，用Adpter中postion的值更新列表项tag，之后在返回后ListView将tag拿出刷新这个列表项
            viewHolder = (ViewHolder) convertView.getTag();
        }

		Icon icon = mfile.get(position);	
        icon.loadImage(viewHolder.tvIcon);
		icon.loadText(viewHolder.tvName);
        return convertView;
    }


    static class ViewHolder 
	{
        protected TextView tvName;
        protected ImageView tvIcon;

        ViewHolder(View rootView) {
            initView(rootView);
        }

        private void initView(View rootView) {
            tvName = (TextView) rootView.findViewById(R.id.Filename);
            tvIcon = (ImageView) rootView.findViewById(R.id.Fileicon);
        }
    }
}

