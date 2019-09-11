package csye6225.cloud.noteapp.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.persistence.*;

@Entity
public class Attachment {

    @Id
    @Column(name = "attachment_id", unique = true)
    private String attachment_id;

    @Column(name = "path")
    private String path;

    public String getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(String attachment_id) {
        this.attachment_id = attachment_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
