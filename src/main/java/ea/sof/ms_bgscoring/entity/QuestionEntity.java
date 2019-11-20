package ea.sof.ms_bgscoring.entity;

import ea.sof.ms_bgscoring.model.QuestionReqModel;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.entities.CommentQuestionEntity;
import ea.sof.shared.models.Answer;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.models.Question;
import ea.sof.shared.models.QuestionFollowers;
import ea.sof.shared.queue_models.QuestionQueueModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
@Document(collection = "questions")
public class QuestionEntity {

	@Id
	private String id;
	private String userId;
	private String userEmail;
	private String title;
	private String body;
	private LocalDateTime created;
	private LocalDateTime lastEdited;
	private Integer votes = 0;
	private Integer active = 1;
	private List<CommentQuestionEntity> topComments = new ArrayList<>();
	private List<AnswerEntity> topAnswers = new ArrayList<>();

	private Set<String> followerEmails = new HashSet<>();

    public QuestionEntity(QuestionReqModel questionModel) {
        this.title = questionModel.getTitle();
        this.body = questionModel.getBody();
        this.created = LocalDateTime.now();
        this.lastEdited = this.created;
    }


    public Question toQuestionModel() {
        Question questionModel = new Question();
        questionModel.setId(this.id);
        questionModel.setUserId(this.userId);
        questionModel.setTitle(this.title);
        questionModel.setBody(this.body);
        questionModel.setCreated(this.created);
        questionModel.setLastEdited(this.lastEdited);
        questionModel.setUpvotes(this.votes);
		questionModel.setActive(this.active);
		List<CommentQuestion> topComments = this.topComments.stream().map(cqe -> cqe.toCommentQuestionModel()).collect(Collectors.toList());
		questionModel.setTopComments(topComments);

		List<Answer> topAnswerModels = this.topAnswers.stream().map(answerEntity -> answerEntity.toAnswerModel()).collect(Collectors.toList());
		questionModel.setTopAnswers(topAnswerModels);

		return questionModel;
	}

	public QuestionQueueModel toQuestionQueueModel() {
		QuestionQueueModel questionQueueModel = new QuestionQueueModel();
		questionQueueModel.setId(this.id);
		questionQueueModel.setTitle(this.title);
		questionQueueModel.setBody(this.body);
		return questionQueueModel;
	}

	public QuestionFollowers toQuestionFollowersModel() {
		QuestionFollowers questionFollowers = new QuestionFollowers();
		questionFollowers.setId(this.id);
		questionFollowers.setTitle(this.title);
		questionFollowers.setFollowerEmails(this.followerEmails);

		return questionFollowers;
	}

	public void upvote() {
		this.votes++;
	}

	public void downvote() {
		this.votes--;
	}

	public void addFollowerEmail(String email) {
		followerEmails.add(email);
	}

	public void addQuestionComment(CommentQuestionEntity commentQuestionEntity) {
		topComments.add(commentQuestionEntity);

		//remove the oldest
		while (topComments.size() > 3) {
			topComments.remove(0);
		}
	}

	public void addAnswer(AnswerEntity answerEntity){
    	this.topAnswers.add(answerEntity);
	}

}
