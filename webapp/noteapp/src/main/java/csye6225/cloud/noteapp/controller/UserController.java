package csye6225.cloud.noteapp.controller;

import com.google.gson.JsonObject;
import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.model.User;
import csye6225.cloud.noteapp.repository.UserRepository;
import csye6225.cloud.noteapp.service.MetricsConfig;
import csye6225.cloud.noteapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;

    public static int salt = 10;

    @Autowired
    public MetricsConfig metricsConfig;

    public static final Pattern email_pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @PostMapping(value = "/user/register")
    public ResponseEntity<Object> createUser(@Valid @RequestBody User user) throws AppException{

        metricsConfig.statsDClient().incrementCounter("Create_user");
        Matcher matcher = email_pattern.matcher(user.getEmail());
        boolean isEmail = matcher.find();

        if(!isEmail){
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Not a valid email");
            return ResponseEntity.badRequest().body(entity.toString());
        }

        String password = user.getPassword();
        boolean hasLetter = false;
        boolean hasDigit = false;

        if (password.length() >= 8) {
            for (int i = 0; i < password.length(); i++) {
                char x = password.charAt(i);
                if (Character.isLetter(x)) {
                    hasLetter = true;
                }
                else if (Character.isDigit(x)) {
                    hasDigit = true;
                }
                // no need to check further, break the loop
                if(hasLetter && hasDigit){
                    break;
                }
            }
            if (hasLetter && hasDigit) {
                System.out.println("STRONG");
            } else {
                JsonObject entity = new JsonObject();
                entity.addProperty("Error","Not a strong Password");
                return ResponseEntity.unprocessableEntity().body(entity.toString());
            }
        } else {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Not a strong Password");
            return ResponseEntity.unprocessableEntity().body(entity.toString());
        }

        user.setPassword(hashpw(user.getPassword()));
        User u = userService.createUser(user);
        if(u != null) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Success","User created.");
            return ResponseEntity.status(201).body(entity.toString());
        }
        else{
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","User already exists.");
            return ResponseEntity.badRequest().body(entity.toString());
        }

    }

    @GetMapping("/")
    public String getTime(){
        metricsConfig.statsDClient().incrementCounter("Get_time");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String date = timestamp.toString();
        JsonObject entity = new JsonObject();
        entity.addProperty("Date",date);
        return entity.toString();

    }

    public String hashpw(String pass){
        return BCrypt.hashpw(pass,BCrypt.gensalt(salt));
    }
}
