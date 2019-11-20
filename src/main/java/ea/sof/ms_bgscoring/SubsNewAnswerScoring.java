package ea.sof.ms_bgscoring;

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

@Service
@Slf4j
public class SubsNewAnswerScoring {

	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	AnswersService answersService;

	@KafkaListener(topics = "${topicNewAnswer}", groupId = "${subsNewAnswerScoring}")
	public void listener(String message) {
		log.info("\nSubsNewAnswerScoring :: New message from topic 'topicNewAnswer': " + message);

		// 1. Getting data from request
		AnswerQueueModel answerQueueModel = null;
		try {
			Gson gson = new Gson();
			answerQueueModel = gson.fromJson(message, AnswerQueueModel.class);
		} catch (Exception ex) {
			log.warn("SubsNewAnswerScoring :: Failed to convert Json: " + ex.getMessage());
		}

		// 2. Retrieving question from DB
		String questionId = answerQueueModel.getQuestionId();
		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			log.warn("SubsNewAnswerScoring :: Failed to retrieve Question from DB.");
			return;
		}

		// 3. Retrieving full answer by ID
		ResponseEntity<AnswerEntity> answerResponse = answersService.getAnswer(answerQueueModel.getId());
		if (answerResponse.getStatusCode() != HttpStatus.OK) {
			log.warn("SubsNewAnswerScoring :: Failed to retrieve Answer for.");
			return;
		}

		AnswerEntity answerEntity = answerResponse.getBody();

		// 3. adding the answer to the top list if there is space
		try {
			if(questionEntity.getTopAnswers().size() < 5){
				questionEntity.addAnswer(answerEntity);
				questionRepository.save(questionEntity);
				log.info("SubsNewAnswerScoring :: Answer added to the top 5 answers list of question");
			}
		} catch (Exception ex){
			log.warn("SubsNewAnswerScoring :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
