import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JacksonFragmentReaderTests {
  
  private static final String EXPECTED = """
      {
        "stirngKey": "stringValue",
        "integerKey": 42,
        "bigDecimalKey": 10.1
      }
      """;
  private Reader reader;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.objectMapper = new ObjectMapper();
    this.reader = new JacksonFragmentReader(objectMapper, List.of("""
        {
          "stirngKey":\s""",
        """
           ,\n  "integerKey":\s""",
        """
           ,\n  "bigDecimalKey":\s""",
        """
        \n}
        """),
        List.of("stringValue", 42, new BigDecimal("10.1")));
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
