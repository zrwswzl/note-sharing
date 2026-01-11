package com.project.login.controller;

import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.ConversationListVO;
import com.project.login.model.vo.ConversationVO;
import com.project.login.service.conversationService.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Conversation", description = "Private chat operations")
@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "Get the list of conversations for a user")
    @GetMapping("/list")
    public StandardResponse<ConversationListVO> getConversationsList(@RequestParam Long userId) {
        ConversationListVO vo = conversationService.getConversationsList(userId);
        return StandardResponse.success(vo);
    }

    @Operation(summary = "Get full conversation by conversationId")
    @GetMapping("/detail")
    public StandardResponse<ConversationVO> getConversation(
            @RequestParam String conversationId
    ) {
        ConversationVO vo = conversationService.getConversation(conversationId);
        if (vo == null) {
            return StandardResponse.success("Conversation not found", null);
        }
        return StandardResponse.success(vo);
    }

    @Operation(summary = "Send a message to another user")
    @PostMapping("/send")
    public StandardResponse<String> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content
    ) {
        String conversationId = conversationService.sendMessage(senderId, receiverId, content);
        if(conversationId==null){
            return StandardResponse.success("failed, please mutualFollow first",null);
        }
        return StandardResponse.success(conversationId);
    }


}
