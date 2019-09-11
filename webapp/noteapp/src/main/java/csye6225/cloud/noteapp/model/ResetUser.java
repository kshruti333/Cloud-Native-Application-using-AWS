package csye6225.cloud.noteapp.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ResetUser {
    @Id
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
