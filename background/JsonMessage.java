package project.leaveamessage.background;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import project.leaveamessage.roomdb.Message;

public class JsonMessage {
    private String id;
    private String user;
    private String name;
    private String surname;
    private String description;
    private String resource;
    private String type;
    private String latitude;
    private String longitude;
    private String distance;
    private String published;

    public int getId() { return Integer.parseInt(id); }

    public String getUser() { return user;  }

    public String getName() {  return name;  }

    public String getSurname() {  return surname;  }

    public String getDescription() { return description;  }

    public String getResource() { return resource;  }

    public String getType() { return type;  }

    public String getLatitude() { return latitude;  }

    public String getLongitude() { return longitude;  }

    public String getDistance() { return distance;  }

    public Double getNumDistance() { return Double.parseDouble(distance);  }


    // copy all informations into Message object (used for Room Database)
    public void inflateEntry(Message entry) {

        entry.idMessage = Integer.parseInt(this.id);
        entry.user = this.user;
        entry.name = this.name;
        entry.surname = this.surname;
        entry.description = this.description;
        entry.resource = this.resource;
        entry.type = this.type;
        entry.latitude = this.latitude;
        entry.longitude = this.longitude;
        entry.distance = this.distance;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        entry.discovered= dateFormat.format(Calendar.getInstance().getTime());

        entry.published = this.published;
    }

}
