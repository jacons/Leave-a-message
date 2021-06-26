package project.leaveamessage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import project.leaveamessage.R;
import project.leaveamessage.interfaces.OnReviewMessageListener;
import project.leaveamessage.roomdb.Message;
/**
 * for paging example
 * https://github.com/arunk7839/PagingExampleUsingRoom
 */
public class MessageAdapter extends PagedListAdapter<Message, MessageAdapter.ViewHolder> {

    private final OnReviewMessageListener listener;

    public MessageAdapter(OnReviewMessageListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // not showed, only used for hold unique message id
        private int idMessage;
        public final TextView userName;
        public final TextView description;
        public final TextView date;
        private final OnReviewMessageListener listener;

        public ViewHolder(@NonNull View itemView, OnReviewMessageListener listener) {
            super(itemView);
            this.listener = listener;
            userName    =  itemView.findViewById(R.id.nameMessage);
            description =  itemView.findViewById(R.id.description);
            date        =  itemView.findViewById(R.id.discovered);

            itemView.findViewById(R.id.moredetails).setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            listener.OnReviewClick(idMessage);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.messageitem,parent,false);
        return new MessageAdapter.ViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = getItem(position);
        if(message!=null) {
            holder.idMessage = message.idMessage;
            holder.userName.setText(message.name+" "+message.surname);
            holder.date.setText(message.discovered);
            if(message.description.length()>50)
                holder.description.setText(message.description.substring(0,50)+"...");
            else
                holder.description.setText(message.description);
        }
    }
    // https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
    public static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.idMessage == newItem.idMessage;
        }
        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.idMessage == newItem.idMessage;
        }
    };
}