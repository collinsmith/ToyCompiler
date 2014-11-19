package com.gmail.collinsmith70.toycompiler;

import com.gmail.collinsmith70.toycompiler.lexer.Scanner;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.ToyScanner;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import com.gmail.collinsmith70.toycompiler.parser.slr.SLRParser;
import com.gmail.collinsmith70.toycompiler.parser.slr.SLRParserGenerator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Main {
	private static final Path OUTPUT_PATH = Paths.get(".", "output");
	private static final Scanner TOY_SCANNER = new ToyScanner();

	private static Parser parser;

	public static void main(String[] args) {
		try {
			SLRParserGenerator parserGenerator = new SLRParserGenerator(Paths.get(".", "res", "toy.cfg.txt"));
			parserGenerator.outputCFG();
			parserGenerator.outputTables();
			parser = new SLRParser(parserGenerator.getGeneratedTables());
		} catch (IOException e) {
			System.out.println("Unable to open toy.cfg.txt");
		}

		Arrays.stream(args)
			.forEach(arg -> {
				try {
					DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(arg));
					for (Path path : files) {
						if (!Files.isReadable(path)) {
							continue;
						}

						compile(path);
					}
				} catch (IOException e) {
					System.out.format("Unreadable source: \"%s\"", arg);
				}
			});
	}

	private static void compile(Path p) {
		String fileName = p.getFileName().toString();
		Path outFile = OUTPUT_PATH.resolve(fileName.substring(0, fileName.lastIndexOf('.')) + ".output.txt");
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedWriter writer = Files.newBufferedWriter(outFile, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader br = Files.newBufferedReader(p, charset)) {
				System.out.format("Analyzing %s...%n", fileName);
				long dt = System.currentTimeMillis();
				TokenStream tokenStream = new TokenStream(TOY_SCANNER, br);
				/*Token next;
				while (tokenStream.hasNext()) {
					next = tokenStream.next();
					if (next.getTokenType() == TokenType.DefaultTokenType._eol) {
						writer.append(String.format("%n"));
						continue;
					} else if (next.getTokenType() == ToyTokenTypes._booleanliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._booleanliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._characterliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._characterliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._doubleliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._doubleliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._integerliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._integerliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._nullliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._nullliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._stringliteral) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._stringliteral, next.getValue().toString()));
					} else if (next.getTokenType() == ToyTokenTypes._id) {
						writer.append(String.format("%s(%s)", ToyTokenTypes._id, next.getValue().toString()));
					} else {
						writer.append(next.getTokenType().toString());
					}

					writer.append(" ");
				}*/

				parser.parse(tokenStream, writer);
				System.out.format("%s scanned and parsed in %dms%n", fileName, System.currentTimeMillis()-dt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
