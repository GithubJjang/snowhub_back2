package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.reply.ReplyParam;
import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.repository.ReplyRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ReplyService {

    private final ReplyRepo replyRepo;
    private final BoardRepo boardRepo;

    // 1. 댓글 등록
    @Transactional
    public ResponseEntity<?> saveReply(ReplyParam replyParam){

        // replyDTO 이용해서 Board 찾기 -> reply 인스턴스에 초기화 -> Reply 등록
        int boardId = Integer.parseInt(replyParam.getBoardId());
        Board board = boardRepo.findById(boardId).orElseThrow(
                ()-> new RuntimeException("There is no ["+ replyParam.getBoardId()+"] Board" )
        );

        log.info("2.saveReply");
        Reply reply = Reply.builder()
                .reply(replyParam.getReply())
                .board(board)
                .build();
        replyRepo.save(reply);
        log.info("3.save Reply successfully");

        return ResponseEntity.ok(HttpStatus.OK);
    }

    // 2. Board Id 기반으로 Reply 가져오기
    @Transactional
    public List<Reply.DAO> getReply(int boardId){
        Board board = boardRepo.findById(boardId).orElseThrow(
                ()-> new NullPointerException("Error:getReply : "+boardId)
        );

        // 댓글이 안달릴수도 있으니까, Optional Null은 제외.
        // board.id를 이용해서 해당 값의 레코드를 찾는 것인데, 인덱스를 쓰는 것보단 풀스캔을 하자.
        // 어차피 정렬하는 것 자체가 불가능하고, 하면 쌉손해.
        return replyRepo.findAllByBoard(board).stream()
                .peek(e -> System.out.println("reply: "+e.getReply()))
                .map((e)->
                    Reply.DAO.builder()
                            .id(e.getId())
                            .content(e.getReply())
                            .name("익명")
                            .createDate(e.getCreateDate())
                            .build()
                )
                .toList()
                ;

    }
}
// getReply
        /*
        List<Reply> replies =replyRepo.findAllByBoard(board);
        List<ReplyFetcher> replyDTOList = new ArrayList<>();

        for(Reply r: replies){
            ReplyFetcher replyFetcher = new ReplyFetcher();
            replyFetcher.setId(r.getId());
            replyFetcher.setContent(r.getReply());
            replyFetcher.setName("익명");
            replyFetcher.setCreateDate(r.getCreateDate());

            replyDTOList.add(replyFetcher);
        }

         */
