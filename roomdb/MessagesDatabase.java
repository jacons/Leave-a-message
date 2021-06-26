package project.leaveamessage.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Message.class}, version  = 1,exportSchema = false)
public abstract class MessagesDatabase extends RoomDatabase {

    public abstract messageDao messageDao();
    private static MessagesDatabase INSTANCE;

    public static MessagesDatabase getDbInstance(Context context) {

        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MessagesDatabase.class, "levemsgDatabase")
                    .allowMainThreadQueries()
                    .build();

        }
        return INSTANCE;
    }
}