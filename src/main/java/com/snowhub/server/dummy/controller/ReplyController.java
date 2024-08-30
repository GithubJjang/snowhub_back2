package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dto.reply.ReplyParam;
import com.snowhub.server.dummy.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@Tag(name = "댓글 관련 API", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글에 대해서 댓글 작성/")
public class ReplyController {

    // 컴파일 때 에러를 잡기 위해서 생성자 주입 방식을 선택.
    private final ReplyService replyService;

    // 1. Reply 등록하기.
    @Operation(summary = "댓글 작성하기", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글에 대한 댓글을 작성/")
    @PostMapping("/board/reply")
    public ResponseEntity<?> getReply(@RequestBody ReplyParam replyParam){
        return replyService.saveReply(replyParam);
    }

}
