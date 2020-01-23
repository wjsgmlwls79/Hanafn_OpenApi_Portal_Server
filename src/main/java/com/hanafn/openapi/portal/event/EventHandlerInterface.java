package com.hanafn.openapi.portal.event;

import com.hanafn.openapi.portal.views.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public interface EventHandlerInterface {
    public abstract void eventCreate(String eventType, Object data, String hfnCd);
    public abstract ResponseEntity<?> handleEvent(HashMap<String,Object> data, String url);
}
