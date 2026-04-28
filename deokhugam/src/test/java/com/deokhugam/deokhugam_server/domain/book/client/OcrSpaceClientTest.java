package com.deokhugam.deokhugam_server.domain.book.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.deokhugam.deokhugam_server.domain.book.config.OcrSpaceProperties;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OcrSpaceClientTest {

  private static final String OCR_SPACE_URL = "https://api.ocr.space/parse/image";

  private MockRestServiceServer server;
  private OcrSpaceClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new OcrSpaceClient(builder, new OcrSpaceProperties("ocr-key", OCR_SPACE_URL));
  }

  @Test
  @DisplayName("성공: OCR 결과 텍스트와 공급자 정보를 반환한다")
  void extractText_Success() {
    // given
    MockMultipartFile image = new MockMultipartFile(
        "file",
        "book.png",
        MediaType.IMAGE_PNG_VALUE,
        "image".getBytes()
    );
    String response = """
        {
          "ParsedResults": [
            {"ParsedText": "ISBN"},
            {"ParsedText": "978-89-1234-567-8"}
          ],
          "IsErroredOnProcessing": false
        }
        """;

    server.expect(requestTo(OCR_SPACE_URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
        .andExpect(content().string(containsString("ocr-key")))
        .andExpect(content().string(containsString("eng")))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when
    TextExtractionResult result = client.extractText(image);

    // then
    assertThat(result.text()).isEqualTo("ISBN\n978-89-1234-567-8");
    assertThat(result.provider()).isEqualTo("OCR_SPACE");
    server.verify();
  }

  @Test
  @DisplayName("실패: OCR 처리 오류 응답이면 ISBN 추출 실패 예외를 던진다")
  void extractText_Fail_ErroredResponse() {
    // given
    MockMultipartFile image = new MockMultipartFile(
        "file",
        "book.png",
        MediaType.IMAGE_PNG_VALUE,
        "image".getBytes()
    );
    String response = """
        {
          "ParsedResults": [{"ParsedText": "ignored"}],
          "IsErroredOnProcessing": true
        }
        """;

    server.expect(requestTo(OCR_SPACE_URL))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when & then
    assertThatThrownBy(() -> client.extractText(image))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ISBN_EXTRACTION_FAILED);
    server.verify();
  }

  @Test
  @DisplayName("실패: OCR 결과 텍스트가 비어 있으면 ISBN 추출 실패 예외를 던진다")
  void extractText_Fail_BlankParsedText() {
    // given
    MockMultipartFile image = new MockMultipartFile(
        "file",
        "book.png",
        MediaType.IMAGE_PNG_VALUE,
        "image".getBytes()
    );
    String response = """
        {
          "ParsedResults": [{"ParsedText": "   "}],
          "IsErroredOnProcessing": false
        }
        """;

    server.expect(requestTo(OCR_SPACE_URL))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when & then
    assertThatThrownBy(() -> client.extractText(image))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ISBN_EXTRACTION_FAILED);
    server.verify();
  }

  @Test
  @DisplayName("실패: OCR API 호출이 실패하면 ISBN 추출 실패 예외로 변환한다")
  void extractText_Fail_ApiException() {
    // given
    MockMultipartFile image = new MockMultipartFile(
        "file",
        "book.png",
        MediaType.IMAGE_PNG_VALUE,
        "image".getBytes()
    );

    server.expect(requestTo(OCR_SPACE_URL))
        .andRespond(withException(new IOException("network error")));

    // when & then
    assertThatThrownBy(() -> client.extractText(image))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ISBN_EXTRACTION_FAILED);
    server.verify();
  }
}
