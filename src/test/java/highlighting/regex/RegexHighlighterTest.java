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
  // Prüft das einfache Keywords und String-Literale korrekt erkannt werden
  void collectMatchesFindsSimpleTokens() {
    var regions = highlighter.computeRegions("public class Test { String s = \"hi\"; }");

    assertEquals(3, regions.size());
  }

  @Test
  // Keywords innerhalb eines Kommentares dürfen nicht separat hervorgehoben werden
  void keywordInsideLineCommentIsDiscardedBecauseCommentWins() {
    var text = "// public class";

    var regions = highlighter.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, regions.get(0).colour());
  }

  @Test
  // Javadoc Kommentare sollen Vorrang vor normalen Block-Kommentaren haben
  void javadocCommentWinsOverBlockComment() {
    var text = "/** public class */";

    var regions = highlighter.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, regions.get(0).colour());
  }

  @Test
  // Halb offene Intervalle wie [0,5) und [5, 10) dürfen sich nicht überschneiden
  void adjacentRegionsDoNotOverlap() {
    var first = new HighlightRegion(0, 5, MiniJavaColours.KEYWORD_COLOUR);
    var second = new HighlightRegion(5, 10, MiniJavaColours.STRING_LITERAL_COLOUR);

    var regions = highlighter.resolveConflicts(List.of(first, second));
    assertEquals(2, regions.size());
  }

  @Test
  // Ein leerer Text darf keine Hervorhebung erzeugen
  void emptyTextProducesNoRegions() {
    var regions = highlighter.computeRegions("");

    assertTrue(regions.isEmpty());
  }

  @Test
  // Texte ohne passende Token dürfen keine Treffer liefern
  void textWithoutMatchesProducesNoRegions() {
    var regions = highlighter.computeRegions("abc def ghi");

    assertTrue(regions.isEmpty());
  }
}
