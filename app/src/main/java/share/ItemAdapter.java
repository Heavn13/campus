package share;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heavn.student.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Administrator on 2017/7/19 0019.
 * 适配器
 */

public class ItemAdapter extends ArrayAdapter<Content>{
    private int resourceId;
//    点赞图片
    private ImageView image_good;
//    评论图片
    private ImageView image_comment;
//    评论内容数组
    private List<ContentComment> comments = new ArrayList<>();
//    评论适配器
    private CommentItemAdapter commentItemAdapter = null;
    //自定义listView
    private MyListview listView;
//    评论编辑text按钮
    private TextView edit_comment;
//    点赞人数显示
    private TextView well_done;
//    弹出窗口编辑框发送内容
    private Button pop_send;
//    弹出窗口编辑框
    private EditText pop_edit;
//    内容的单项
    private Content item;
//    单项内容的objectId
    private String id;
//    单项内容
    private String s_content;
//    评论内容
    private String s_comment;
//    弹出窗口
    private PopupWindow popupWindow;
//    点赞次数
    private Integer c;
    private int width,height;

    private ContentComment contentComment;

    public ItemAdapter(Context context, int textViewResourceId, List<Content> obejects){
        super(context,textViewResourceId,obejects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);

        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        //刷新管理器
        NotificationManager manager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);

        TextView textView1 = (TextView)view.findViewById(R.id.nickname);
        textView1.setText(item.getNickname());
        TextView textView2 = (TextView)view.findViewById(R.id.content);
        textView2.setText(item.getContent());
        TextView textView3 = (TextView)view.findViewById(R.id.location);
        textView3.setText(item.getLocation());
        TextView textView4 = (TextView)view.findViewById(R.id.date);
        textView4.setText(item.getDate());

//        onClickListener传数据需写在adapter里
        image_good = (ImageView)view.findViewById(R.id.image_good);
        image_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = getItem(position);
                id = item.getId();
                //单个item获赞次数累加
                c = item.getCount();
                Log.e("item",""+c);
                c++;
                Log.e("item",""+id);
                item.setCount(c);
                //更新保存获赞次数
                Content content = new Content();
                content.increment("count");
                content.update(id, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                    }
                });
                //listview数据更新
                notifyDataSetChanged();
            }
        });

        image_comment = (ImageView)view.findViewById(R.id.image_comment);
        image_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = getItem(position);
                id = item.getId();
                s_content = item.getContent();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    initPopupWindowView(item);
                    popupWindow.showAsDropDown(v, Gravity.BOTTOM, 0, 0);
                }
            }
        });


        edit_comment = (TextView) view.findViewById(R.id.edit_comment);
        edit_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = getItem(position);
                id = item.getId();
                s_content = item.getContent();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    initPopupWindowView(item);
                    popupWindow.showAsDropDown(v, Gravity.BOTTOM, width-200, height-200);
                }
            }
        });

        well_done = (TextView) view.findViewById(R.id.well_done);
        //初始化点赞
        if(item.getCount() != 0){
            well_done.setText(item.getCount()+"人觉得很赞");
        }

        listView = (MyListview)view.findViewById(R.id.comment_lists);
//        初始化评论
        if(item.getComment() != null){
            comments =  item.getComment();
            commentItemAdapter = new CommentItemAdapter(getContext(),R.layout.comment_item_layout,comments);
            listView.setAdapter(commentItemAdapter);
            setListViewHeightBasedOnChildren(listView);
            notifyDataSetChanged();
        }

        return view;
    }


    //    弹出窗口
    public void initPopupWindowView(final Content item) {
        // // 获取自定义布局文件pop.xml的视图
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.edit_comment_layout, null, false);
        // 创建PopupWindow实例,让它能够适应键盘的出现和消失,注意LinearLayout.LayoutParams.FILL_PARENT,130才能达到该效果
        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT,130,true);
        // 自定义view添加触摸事件，设置点击其他区域弹窗消失
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });

        pop_edit = (EditText)customView.findViewById(R.id.pop_edit);
        //打开软键盘
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        // 获取编辑框焦点
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(false);
        //软键盘不会挡着popupwindow
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //设置菜单显示的位置
        pop_edit.setFocusable(true);
        pop_edit.requestFocus();

        pop_send = (Button)customView.findViewById(R.id.pop_send);
//        发表内容
        pop_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_comment = pop_edit.getText().toString();
                if( !s_comment.equals("")){
                    final ContentComment contentComment = new ContentComment(ShareMainActivity.s_nickname,s_comment,item.getNickname());
//                    不能new出一个新的对象再添加，否则会出现点赞数重新变为0的情况
                    item.add("comment",contentComment);
                    item.update(item.getId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e == null){
                                Toast.makeText(getContext(),"发表成功",Toast.LENGTH_SHORT).show();
//                                必须使用item.getComment()方法获取到的comments对象添加，使其显示在对应的item中
//                                 如果直接使用comments添加对象，会出现内容更新后只会显示在最后一个item上的情况
                                item.getComment().add(contentComment);
                                commentItemAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    popupWindow.dismiss();
                }else{
                    Toast.makeText(getContext(),"请填写内容后发表",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //计算listView的定高
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
//        Log.e("height",""+comment_lists.size());
        for (int i = 0; i < comments.size(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
//            Log.e("height",""+listItem.getMeasuredHeight());
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (comments.size()+1));
        listView.setLayoutParams(params);
    }

}


