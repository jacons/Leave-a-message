package project.leaveamessage.roomdb;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface messageDao {

    @Query("SELECT * FROM messagestable")
    DataSource.Factory<Integer, Message> getAllMessages();

    @Query("SELECT * FROM messagestable WHERE idMessage=:id")
    Message getMessageById(int id);


    @Insert
    void  addMessage(Message... user);

}
