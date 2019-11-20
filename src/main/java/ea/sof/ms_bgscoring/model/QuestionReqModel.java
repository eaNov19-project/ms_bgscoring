package ea.sof.ms_bgscoring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionReqModel {

    @NotEmpty(message = "Please provide the title")
    private String title;

    @NotEmpty(message = "Please provide the body")
    private String body;
}
