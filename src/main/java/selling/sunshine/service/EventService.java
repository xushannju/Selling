package selling.sunshine.service;

import java.util.Map;

import common.sunshine.model.selling.event.EventApplication;
import common.sunshine.model.selling.event.GiftEvent;
import common.sunshine.model.selling.order.EventOrder;
import common.sunshine.pagination.DataTableParam;
import common.sunshine.utils.ResultData;

public interface EventService {
	
	ResultData createGiftEvent(GiftEvent event);
	
	ResultData fetchGiftEvent(Map<String, Object> condition);
	
	ResultData updateGiftEvent(GiftEvent event);
	
	ResultData fetchGiftEventByPage(Map<String, Object> condition, DataTableParam param);
	
	ResultData fetchEventApplication(Map<String, Object> condition);
	
	ResultData fetchEventApplicationByPage(Map<String, Object> condition, DataTableParam param);
	
	ResultData updateEventApplication(EventApplication eventApplication);
	
	ResultData createEventOrder(EventOrder eventOrder);
	
	ResultData updateEventOrder(EventOrder eventOrder);
	
	ResultData fetchEventOrder(Map<String, Object> condition);
	
	ResultData fetchEventOrderByPage(Map<String, Object> condition, DataTableParam param);



}
