package com.snowhub.server.dummy.repository.custom;

import com.snowhub.server.dummy.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface BoardRepoCustom  {
    List<Board> pagination(String category,int page);
}
