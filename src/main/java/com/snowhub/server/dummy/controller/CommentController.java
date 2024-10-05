package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dto.comment.CommentParam;
import com.snowhub.server.dummy.model.Comment;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.ReplyRepo;
import com.snowhub.server.dummy.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;


@Slf4j
@AllArgsConstructor
@RestController
@Tag(name = "답글 관련 API", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글의 댓글에 대해서 답글 작성/불러오기/")
public class CommentController {


    private final ReplyRepo replyRepo;// 런타임 전에 오류를 잡기 위해서, 생성자 의존성 주입을 한다.
    private final CommentService commentService;

    @Operation(summary = "답글 작성하기", description = "하나의 댓글에 대한 하나의 답글을 작성합니다.")
    @PostMapping("/board/comment")
    public ResponseEntity<?> getComment(@RequestParam(name = "id") int replyId,
                                        @RequestBody CommentParam commentParam){
        return commentService.saveComment(commentParam,replyId);

    }

    @Operation(summary = "답글 불러오기", description = "하나의 댓글에 대한 모든 답글을 불러옵니다.")
    @GetMapping("/board/reply/comment")
    public ResponseEntity<?> getComment(@RequestParam(name = "id")int replyId){
        // 이 부분에 대해서도 쿼리 2방 보내는 것 대신에 1방으로 수정이 가능하다

        // Original버전
        Reply reply = replyRepo.findById(replyId).orElseThrow(
                ()-> new NullPointerException("/board/reply/commment")
        );

        List<Comment.DAO> commentFetchers = commentService.getComment(reply);

        // reply로 comment에 관련 답글찾기
        return ResponseEntity.ok(commentFetchers);
    }

}
