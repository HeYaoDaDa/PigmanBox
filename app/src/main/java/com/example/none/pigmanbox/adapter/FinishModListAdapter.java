package com.example.none.pigmanbox.adapter;


import android.annotation.SuppressLint;
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
import com.example.none.pigmanbox.util.ModUtils;

import java.util.List;

/**
 * FinishModListFragment adapter
 */
public class FinishModListAdapter extends RecyclerView.Adapter<FinishModListAdapter.ViewHolder> {
    private List<Mod> modList;

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

    public FinishModListAdapter(List<Mod> modList) {
        this.modList = modList;
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
        final Mod mod = modList.get(i);
        if (mod == null)
            return;
//        String iconUrl = ModUtil.getModIconUrl(mod);
//        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.placeholder(R.mipmap.modicon_placeholder);
//        Glide.with(MyApplication.getContext()).load(iconUrl).apply(requestOptions).into(viewHolder.icon);
        viewHolder.name.setText(mod.getName());
        viewHolder.author.setText(mod.getAuthor());
        viewHolder.description.setText(mod.getDescription());
        viewHolder.button.setText(R.string.uninstall);
        viewHolder.button.setOnClickListener(v -> {
            if (ModUtils.removeFinishMod(mod.getId())) {
                ModUtils.mods[mod.getId()] = null;
                modList.remove(mod);
            }
            notifyItemRemoved(i);
        });
    }

    @Override
    public int getItemCount() {
        return modList.size();
    }

}
