package ea.sof.ms_bgscoring;

import com.google.gson.Gson;
import ea.sof.ms_bgscoring.repository.QuestionRepository;
import ea.sof.ms_bgscoring.services.AnswersService;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.entities.CommentQuestionEntity;
import ea.sof.shared.entities.QuestionEntity;
import ea.sof.shared.models.Answer;
import ea.sof.shared.models.Response;
import ea.sof.shared.queue_models.AnswerQueueModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsNewAnswerScoring {

	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	AnswersService answersService;

	@KafkaListener(topics = "${topicNewAnswer}", groupId = "${subsNewAnswerScoring}")
	public void listener(String message) {
		System.out.println("\nSubsNewAnswerScoring :: New message from topic 'topicNewAnswer': " + message);

		// 1. Getting data from request
		AnswerQueueModel answerQueueModel = null;
		try {
			Gson gson = new Gson();
			answerQueueModel = gson.fromJson(message, AnswerQueueModel.class);
		} catch (Exception ex) {
			System.out.println("SubsNewAnswerScoring :: Failed to convert Json: " + ex.getMessage());
		}

		// 2. Retrieving question from DB
		String questionId = answerQueueModel.getQuestionId();
		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("SubsNewAnswerScoring :: Failed to retrieve Question from DB.");
			return;
		}

		// 3. Retrieving full answer by ID
		ResponseEntity<Answer> answerResponse = answersService.getAnswer(answerQueueModel.getId(), "");
		if (answerResponse.getStatusCode() != HttpStatus.OK) {
			System.out.println("SubsNewAnswerScoring :: Failed to retrieve Answer for.");
			return;
		}

		// 3. Comparing answer first
		// if new answer is better then include it and save
		if ()
		// if new answer is worse then doing nothing

		try {
			AnswerEntity answerEntity = new AnswerEntity(answerQueueModel);

			questionEntity.addQuestionComment();
			questionRepository.save(questionEntity);
			System.out.println("SubsNewAnswerScoring :: Comment added");
		} catch (Exception ex){
			System.out.println("SubsNewAnswerScoring :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
