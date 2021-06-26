package project.leaveamessage.roomdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messagestable")
public class Message {

    @PrimaryKey
    @ColumnInfo(name = "idMessage")
    public int idMessage;

    @ColumnInfo(name = "userId")
    public String user;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "surname")
    public String surname;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "resource")
    public String resource;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "latitude")
    public String latitude;

    @ColumnInfo(name = "longitude")
    public String longitude;

    @ColumnInfo(name = "distance")
    public String distance;

    @ColumnInfo(name = "discovered")
    public String discovered;

    @ColumnInfo(name = "published")
    public String published;

}
