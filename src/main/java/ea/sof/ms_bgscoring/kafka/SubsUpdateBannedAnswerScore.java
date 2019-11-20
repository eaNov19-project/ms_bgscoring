package ea.sof.ms_bgscoring.kafka;

import com.google.gson.Gson;
import ea.sof.ms_bgscoring.entity.QuestionEntity;
import ea.sof.ms_bgscoring.repository.QuestionRepository;
import ea.sof.ms_bgscoring.services.AnswersService;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.queue_models.AnswerQueueModel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubsUpdateBannedAnswerScore {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubsUpdateBannedAnswerScore.class);

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    AnswersService answersService;

    @KafkaListener(topics = "${topicUpdateBannedAnswer}", groupId = "${subsUpdateBannedAnswerScore}")
    public void listener(String message) {
        LOGGER.info("\nSubsUpdateBannedAnswerScore :: New message from topic 'topicUpdateBannedAnswer': " + message);

        // 1. Getting data from request
        AnswerQueueModel answerQueueModel = null;
        try {
            Gson gson = new Gson();
            answerQueueModel = gson.fromJson(message, AnswerQueueModel.class);
        } catch (Exception ex) {
            LOGGER.warn("SubsUpdateBannedAnswerScore :: Failed to convert Json: " + ex.getMessage());
        }

        // 2. Retrieving question from DB
        String questionId = answerQueueModel.getQuestionId();
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            LOGGER.warn("SubsUpdateBannedAnswerScore :: Failed to retrieve Question from DB.");
            return;
        }

        ResponseEntity<List<AnswerEntity>> getAnswersResp = null;
        List<AnswerEntity> answerEntities = null;
        try {
            getAnswersResp = answersService.getTop5AnswersByQuestionId(questionId);
            answerEntities = getAnswersResp.getBody();
        }catch (Exception ex){
            LOGGER.warn("SubsUpdateBannedAnswerScore :: Failed to get top 5 answers from AnswerService: " + ex.getMessage());
        }

        // 3. adding the answer to the top list if there is space
        try {

            for (int i = questionEntity.getTopAnswers().size() - 1; i >= 0; i--) {
                if (questionEntity.getTopAnswers().get(i).getId().equals(answerQueueModel.getId())) {

                    answerEntities = answerEntities.stream().sorted(Comparator.comparingInt(AnswerEntity::getVotes).reversed()).limit(5).collect(Collectors.toList());

                    questionEntity.setTopAnswers(answerEntities);
                    questionRepository.save(questionEntity);

                    LOGGER.info("SubsUpdateBannedAnswerScore :: top 5 answers list updated");
                    break;
                }
            }

        } catch (Exception ex) {
            LOGGER.warn("SubsUpdateBannedAnswerScore :: Failed to save Entity: " + ex.getMessage());
        }
    }

}
