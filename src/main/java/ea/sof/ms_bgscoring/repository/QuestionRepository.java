package ea.sof.ms_bgscoring.repository;

import ea.sof.shared.entities.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {
    Optional<QuestionEntity> findById(String id);
}
