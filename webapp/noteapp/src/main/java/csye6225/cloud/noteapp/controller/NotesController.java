package csye6225.cloud.noteapp.controller;

import com.google.gson.JsonObject;
import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.model.Attachment;
import csye6225.cloud.noteapp.model.Notes;
import csye6225.cloud.noteapp.repository.AttachmentRepository;
import csye6225.cloud.noteapp.repository.NotesRepository;
import csye6225.cloud.noteapp.repository.UserRepository;
import csye6225.cloud.noteapp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.security.Principal;
import java.util.*;

@RestController
public class NotesController {

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private NotesService notesService;

    @Autowired
    private AttachmentRepository ar;

    @Autowired
    private AttachmentService as;

    @Autowired
    public MetricsConfig metricsConfig;

    @Value("${spring.profile}")
    private String profile;

    @GetMapping("/note")
    public List<Notes> getNotes(Authentication auth) throws AppException {
        List<Notes> notesList = notesService.getUserNotes(auth.getName());
        return notesList;
    }

    @PostMapping(value= "/note")
    public ResponseEntity<Object> createNote(@Valid @RequestBody Notes note,Authentication auth) throws AppException {
        metricsConfig.statsDClient().incrementCounter("Create_Note");
        String title = note.getTitle();
        String content = note.getContent();
        if(title != null && content != null) {
            Notes nt = notesService.createNote(title,content,auth.getName());
            if(nt != null) {
                JsonObject entity = new JsonObject();
                entity.addProperty("Success", "Note created");
                entity.addProperty("NoteID", nt.getNote_id());
                return ResponseEntity.status(201).body(entity.toString());
            }else{
                metricsConfig.statsDClient().decrementCounter("Create_Note");
                JsonObject entity = new JsonObject();
                entity.addProperty("Error","A note with same title already exists for this user. Please create another or update old one.");
                return ResponseEntity.badRequest().body(entity.toString());
            }
        }else {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Please send the necessary parameters to store note.");
            return ResponseEntity.badRequest().body(entity.toString());
        }

    }

    @GetMapping("/note/{id}")
    public ResponseEntity<Object> getNote(@PathVariable final String id,Authentication auth){
        metricsConfig.statsDClient().incrementCounter("Get_note");
        Notes note = notesService.findNotesById(id);
        if(note != null)
            if(auth.getName().equalsIgnoreCase(note.getUser_id())) {
                return ResponseEntity.ok().body(note);
            }else{
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", "Access denied.");
                return ResponseEntity.status(401).body(entity.toString());
            }
        else {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Note not found");
            return ResponseEntity.status(404).body(entity.toString());
        }
    }

    @PutMapping("/note/{noteId}")
    public ResponseEntity<Object> updateNote(@RequestBody Notes note,@PathVariable final String noteId, Authentication auth){
        metricsConfig.statsDClient().incrementCounter("Update_note");
        Notes userNotes = notesService.findNotesById(noteId);
        Notes updated = null;
        if (userNotes == null) {
            JsonObject entity = new JsonObject();
            metricsConfig.statsDClient().decrementCounter("Update_note");
            entity.addProperty("Error", "Note not found");
            return ResponseEntity.status(404).body(entity.toString());
        }

        if(auth.getName().equalsIgnoreCase(userNotes.getUser_id())) {
            userNotes.setTitle(note.getTitle());
            userNotes.setContent(note.getContent());
            userNotes.setCreated_ts(userNotes.getCreated_ts());
            userNotes.setUpdates_ts(new Date().toString());
            updated = notesRepository.save(userNotes);

            if(updated != null) {
                JsonObject entity = new JsonObject();
                entity.addProperty("Success", "Note has been updated.");
                return ResponseEntity.status(204).body(entity.toString());
            }else{
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", "error updating the note. Look at logs for details");
                return ResponseEntity.badRequest().body(entity.toString());
            }
        }else{
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Access denied.");
            return ResponseEntity.status(401).body(entity.toString());
        }
    }

    @DeleteMapping("/note/{id}")
    public ResponseEntity<Object> deleteNote( @PathVariable final String id, Authentication auth){
        metricsConfig.statsDClient().incrementCounter("Delete_note");
        if(id == null){
            JsonObject entity = new JsonObject();
            metricsConfig.statsDClient().decrementCounter("delete note");
            entity.addProperty("Error", "Please enter a valid note id");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        Notes note = notesService.findNotesById(id);
        if(note != null) {
            if (auth.getName().equalsIgnoreCase(note.getUser_id())) {
                Collection<Attachment> lista = note.getAttachments();
                for (Attachment a: lista ) {
                    if(profile.equalsIgnoreCase("dev")) {
                        amazonClient.deleteFileFromS3Bucket(a.getPath());
                    }else{
                        File oldfile = new File(a.getPath());
                        if (oldfile.exists()) {
                            oldfile.delete();
                        }
                    }
                }
                notesRepository.deleteById(id);
                JsonObject entity = new JsonObject();
                entity.addProperty("Success", "Deleted the note.");
                return ResponseEntity.status(204).body(entity.toString());
            } else {
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", "Access denied.");
                return ResponseEntity.status(401).body(entity.toString());
            }
        }else{
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Note not found");
            return ResponseEntity.status(404).body(entity.toString());
        }
    }

}
