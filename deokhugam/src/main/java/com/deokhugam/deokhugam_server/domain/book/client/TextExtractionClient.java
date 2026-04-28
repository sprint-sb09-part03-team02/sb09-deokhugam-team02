package com.deokhugam.deokhugam_server.domain.book.client;

import org.springframework.web.multipart.MultipartFile;

public interface TextExtractionClient {

  String parseText(MultipartFile image);
}
