package com.minzheng.blog.service;

import com.minzheng.blog.dto.MessageDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 留言实时推送服务
 *
 * @author caiguoyu
 */
@Service
public class MessageSseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connect").data("ok"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void sendMessage(MessageDTO messageDTO) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("message").data(messageDTO));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        });
    }
}
