package csye6225.cloud.noteapp.service;

import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.repository.UserRepository;
import csye6225.cloud.noteapp.model.User;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static int salt = 10;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username " + username);
        User user = findUserByEmail(username);
        if(user == null){
            throw new UsernameNotFoundException(username + " not found");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return  new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    public User findUserByEmail(String email){
        logger.info("Finding user by email" + email);
        Iterable<User> list = userRepository.findAll();
        for(User u : list){
            if(u.getEmail().equalsIgnoreCase(email)){
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws AppException {
        try {
            logger.info("Getting all users");
            List<User> userList = userRepository.findAll();
            return userList;
        } catch (DataException e){
            logger.error("Exception in getting all existing users : ",e);
            throw new AppException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in getting all existing users : ",e);
            throw new AppException("Error getting all users");
        }
    }

    public User createUser(User user) throws AppException {
        try {
            System.out.println(user.getPassword());

            List<User> userList = getAllUsers();
            for (User u : userList) {
                if (u.getEmail().equals(user.getEmail())) {
                    return null;
                }
            }
            User newuser = new User();
            newuser.setEmail(user.getEmail());
            newuser.setPassword(user.getPassword());

            System.out.println(newuser.getPassword());

            return userRepository.save(newuser);
        } catch (DataException e){
            logger.error("Data Exception in creating user : ",e);
            throw new AppException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in creating user : ",e);
            throw new AppException("Error creating person");
        }
    }


}
