package highlighting.antlr;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import java.awt.*;
import java.util.List;
import org.antlr.v4.runtime.*;
import java.util.ArrayList;

// TODO Phase III — AntlrTokenCollector (token-based syntax highlighting).

// This highlighter uses the ANTLR-generated MiniJavaLexer to turn the input text into a token
// stream. {@code collectMatches(String)} is the only method you need to implement: extract tokens
// of interest and map them to {@code HighlightRegions} using the colours from {@code
// MiniJavaColours}. Sorting, filtering of invalid regions, and conflict handling are performed by
// the base class {@code SyntaxHighlighter} via the template method {@code computeRegions(...)}.
public class AntlrTokenCollector extends SyntaxHighlighter {

  // TODO (Phase III — implement this method): Use the token stream produced by the ANTLR-generated
  // {@code MiniJavaLexer} to collect highlight regions.
  //
  // Requirements / hints:
  // - Iterate over the lexer tokens (typically via {@code CommonTokenStream}); ignore the EOF
  // token.
  // - For each token type that should be coloured (e.g., keywords, string/char literals, comments),
  // create a {@code HighlightRegion} with the corresponding colour from {@code MiniJavaColours}.
  // - Use {@code Token#getStartIndex()} and {@code Token#getStopIndex()} (inclusive) to compute
  // {@code [start, end)} ranges: {@code start = startIndex, end = stopIndex + 1}.
  // - Do not sort, merge, or resolve overlaps here; return all candidates as you find them.
  // Normalisation and conflict resolution are handled later by the template method.
  // - Annotation highlighting: colour '@' and the immediately following IDENTIFIER token (if
  // present).
  @Override
  public List<HighlightRegion> collectMatches(String text) {

      // Eingabetext in einen ANTLR-Zeichenstrom umwandeln
    CharStream input = CharStreams.fromString(text);

      // Lexer erzeugen und den Text in Tokens zerlegen
    MiniJavaLexer lexer = new MiniJavaLexer(input);

      // Alle vom Lexer erzeugten Tokens sammeln
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    tokenStream.fill();

    List<HighlightRegion> regions = new ArrayList<>();
    List<Token> tokens = tokenStream.getTokens();

      // Alle Tokens durchlaufen
    for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);

        // EOF markiert das Ende des Token-Streams und wird ignoriert
        if  (token.getType() == Token.EOF) {
            continue;
        }

        // Passende Farbe für den aktuellen Tokentyp bestimmen
        Color colour = colourFor(token.getType());

        // Für relevante Token eine Highlight-Region erzeugen
        if (colour != null) {
            int start = token.getStartIndex();
            int end = token.getStopIndex() + 1;
            regions.add(new HighlightRegion(start, end, colour));
        }

        // Annotationen wie @Override als gemeinsame Region markieren
        if (token.getType() == MiniJavaLexer.AT
            && i + 1 < tokens.size()
            && tokens.get(i + 1).getType() == MiniJavaLexer.IDENTIFIER) {

            Token next = tokens.get(i + 1);
            regions.add(
                new HighlightRegion(
                    token.getStartIndex(),
                    next.getStopIndex() + 1,
                    MiniJavaColours.ANNOTATION_COLOUR));
        }
    }

      return regions;
  }
    /**
     * Ordnet einem ANTLR-Tokentyp die passende Highlight-Farbe zu.
     *
     * @param tokenType Typ des vom Lexer erzeugten Tokens
     * @return Zugehörige Farbe oder null, falls der Tokentyp
     *         nicht hervorgehoben werden soll
     */

    private Color colourFor(int tokenType) {
        return switch (tokenType) {
            case MiniJavaLexer.STRING_LITERAL -> MiniJavaColours.STRING_LITERAL_COLOUR;
            case MiniJavaLexer.CHAR_LITERAL -> MiniJavaColours.CHAR_LITERAL_COLOUR;

            case MiniJavaLexer.PACKAGE,
                 MiniJavaLexer.IMPORT,
                 MiniJavaLexer.CLASS,
                 MiniJavaLexer.PUBLIC,
                 MiniJavaLexer.PRIVATE,
                 MiniJavaLexer.FINAL,
                 MiniJavaLexer.RETURN,
                 MiniJavaLexer.NULL,
                 MiniJavaLexer.NEW,
                 MiniJavaLexer.IF,
                 MiniJavaLexer.ELSE,
                 MiniJavaLexer.WHILE,
                 MiniJavaLexer.EXTENDS,
                 MiniJavaLexer.IMPLEMENTS -> MiniJavaColours.KEYWORD_COLOUR;

            case MiniJavaLexer.LINE_COMMENT -> MiniJavaColours.LINE_COMMENT_COLOUR;
            case MiniJavaLexer.BLOCK_COMMENT -> MiniJavaColours.BLOCK_COMMENT_COLOUR;
            case MiniJavaLexer.JAVADOC_COMMENT -> MiniJavaColours.JAVADOC_COMMENT_COLOUR;

            case MiniJavaLexer.AT -> MiniJavaColours.ANNOTATION_COLOUR;

            default -> null;
        };
    }
}

