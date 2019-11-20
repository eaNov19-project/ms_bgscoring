package ea.sof.ms_bgscoring.kafka;

import com.google.gson.Gson;
import ea.sof.ms_bgscoring.entity.QuestionEntity;
import ea.sof.ms_bgscoring.repository.QuestionRepository;
import ea.sof.ms_bgscoring.services.AnswersService;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.queue_models.AnswerQueueModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubsAnswerVoted {

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    AnswersService answersService;

    @KafkaListener(topics = "${topicAnswerVoted}", groupId = "${subsAnswerVoted}")
    public void listener(String message) {
        log.info("\nSubsAnswerVoted :: New message from topic 'topicAnswerVoted': " + message);

        // 1. Getting data from request
        AnswerQueueModel answerQueueModel = null;
        try {
            Gson gson = new Gson();
            answerQueueModel = gson.fromJson(message, AnswerQueueModel.class);
        } catch (Exception ex) {
            log.warn("SubsAnswerVoted :: Failed to convert Json: " + ex.getMessage());
        }

        // 2. Retrieving question from DB
        String questionId = answerQueueModel.getQuestionId();
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            log.warn("SubsAnswerVoted :: Failed to retrieve Question from DB.");
            return;
        }


        // 3. Retrieving full answer by ID
        ResponseEntity<AnswerEntity> answerResponse = answersService.getAnswer(answerQueueModel.getId());
        if (answerResponse.getStatusCode() != HttpStatus.OK) {
            log.warn("SubsAnswerVoted :: Failed to retrieve Answer for.");
            return;
        }
        AnswerEntity answerEntity = answerResponse.getBody();


        // 3. adding the answer to the top list if there is space
        try {

            if (questionEntity.getTopAnswers().size() < 5) {
                questionEntity.addAnswer(answerEntity);
                log.info("SubsAnswerVoted :: Answer added to the top 5 answers list of question");
            } else {
                for (int i = questionEntity.getTopAnswers().size() - 1; i >= 0; i--) {
                    if (questionEntity.getTopAnswers().get(i).getVotes() < answerQueueModel.getVotes()) {
                        questionEntity.getTopAnswers().remove(questionEntity.getTopAnswers().get(i));
                        questionEntity.addAnswer(answerEntity);
                        log.info("SubsAnswerVoted :: Answer was replaced in the top 5 answers list of questions");
                    }
                }
            }
            questionEntity.getTopAnswers().stream()
                    .sorted(Comparator.comparingInt(AnswerEntity::getVotes).reversed())
                    .collect(Collectors.toList());
            questionRepository.save(questionEntity);
            log.info("SubsAnswerVoted :: Top 5 answers list updated successfully");

        } catch (Exception ex) {
            log.warn("SubsAnswerVoted :: Failed to save Entity: " + ex.getMessage());
        }
    }

}
