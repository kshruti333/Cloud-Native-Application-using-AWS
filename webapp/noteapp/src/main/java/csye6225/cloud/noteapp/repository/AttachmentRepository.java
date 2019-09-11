package csye6225.cloud.noteapp.repository;

import csye6225.cloud.noteapp.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {

}
