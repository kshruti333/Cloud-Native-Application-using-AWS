package csye6225.cloud.noteapp.service;

import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.model.Notes;
import csye6225.cloud.noteapp.repository.NotesRepository;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotesService {

    private static final Logger logger = LoggerFactory.getLogger(NotesService.class);

    @Autowired
    private NotesRepository noteRepository;

    public List<Notes> getAllNotes() throws AppException {
        try {
            logger.info("Getting all notes");
            List<Notes> notesList = noteRepository.findAll();
            return notesList;
        } catch (DataException e){
            throw new AppException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in getting all notes : " ,e);
            throw new AppException("Error getting all notes");
        }
    }

    public Notes createNote(String title, String content,String name) throws AppException {
        try {
            logger.info("Creating Note with " + title );
            List<Notes> notesList  = getAllNotes();
            for (Notes n : notesList) {
                if (n.getTitle().equalsIgnoreCase(title) && n.getUser_id().equalsIgnoreCase(name)) {
                    return null;
                }
            }
            Notes newnote = new Notes();
            UUID uuid = UUID.randomUUID();
            newnote.setNote_id(uuid.toString());
            newnote.setTitle(title);
            newnote.setContent(content);
            newnote.setCreated_ts(new Date().toString());
            newnote.setUpdates_ts(new Date().toString());
            newnote.setUser_id(name);

            return noteRepository.save(newnote);
        } catch (DataException e){
            logger.error("Dataexception in creating note : ",e);
            throw new AppException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in creating note : ",e);
            throw new AppException("Error creating note");
        }
    }


    public Notes findNotesById(String noteId){
        logger.info("Finding note by noteID "+ noteId);
        Iterable<Notes> notesList = noteRepository.findAll();
        Notes notes = null;
        for(Notes note : notesList){
            if(note.getNote_id().equalsIgnoreCase(noteId)){
                notes = note;
            }
        }
        return notes;
    }

    public List<Notes> getUserNotes(String user) throws AppException{
        try{
            logger.info("Getting all notes created  by user " + user);
            List<Notes> notesList = getAllNotes();
            List<Notes> userNotes = new ArrayList<Notes>();
            for(Notes note : notesList) {
                if (note.getUser_id().equalsIgnoreCase(user))
                    userNotes.add(note);
            }
            return userNotes;
        } catch (Exception e){
            logger.error("Exception in getting all the notes : ",e);
            throw new AppException(400,e.getMessage());
        }
    }

}
