package com.deokhugam.deokhugam_server.domain.book.client;

import com.deokhugam.deokhugam_server.domain.book.config.OcrSpaceProperties;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class OcrSpaceClient implements TextExtractionClient {

  private static final String PROVIDER = "OCR_SPACE";

  private final RestClient.Builder restClientBuilder;
  private final OcrSpaceProperties properties;

  @Override
  public TextExtractionResult extractText(MultipartFile image) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("apikey", properties.key());
      body.add("language", "eng");
      body.add("isOverlayRequired", "false");
      body.add("file", image.getResource());

      OcrSpaceResponse response = restClientBuilder.build()
        .post()
        .uri(properties.url())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
        .retrieve()
        .body(OcrSpaceResponse.class);

      if (response == null || response.isErroredOnProcessing()) {
        throw new DeokhugamException(ErrorCode.ISBN_EXTRACTION_FAILED);
      }

      String parsedText = response.parsedResults().stream()
        .map(OcrParsedResult::parsedText)
        .filter(text -> text != null && !text.isBlank())
        .reduce("", (left, right) -> left + "\n" + right)
        .trim();

      if (parsedText.isBlank()) {
        throw new DeokhugamException(ErrorCode.ISBN_EXTRACTION_FAILED);
      }

      return new TextExtractionResult(parsedText, PROVIDER);
    } catch (DeokhugamException e) {
      throw e;
    } catch (Exception e) {
      throw new DeokhugamException(ErrorCode.ISBN_EXTRACTION_FAILED);
    }
  }

  private record OcrSpaceResponse(
    @JsonProperty("ParsedResults")
    List<OcrParsedResult> parsedResults,
    @JsonProperty("IsErroredOnProcessing")
    boolean isErroredOnProcessing
  ) {
    private OcrSpaceResponse {
      parsedResults = parsedResults == null ? List.of() : parsedResults;
    }
  }

  private record OcrParsedResult(
    @JsonProperty("ParsedText")
    String parsedText
  ) {
  }
}
