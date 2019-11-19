package ea.sof.ms_bgscoring.services;

import ea.sof.shared.showcases.MsAnswersShowcase;
import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="answer-ms-service", url = "${ANSWERS_SERVICE}")
public interface AnswersService extends MsAnswersShowcase {

}
