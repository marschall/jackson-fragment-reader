import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class JacksonFragmentReaderTests {
  
  private static final String EXPECTED = """
      {
        "stirngKey": "stringValue",
        "integerKey": 42,
        "bigDecimalKey": 10.1,
        "dateKey": "2023-05-06"
      }
      """;
  private Reader reader;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    this.reader = new JacksonFragmentReader(objectMapper, List.of("""
        {
          "stirngKey":\s""",
        """
           ,\n  "integerKey":\s""",
        """
           ,\n  "bigDecimalKey":\s""",
        """
           ,\n  "dateKey":\s""",
        """
        \n}
        """),
        List.of("stringValue", 42, new BigDecimal("10.1"), LocalDate.of(2023, Month.MAY, 6)));
  }

  @Test
  void readFully() throws IOException {
    StringWriter writer = new StringWriter();
    this.reader.transferTo(writer);
    assertEquals(EXPECTED, writer.toString());
  }

  @Test
  void charBufferHeap() throws IOException {
    CharBuffer charBuffer = CharBuffer.allocate(EXPECTED.length());
    while (this.reader.read(charBuffer) != -1) {
      // nothing
    }
    charBuffer.flip();
    assertEquals(EXPECTED, charBuffer.toString());
  }
  
  @Test
  void charBufferOffHeap() throws IOException {
    CharBuffer charBuffer = ByteBuffer.allocateDirect(EXPECTED.length() * 2).asCharBuffer();
    while (this.reader.read(charBuffer) != -1) {
      // nothing
    }
    charBuffer.flip();
    assertEquals(EXPECTED, charBuffer.toString());
  }

  @Test
  void readJson() throws IOException {
    JsonNode actual = this.objectMapper.readTree(this.reader);
    JsonNode expected = this.objectMapper.readTree(EXPECTED);
    
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

}
