package project.leaveamessage.roomdb;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;


public class ViewModel extends AndroidViewModel {

    public final messageDao messageDao;
    public final LiveData<PagedList<Message>> messagesList;

    public ViewModel(@NonNull Application application) {

        super(application);
        MessagesDatabase messagesDatabase = MessagesDatabase.getDbInstance(this.getApplication());
        messageDao = messagesDatabase.messageDao();

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setPageSize(5)
                        .setInitialLoadSizeHint(10)
                        .setPrefetchDistance(8)
                        .setEnablePlaceholders(true)
                        .build();

        messagesList = (new LivePagedListBuilder(messageDao.getAllMessages(), pagedListConfig)).build();

    }
}