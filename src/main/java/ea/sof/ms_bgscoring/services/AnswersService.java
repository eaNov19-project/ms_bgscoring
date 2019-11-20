package ea.sof.ms_bgscoring.services;

import ea.sof.shared.showcases.MsAnswersShowcase;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="answersService", url = "${answers.service}")
public interface AnswersService extends MsAnswersShowcase {

}
