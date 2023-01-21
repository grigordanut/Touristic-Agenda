package com.example.danut.touristicagenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ImageViewHolder>{

    private final Context eventContext;
    private final List<Events> eventUploads;
    private OnItemClickListener clickListener;

    public EventsAdapter(Context event_context, List<Events> event_uploads){
        eventContext = event_context;
        eventUploads = event_uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(eventContext).inflate(R.layout.image_event,parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {

        Events uploadCurrent = eventUploads.get(position);
        holder.tvEventDate.setText(uploadCurrent.getEvent_Date());
        holder.tvEventName.setText(uploadCurrent.getEvent_Name());
        holder.tvEventAddress.setText(uploadCurrent.getEvent_Address());
        holder.tvEventMessage.setText(uploadCurrent.getEvent_Message());
        Picasso.get()
                .load(uploadCurrent.getEvent_Image())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageEvent);
    }

    @Override
    public int getItemCount() {
        return eventUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvEventDate;
        public ImageView imageEvent;
        public TextView tvEventName;
        public TextView tvEventAddress;
        public TextView tvEventMessage;

        public ImageViewHolder(View itemView) {
            super(itemView);

            tvEventDate = itemView.findViewById(R.id.tvDate);
            imageEvent = itemView.findViewById(R.id.imgShowEvent);
            tvEventName = itemView.findViewById(R.id.tvName);
            tvEventAddress = itemView.findViewById(R.id.tvAddress);
            tvEventMessage = itemView.findViewById(R.id.tvComments);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(position);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItmClickListener(OnItemClickListener listener){
        clickListener = listener;
    }
}
