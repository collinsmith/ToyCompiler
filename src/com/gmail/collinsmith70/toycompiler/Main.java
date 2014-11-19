package com.gmail.collinsmith70.toycompiler;

import com.gmail.collinsmith70.toycompiler.lexer.Scanner;
import com.gmail.collinsmith70.toycompiler.lexer.Token;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.TokenType;
import com.gmail.collinsmith70.toycompiler.lexer.TokenTypes;
import com.gmail.collinsmith70.toycompiler.lexer.ToyScanner;
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

	public static void main(String[] args) {
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
				Token next;
				while (tokenStream.hasNext()) {
					next = tokenStream.next();
					if (next.getTokenType() == TokenType.DefaultTokenType._eol) {
						writer.append(String.format("%n"));
						continue;
					} else if (next.getTokenType() == TokenTypes._booleanliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._booleanliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._characterliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._characterliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._doubleliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._doubleliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._integerliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._integerliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._nullliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._nullliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._stringliteral) {
						writer.append(String.format("%s(%s)", TokenTypes._stringliteral, next.getValue().toString()));
					} else if (next.getTokenType() == TokenTypes._id) {
						writer.append(String.format("%s(%s)", TokenTypes._id, next.getValue().toString()));
					} else {
						writer.append(next.getTokenType().toString());
					}

					writer.append(" ");
				}

				//parser.parse(lexer);
				System.out.format("%s scanned and parsed in %dms%n", fileName, System.currentTimeMillis()-dt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
