package com.example.danut.touristicagenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ImageViewHolder> {

    private final Context eventContext;
    private final List<Events> eventUploads;
    private OnItemClickListener clickListener;

    public EventAdapter(Context event_context, List<Events> event_uploads) {
        eventContext = event_context;
        eventUploads = event_uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(eventContext).inflate(R.layout.image_event, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {

        Events uploadCurrent = eventUploads.get(position);
        holder.tvDateEv.setText(uploadCurrent.getEvent_Date());
        holder.tvNameEv.setText(uploadCurrent.getEvent_Name());
        holder.tvPlaceEv.setText(uploadCurrent.getEvent_Place());
        holder.tvMessageEv.setText(uploadCurrent.getEvent_Message());
        Picasso.get()
                .load(uploadCurrent.getEvent_Image())
                .placeholder(R.drawable.image_app_icon)
                .fit()
                .centerCrop()
                .into(holder.imageEv);
    }

    @Override
    public int getItemCount() {
        return eventUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvDateEv;
        public ImageView imageEv;
        public TextView tvNameEv;
        public TextView tvPlaceEv;
        public TextView tvMessageEv;

        public ImageViewHolder(View itemView) {
            super(itemView);

            tvDateEv = itemView.findViewById(R.id.tvDateEvent);
            imageEv = itemView.findViewById(R.id.imageEvent);
            tvNameEv = itemView.findViewById(R.id.tvNameEvent);
            tvPlaceEv = itemView.findViewById(R.id.tvPlaceEvent);
            tvMessageEv = itemView.findViewById(R.id.tvMessageEvent);

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

    public void setOnItmClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }
}
