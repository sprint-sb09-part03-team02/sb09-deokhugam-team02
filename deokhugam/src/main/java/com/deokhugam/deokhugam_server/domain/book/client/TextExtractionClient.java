package com.deokhugam.deokhugam_server.domain.book.client;

import org.springframework.web.multipart.MultipartFile;

public interface TextExtractionClient {

  TextExtractionResult extractText(MultipartFile image);
}
