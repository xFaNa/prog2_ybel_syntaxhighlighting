package highlighting.antlr;

import java.util.Scanner;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class PrettyPrinterDemo {

    public static void main(String[] args) {

        // Benutzer nach der gewünschten Einrückungsbreite fragen
        Scanner scanner = new Scanner(System.in);

        System.out.print("Einrückung (z.B. 2, 4 oder 8 Leerzeichen): ");
        int indentWidth = scanner.nextInt();

        // Beispielcode für die Demonstration des Pretty Printers
        String code = """
                package demo;

                public class Test{
                private final String name="Jan";

                public void run(){
                if(name==null){
                return;
                }

                while(name!=null){
                name=name;
                }
                }
                }
                """;

        // Eingabetext in einen ANTLR CharStream umwandeln
        CharStream input = CharStreams.fromString(code);

        // Lexer erzeugen und den Eingabetext in Tokens zerlegen
        MiniJavaLexer lexer = new MiniJavaLexer(input);

        // Token-Stream für den Parser erzeugen
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Parser erstellen
        MiniJavaParser parser = new MiniJavaParser(tokens);

        // Parse Tree der Compilation Unit erzeugen
        MiniJavaParser.CompilationUnitContext tree = parser.compilationUnit();

        // Pretty Printer mit der gewählten Einrückungsbreite erzeugen
        PrettyPrinterVisitor visitor = new PrettyPrinterVisitor(indentWidth);

        // Parse Tree traversieren und formatierten Code erzeugen
        visitor.visit(tree);

        // Ergebnis auf der Konsole ausgeben
        System.out.println("\n===== Pretty Printed Code =====\n");
        System.out.println(visitor.result());
    }
}
