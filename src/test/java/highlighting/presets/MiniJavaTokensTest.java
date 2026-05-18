package highlighting.presets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import highlighting.core.HighlightRegion;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MiniJavaTokensTest {

  private List<HighlightRegion> regionsFor(Color colour, String text) {
    return MiniJavaTokens.defaultTokens().stream()
        .filter(token -> token.colour().equals(colour))
        .flatMap(token -> token.test(text).stream())
        .toList();
  }

  @Test
  void stringLiteralMatchesTextBetweenDoubleQuotes() {
    var text = "String s = \"hello\";";

    var regions = regionsFor(MiniJavaColours.STRING_LITERAL_COLOUR, text);

    assertEquals(1, regions.size());

    var region = regions.get(0);

    assertEquals("\"hello\"", text.substring(region.start(), region.end()));
  }

  @Test
  void stringLiteralCanContainCommentLikeText() {
    var text = "\"// not a comment\"";

    var regions = regionsFor(MiniJavaColours.STRING_LITERAL_COLOUR, text);

    assertEquals(1, regions.size());
  }

  @Test
  void CharLiteralMatchesSingleCharacter() {
    var text = "char c = 'a';";

    var regions = regionsFor(MiniJavaColours.CHAR_LITERAL_COLOUR, text);

    assertEquals(1, regions.size());
  }

  @Test
  void keywordMatchesOnlyWholeWords() {
    var text = "class myclass = new Example;";

    var regions = regionsFor(MiniJavaColours.KEYWORD_COLOUR, text);

    assertEquals(2, regions.size());
  }

  @Test
  void annotationsMatchesAtLineStartAndAfterWhitespace() {
    var text = "@Override\n  @Over-ride";

    var regions = regionsFor(MiniJavaColours.ANNOTATION_COLOUR, text);

    assertEquals(2, regions.size());
  }

  @Test
  void lineCommentMatchesAcrossMultipleLines() {
    var text = "int x; // comment class";

    var regions = regionsFor(MiniJavaColours.LINE_COMMENT_COLOUR, text);

    assertEquals(1, regions.size());
  }

  @Test
  void blockCommentMatchesAcrossMultipleLines() {
    var text = "a /* block\ncomment */ b";

    var regions = regionsFor(MiniJavaColours.BLOCK_COMMENT_COLOUR, text);

    assertEquals(1, regions.size());
  }

  @Test
  void javadocCommentMatchesAcrossMultipleLines() {
    var text = "/** docs\nmore docs */";

    var regions = regionsFor(MiniJavaColours.JAVADOC_COMMENT_COLOUR, text);

    assertEquals(1, regions.size());
  }

  @Test
  void noKeywordMatchInsideLongerIdentifier() {
    var text = "classification importer";

    var regions = regionsFor(MiniJavaColours.KEYWORD_COLOUR, text);

    assertTrue(regions.isEmpty());
  }
}
