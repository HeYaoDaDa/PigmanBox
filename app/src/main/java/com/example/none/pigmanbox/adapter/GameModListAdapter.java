package com.example.none.pigmanbox.adapter;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.modle.Mod;

import java.util.List;

/**
 * 游戏mod列表的adapter
 */
public class GameModListAdapter extends RecyclerView.Adapter<GameModListAdapter.ViewHolder> {
    private List<Mod> installModList;
    private List<Mod> planAddModList;
    private List<Mod> planDeleteModList;

    private Runnable mRunnable;


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        ImageView icon;
        TextView name;
        TextView author;
        TextView description;
        Button button;
        View modView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.ig_CarDview);
            icon = itemView.findViewById(R.id.ig_ImageView);
            name = itemView.findViewById(R.id.ig_name);
            author = itemView.findViewById(R.id.ig_author);
            description = itemView.findViewById(R.id.ig_description);
            button = itemView.findViewById(R.id.ig_button);
            modView = itemView;
        }
    }

    public GameModListAdapter(List<Mod> inatallModList, List<Mod> planAddModList, List<Mod> planDeleteModList, Runnable runnable) {
        this.installModList = inatallModList;
        this.planAddModList = planAddModList;
        this.planDeleteModList = planDeleteModList;
        this.mRunnable = runnable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gamemodlist, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        int stats = 0;//0为默认直接存在在游戏里的；1为计划添加到游戏里的；2为计划删除的。
        final Mod mod;
        if (i < planAddModList.size()) {
            mod = planAddModList.get(i);
            stats = 1;
        } else {
            mod = installModList.get(i - planAddModList.size());

            if (planDeleteModList.contains(mod)) {//如果mod存在于计划删除列表中
                stats = 2;
            }
        }
        if (mod == null)
            return;
//        String iconUrl = ModUtil.getModIconUrl(mod);
//        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.placeholder(R.mipmap.modicon_placeholder);
//        Glide.with(MyApplication.getContext()).load(iconUrl).apply(requestOptions).into(viewHolder.icon);
        viewHolder.name.setText(mod.getName());
        viewHolder.author.setText(mod.getAuthor());
        viewHolder.description.setText(mod.getDescription());
        viewHolder.button.setText("卸载");
        viewHolder.mCardView.setCardBackgroundColor(Color.WHITE);
        switch (stats) {
            case 0:
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        planDeleteModList.add(mod);
                        notifyItemChanged(i);//刷新一个条目的内容
                        mRunnable.run();//回调按钮被点击
                    }
                });
                break;
            case 1:
                viewHolder.mCardView.setCardBackgroundColor(Color.GREEN);
                viewHolder.button.setText("取消");
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        planAddModList.remove(mod);
                        notifyItemRemoved(i);//刷新删除一个条目
                        mRunnable.run();//回调按钮被点击
                    }
                });
                break;
            case 2:

                viewHolder.mCardView.setCardBackgroundColor(Color.RED);
                viewHolder.button.setText("取消");
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        planDeleteModList.remove(mod);
                        notifyItemChanged(i);//刷新一个条目的内容
                        mRunnable.run();//回调按钮被点击
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (installModList == null)
            return 0;
        return installModList.size() + planAddModList.size();
    }
}
