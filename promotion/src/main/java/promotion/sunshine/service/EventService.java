package promotion.sunshine.service;

import java.util.Map;

import common.sunshine.model.selling.event.EventApplication;
import common.sunshine.model.selling.event.QuestionAnswer;
import common.sunshine.utils.ResultData;

public interface EventService {
	ResultData fetchGiftEvent(Map<String, Object> condition);
	
	ResultData fetchQuestionOption(Map<String, Object> condition);
	
	ResultData insertEventApplication(EventApplication eventApplication);
	
	ResultData fetchEventApplication(Map<String, Object> condition);
	
	ResultData insertQuestionAnswer(QuestionAnswer questionAnswer);
}
