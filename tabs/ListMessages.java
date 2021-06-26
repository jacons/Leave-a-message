package project.leaveamessage.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import project.leaveamessage.R;
import project.leaveamessage.activities.ViewSingeMessage;
import project.leaveamessage.adapter.MessageAdapter;
import project.leaveamessage.interfaces.OnReviewMessageListener;
import project.leaveamessage.roomdb.ViewModel;

public class ListMessages extends Fragment implements OnReviewMessageListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_listmsg, container, false);

        Activity activity = getActivity();
        if (activity==null) return rootView;

        RecyclerView rvMessage = rootView.findViewById(R.id.reycleviewMessage);
        rvMessage.setHasFixedSize(true);
        rvMessage.setLayoutManager(new LinearLayoutManager(getContext()));


        ViewModel viewModel = ViewModelProviders.of(this).get(ViewModel.class);

        MessageAdapter messageAdapter = new MessageAdapter(this);

        viewModel.messagesList.observe((LifecycleOwner) activity, messageAdapter::submitList);
        rvMessage.setAdapter(messageAdapter);

        return rootView;
    }
    @Override
    public void OnReviewClick(int id) {
        Intent i = new Intent(getContext(), ViewSingeMessage.class);
        i.putExtra("idMessage",id);
        startActivity(i);
    }
}