package highlighting.regex;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaTokens;
import java.util.ArrayList;
import java.util.List;

// TODO: Implement a simple regex-based highlighting strategy. Unlike the scanning approach, this
// strategy applies each token independently to the entire input text and collects all resulting
// {@code HighlightRegion}s, even if they overlap. Conflicts are resolved in a separate step.

// TODO: Make this class extend {@code SyntaxHighlighter}, implement the abstract method {@code
// collectMatches}, and override {@code resolveConflicts} to handle overlapping regions produced by
// the naive regex-based strategy.
public class RegexHighlighter extends SyntaxHighlighter {

  // TODO: For each token, find all matches of its pattern in the input text, convert them into
  // {@code HighlightRegion}s, and combine all of these regions into a single list.

  /**
   * Wendet alle definierten Token unabhängig voneinander auf den gesamten Text an und sammelt alle
   * gefundenen Highlight-Regionen.
   */
  @Override
  public List<HighlightRegion> collectMatches(String text) {
    var regions = new ArrayList<HighlightRegion>();

    // Alle Token nacheinander auf den kompletten Text anwenden
    for (var token : MiniJavaTokens.defaultTokens()) {
      regions.addAll(token.test(text));
    }

    return regions;
  }

  // TODO: Resolve overlapping regions. Assume that {@code regions} has been normalised and sorted.
  // For any overlapping regions, keep the one that appears first in this list (which reflects the
  // token order) and discard all later overlapping regions. Longer regions that start at the same
  // position are preferred because of the sorting in {@code normalize}.

  /**
   * Entfernt überlappende Regionen. Falls sich zwei Regionen überschneiden, bleibt die Region
   * erhalten , die zuerst in der sortierten Liste vorkommt.
   */
  @Override
  public List<HighlightRegion> resolveConflicts(List<HighlightRegion> regions) {
    var result = new ArrayList<HighlightRegion>();

    // Regionen der Reihe nach prüfen
    for (var region : regions) {
      if (!overlapsAny(region, result)) {
        result.add(region);
      }
    }

    return result;
  }

  /**
   * Prüft ob sich die übergebene Region mit mindestens einer bereits ausgewählten Region
   * überschneidet
   */
  private boolean overlapsAny(HighlightRegion region, List<HighlightRegion> selectedRegions) {
    for (var selectedRegion : selectedRegions) {
      if (overlaps(region, selectedRegion)) {
        return true;
      }
    }

    return false;
  }

  /** Prüft ob sich zwei halb-offene Intervalle überschneiden */
  private boolean overlaps(HighlightRegion first, HighlightRegion second) {
    return first.start() < second.end() && second.start() < first.end();
  }
}
