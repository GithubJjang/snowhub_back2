package com.snowhub.server.dummy.dto.reply;

import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.Reply;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardWithReplies {
    private Board.DAO boardDTO;
    private List<Reply.DAO> replyDTO;

}
