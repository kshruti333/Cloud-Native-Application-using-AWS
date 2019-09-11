package csye6225.cloud.noteapp.controller;

import com.google.gson.JsonObject;
import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.model.Attachment;
import csye6225.cloud.noteapp.model.Notes;
import csye6225.cloud.noteapp.repository.AttachmentRepository;
import csye6225.cloud.noteapp.repository.NotesRepository;
import csye6225.cloud.noteapp.service.AmazonClient;
import csye6225.cloud.noteapp.service.AttachmentService;
import csye6225.cloud.noteapp.service.MetricsConfig;
import csye6225.cloud.noteapp.service.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

@RestController
public class AttachmentController {

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private Environment environment;

    @Autowired
    private AttachmentRepository ar;

    @Autowired
    private AttachmentService as;

    @Autowired
    private NotesService ns;

    @Autowired
    private NotesRepository nr;

    @Value("${spring.profile}")
    private String profile;

    @Autowired
    public MetricsConfig metricsConfig;

    @PostMapping("/note/{noteid}/attachment")
    public ResponseEntity<Object> addAttachments(@RequestParam("file") MultipartFile file, Authentication auth, @PathVariable final String noteid,
                                                 HttpServletRequest req, HttpServletResponse res) throws AppException, SQLException {

        metricsConfig.statsDClient().incrementCounter("Adding_attachments");
        if (file.isEmpty()) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Please attach one file.");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        String attach = null;
        Notes note = ns.findNotesById(noteid);
        if(note == null) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Note not found.Please enter a valid note id");
            return ResponseEntity.badRequest().body(entity.toString());
        }

        UUID uuid= UUID.randomUUID();
        Connection conn = amazonClient.getRemoteConnection();
        Statement setupStatement = conn.createStatement();
        String createTable = "CREATE TABLE IF NOT EXISTS csye6225.attachmentdata ( attachmentid varchar(100) NOT NULL, file_size varchar (50), PRIMARY KEY(attachmentid));";
        String insertRow1 = "INSERT INTO csye6225.attachmentdata (attachmentid,file_size) VALUES ('"+ uuid.toString() +"','"+ file.getSize() +"');";

        setupStatement.addBatch(createTable);
        setupStatement.addBatch(insertRow1);
        setupStatement.executeBatch();
        setupStatement.close();

        attach = amazonClient.uploadFile(file,uuid.toString());
        Attachment att = new Attachment();
        att.setPath(attach);
        att.setAttachment_id(uuid.toString());
        note.getAttachments().add(att);
        nr.save(note);

        if(attach != null){
            return ResponseEntity.status(201).body("");
        }else{
            JsonObject entity = new JsonObject();
            metricsConfig.statsDClient().decrementCounter("Adding_attachments");
            entity.addProperty("Error", "Access denied.");
            return ResponseEntity.status(401).body(entity.toString());
        }
    }

    @PutMapping("/note/{noteid}/attachment/{attachmentid}")
    public ResponseEntity<Object> updateAttachments(@RequestParam("file") MultipartFile file, Authentication auth, @PathVariable final String noteid, @PathVariable final String attachmentid) throws AppException, SQLException {
        metricsConfig.statsDClient().incrementCounter("Update_attachments");

        if (file.isEmpty()) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Please attach one file.");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        Notes note = ns.findNotesById(noteid);
        if (note == null) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error","Note not found. Please enter a valid note id.");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        if(auth.getName().equalsIgnoreCase(note.getUser_id())){
            String status = null;
            if(profile.equalsIgnoreCase("dev")) {
                amazonClient.deleteFileFromS3Bucket(ar.getOne(attachmentid).getPath());
                status = as.updateCloudAttachment(file, note, attachmentid);

                Connection con = amazonClient.getRemoteConnection();
                Statement setupStatement = con.createStatement();
                String insertRow1 = "UPDATE attachmentdata SET file_size='"+ file.getSize() +"' WHERE attachmentid='"+ attachmentid +"';";

                setupStatement.addBatch(insertRow1);
                setupStatement.executeBatch();
                setupStatement.close();

            }else {
                status = as.updateAttachment(file, note, attachmentid);
            }
            if(status == null){
                JsonObject entity = new JsonObject();
                metricsConfig.statsDClient().decrementCounter("Update_attachments");
                entity.addProperty("Error", "Attachment not found. Please enter a valid attachment ID");
                return ResponseEntity.ok().body(entity.toString());
            }else{
                JsonObject entity = new JsonObject();
                entity.addProperty("Success", "Updated the attachment.");
                return ResponseEntity.status(204).body(entity.toString());
            }
        }else{
            JsonObject entity = new JsonObject();
            metricsConfig.statsDClient().decrementCounter("Update_attachments");
            entity.addProperty("Error", "Access denied.");
            return ResponseEntity.status(401).body(entity.toString());
        }
    }

    @DeleteMapping("/note/{noteid}/attachment/{attachmentid}")
    public ResponseEntity<Object> deleteAttachments(Authentication auth, @PathVariable final String noteid, @PathVariable final String attachmentid) throws AppException, SQLException {
        metricsConfig.statsDClient().incrementCounter("Deleting_attachment");
        if (noteid == null || attachmentid == null) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Please enter a valid note & attachment id");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        Notes note = ns.findNotesById(noteid);
        if (note == null) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Note not found. Please enter a valid note id");
            return ResponseEntity.badRequest().body(entity.toString());
        }
        if (auth.getName().equalsIgnoreCase(note.getUser_id())) {
            if(profile.equalsIgnoreCase("dev")) {
                amazonClient.deleteFileFromS3Bucket(ar.getOne(attachmentid).getPath());

                Connection con = amazonClient.getRemoteConnection();
                Statement setupStatement = con.createStatement();
                String insertRow1 = "DELETE FROM attachmentdata WHERE attachmentid='"+ attachmentid +"';";
                setupStatement.addBatch(insertRow1);
                setupStatement.executeBatch();
                setupStatement.close();
            }
            int present = as.deleteAttachment(note, attachmentid);
            if (present != 1) {
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", "Attachment not found. Please enter a valid attachment ID");
                return ResponseEntity.ok().body(entity.toString());
            } else {
                JsonObject entity = new JsonObject();
                entity.addProperty("Success", "Deleted the attachment.");
                return ResponseEntity.status(204).body(entity.toString());
            }

        } else {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Access denied.");
            return ResponseEntity.status(401).body(entity.toString());
        }
    }
}
