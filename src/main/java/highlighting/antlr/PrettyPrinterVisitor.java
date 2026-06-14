package highlighting.antlr;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTree;

/// MiniJava Pretty Printer (minimal, stateful)
///
/// Requirements:
/// - Reproduce the whole program (comments and whitespaces are gone).
/// - Ignore whitespace from the input; instead, generate:
///     - indentation for class bodies and blocks,
///     - exactly one line per statement (lines ending in ';').
///
/// Simplification:
/// Everything that is not indentation or line breaks is printed as raw tokens (with a very simple
/// space heuristic). Expression and signature formatting is therefore not "nice", which is
/// acceptable for this exercise.
///

/**
 *  Der Visitor durchläuft den von ANTLR erzeugten Parse Tree und erzeugt daraus formatierten
 *  Quellcode. Dabei werden die ursprünglichen Leerzeichen und Kommentare nicht übernommen, sondern
 *  Einrückungen und Zeilenumbrüche neu erzeugt.
 */
public final class PrettyPrinterVisitor extends MiniJavaBaseVisitor<Void> {

  private final StringBuilder out = new StringBuilder();
  private final int indentWidth;
  private int currentIndent = 0;
  private boolean atLineStart = true;

  // For simple spacing between tokens:
  private Token lastToken = null;

  public PrettyPrinterVisitor(int indentWidth) {
    this.indentWidth = Math.max(0, indentWidth);
  }

  public String result() {
    return out.toString();
  }

  // ----------------------------------------------------
  // Structural methods – these enforce indentation and "one statement per line"
  //
  // TODO: implement the four structural visitXyz-methods below: visitCompilationUnit,
  // visitClassBody, visitBlock, and visitStatement
  // ----------------------------------------------------

  @Override
  public Void visitCompilationUnit(MiniJavaParser.CompilationUnitContext ctx) {
    // TODO:
    // Produce a nicely structured compilation unit:
    // - package declaration (if present),
    // - import declarations (one per line),
    // - type declarations (one after another),
    // with sensible blank lines between these parts.

      // Die oberste Programmebene durchlaufen: package, imports und Klassen
      for (int i = 0; i < ctx.getChildCount(); i++) {
          ParseTree child = ctx.getChild(i);

          // EOF ist nur das künstliche Dateiende und wird nicht ausgegeben
          if (child instanceof TerminalNode terminal
              && terminal.getSymbol().getType() == Token.EOF) {
              continue;
          }

          // Aktuelles Kind besuchen und dadurch ausgeben
          visit(child);

          // Falls nach dem Besuch noch Text in der aktuellen Zeile steht, Zeile abschließen
          if (!atLineStart) {
              nl();
          }

          // Nach package- und import-Anweisungen eine zusätzliche Leerzeile einfügen
          String text = child.getText();
          if (text.startsWith("package") || text.startsWith("import")) {
              nl();
          }
      }

    return null;
  }

  @Override
  public Void visitClassBody(MiniJavaParser.ClassBodyContext ctx) {
    // TODO:
    // Format the contents of a class body:
    // - opening and closing brace,
    // - one member declaration per line,
    // - members indented relative to the class.

      // Klassenrumpf formatieren: öffnende Klammer, eingerückte Member, schließende Klammer
      for (int i = 0; i < ctx.getChildCount(); i++) {
          ParseTree child = ctx.getChild(i);

          if (child instanceof TerminalNode terminal) {
              int type = terminal.getSymbol().getType();

              if (type == MiniJavaLexer.LBRACE) {

                  // Nach einer öffnenden geschweiften Klammer folgt ein Zeilenumbruch
                  write("{");
                  nl();
                  currentIndent++;
              } else if (type == MiniJavaLexer.RBRACE) {

                  // Die schließende Klammer wird wieder eine Ebene weniger eingerückt
                  currentIndent--;
                  write("}");
              } else {

                  // Member Deklaration besuchen und anschließend auf eine neue Zeile wechseln
                  visit(child);
              }
          } else {
              visit(child);

              if (!atLineStart) {
                  nl();
              }
          }
      }

    return null;
  }

