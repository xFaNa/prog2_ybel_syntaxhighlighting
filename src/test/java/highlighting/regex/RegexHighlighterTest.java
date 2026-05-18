package highlighting.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import highlighting.core.HighlightRegion;
import highlighting.presets.MiniJavaColours;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RegexHighlighterTest {

  private final RegexHighlighter highlighter = new RegexHighlighter();

  @Test
  void collectMatchesFindsSimpleTokens() {
    var regions = highlighter.computeRegions("public class Test { String s = \"hi\"; }");

    assertEquals(3, regions.size());
  }

  @Test
  void keywordInsideLineCommentIsDiscardedBecauseCommentWins() {
    var text = "// public class";

    var regions = highlighter.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, regions.get(0).colour());
  }

  @Test
  void javadocCommentWinsOverBlockComment() {
    var text = "/** public class */";

    var regions = highlighter.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, regions.get(0).colour());
  }

  @Test
  void adjacentRegionsDoNotOverlap() {
    var first = new HighlightRegion(0, 5, MiniJavaColours.KEYWORD_COLOUR);
    var second = new HighlightRegion(5, 10, MiniJavaColours.STRING_LITERAL_COLOUR);

    var regions = highlighter.resolveConflicts(List.of(first, second));
    assertEquals(2, regions.size());
  }

  @Test
  void emptyTextProducesNoRegions() {
    var regions = highlighter.computeRegions("");

    assertTrue(regions.isEmpty());
  }

  @Test
  void textWithoutMatchesProducesNoRegions() {
    var regions = highlighter.computeRegions("abc def ghi");

    assertTrue(regions.isEmpty());
  }
}
