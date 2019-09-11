package csye6225.cloud.noteapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import csye6225.cloud.noteapp.model.Notes;

import java.util.UUID;

public interface NotesRepository extends JpaRepository<Notes, String> {
    //Optional<Notes> findNotesByNote_id(String note_id);
}