  @Override
  public Void visitBlock(MiniJavaParser.BlockContext ctx) {
    // TODO:
    // Format a block:
    // - opening and closing brace,
    // - one blockStatement per line,
    // - nested blocks indented further.

      // Allgemeine Blöcke formatieren, z.B. Methodenrumpf, if-Block oder while-Block
      for (int i = 0; i < ctx.getChildCount(); i++) {
          ParseTree child = ctx.getChild(i);

          if (child instanceof TerminalNode terminal) {
              int type = terminal.getSymbol().getType();

              if (type == MiniJavaLexer.LBRACE) {
                  // Inhalt eines Blocks beginnt in der nächsten Zeile und eine Ebene tiefer
                  write("{");
                  nl();
                  currentIndent++;
              } else if (type == MiniJavaLexer.RBRACE) {
                  // Vor der schließenden Klammer Einrückung wieder reduzieren
                  currentIndent--;
                  write("}");
              } else {
                  visit(child);
              }
          } else {
              // Blockinhalt ausgeben, z.B. Statements oder lokale Deklarationen
              visit(child);

              if (!atLineStart) {
                  nl();
              }
          }
      }

      return null;
  }

  @Override
  public Void visitStatement(MiniJavaParser.StatementContext ctx) {
    // TODO:
    // Ensure that each statement (if/while/return/block/...) ends up
    // on exactly one line, with proper indentation for nested statements.

      // Statement normal über seine Kinder ausgeben
      visitChildren(ctx);

      // Statements mit Semikolon sollen auf einer eigenen Zeile enden
      String text = ctx.getText();
      if (text.endsWith(";") && !atLineStart) {
          nl();
      }

      return null;
  }

  // ---------------- helper methods ----------------

  private void indent() {
    if (atLineStart) {
      out.repeat(" ", Math.max(0, indentWidth * currentIndent));
      atLineStart = false;
    }
  }

  private void write(String s) {
    if (s == null || s.isEmpty()) return;
    indent();
    out.append(s);
  }

  private void nl() {
    out.append('\n');
    atLineStart = true;
    lastToken = null; // Reset spacing context at the beginning of a line
  }

  private void writeln(String s) {
    write(s);
    nl();
  }

  // --------------- token output + basic spacing ---------------

  @Override
  public Void visitTerminal(TerminalNode node) {
    Token t = node.getSymbol();
    String text = t.getText();

    if (lastToken != null) {
      int prevType = lastToken.getType();
      int curType = t.getType();

      // Simple heuristic: insert a space between "word-like" tokens
      if (needsSpaceBetween(prevType, curType)) write(" ");
    }

    write(text);
    lastToken = t;
    return null;
  }

  private boolean needsSpaceBetween(int prevType, int curType) {
    return isWordLike(prevType) && isWordLike(curType);
  }

  private boolean isWordLike(int type) {
    return type == MiniJavaLexer.IDENTIFIER
        || type == MiniJavaLexer.STRING_LITERAL
        || type == MiniJavaLexer.CHAR_LITERAL
        || type == MiniJavaLexer.NULL
        || type == MiniJavaLexer.PACKAGE
        || type == MiniJavaLexer.IMPORT
        || type == MiniJavaLexer.CLASS
        || type == MiniJavaLexer.PUBLIC
        || type == MiniJavaLexer.PRIVATE
        || type == MiniJavaLexer.FINAL
        || type == MiniJavaLexer.RETURN
        || type == MiniJavaLexer.NEW
        || type == MiniJavaLexer.IF
        || type == MiniJavaLexer.ELSE
        || type == MiniJavaLexer.WHILE
        || type == MiniJavaLexer.EXTENDS
        || type == MiniJavaLexer.IMPLEMENTS;
  }
}
