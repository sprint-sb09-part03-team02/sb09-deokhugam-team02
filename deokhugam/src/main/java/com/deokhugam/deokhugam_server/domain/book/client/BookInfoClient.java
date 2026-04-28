package com.deokhugam.deokhugam_server.domain.book.client;

import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;

public interface BookInfoClient {

  NaverBookDto searchByIsbn(String isbn);
}
