package ea.sof.ms_bgscoring.kafka;

import com.google.gson.Gson;
import ea.sof.ms_bgscoring.entity.QuestionEntity;
import ea.sof.ms_bgscoring.repository.QuestionRepository;
import ea.sof.ms_bgscoring.services.AnswersService;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.queue_models.AnswerQueueModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubsAnswerVoted {

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    AnswersService answersService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubsAnswerVoted.class);

    @KafkaListener(topics = "${topicAnswerVoted}", groupId = "${subsAnswerVoted}")
    public void listener(String message) {
        LOGGER.info("\nSubsAnswerVoted :: New message from topic 'topicAnswerVoted': " + message);

        // 1. Getting data from request
        AnswerQueueModel answerQueueModel = null;
        try {
            Gson gson = new Gson();
            answerQueueModel = gson.fromJson(message, AnswerQueueModel.class);
        } catch (Exception ex) {
            LOGGER.warn("SubsAnswerVoted :: Failed to convert Json: " + ex.getMessage());
            return;
        }

        // 2. Retrieving question from DB
        String questionId = answerQueueModel.getQuestionId();
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            LOGGER.warn("SubsAnswerVoted :: Failed to retrieve Question from DB.");
            return;
        }


        // 3. Retrieving full answer by ID
        ResponseEntity<AnswerEntity> answerResponse = answersService.getAnswer(answerQueueModel.getId());
        if (answerResponse.getStatusCode() != HttpStatus.OK) {
            LOGGER.warn("SubsAnswerVoted :: Failed to retrieve Answer for.");
            return;
        }
        AnswerEntity answerEntity = answerResponse.getBody();


        // 4. get the top answers from service
        List<AnswerEntity> answerEntities = null;
        try {
            ResponseEntity<List<AnswerEntity>> answersServiceResp = answersService.getTop5AnswersByQuestionId(questionId);
            answerEntities = answersServiceResp.getBody();
        } catch (Exception ex){
            LOGGER.warn("SubsAnswerVoted :: Failed to retrieve top 5 answers for question with ID: ." + questionId);
            return;
        }

        // 5. Update the top answers
        try {
            questionEntity.setTopAnswers(answerEntities);
            questionRepository.save(questionEntity);
            LOGGER.info("SubsAnswerVoted :: Top 5 answers list updated successfully");

        } catch (Exception ex) {
            LOGGER.warn("SubsAnswerVoted :: Failed to save Entity: " + ex.getMessage());
        }
    }

}
