package com.senzing.io;

import java.io.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import org.mozilla.universalchardet.UniversalDetector;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import static java.nio.file.FileVisitResult.*;

/**
 * Static I/O utility functions.
 */
public class IOUtilities {
  private IOUtilities() {
    // do nothing
  }

  /**
   * Reads the contents of the file as text and returns the {@link String}
   * representing the contents.  The text is expected to be encoded in the
   * specified character encoding.  If the specified character encoding is
   * <tt>null</tt> then the system default encoding is used.
   *
   * @param file The {@link File} whose contents should be read.
   * @param charEncoding The character encoding for the text in the file.
   * @return The {@link String} representing the contents of the file.
   * @throws IOException If an I/O failure occurs.
   */
  public static String readTextFileAsString(File file, String charEncoding)
    throws IOException
  {
    Charset charset = (charEncoding == null) ? Charset.defaultCharset()
                    : Charset.forName(charEncoding);

    try (FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, charset);
         Reader reader = bomSkippingReader(isr, charset.name());
         BufferedReader br = new BufferedReader(reader))
    {
      long size = file.length();
      if (size > Integer.MAX_VALUE) size = Integer.MAX_VALUE;

      StringBuilder sb = new StringBuilder((int) size);
      for (int nextChar = br.read(); nextChar >= 0; nextChar = br.read()) {
        if (nextChar == 0) continue;
        sb.append((char) nextChar);
      }
      return sb.toString();
    }
  }

  /**
   * Reads data from the specified {@link InputStream} assuming the data
   * represents characters and attempts via several methods to guess the
   * character encoding of those characters.  If no character encoding can
   * be determined with confidence then this returns <tt>null</tt>.
   *
   * @param is The {@link InputStream} to read from.
   * @return The name of the character encoding that was guessed.
   * @throws IOException If an I/O failure occurs.
   */
  public static String detectCharacterEncoding(InputStream is)
      throws IOException
  {
    UniversalDetector detector = new UniversalDetector(null);
    int readCount = 0;
    int totalReadCount = 0;
    byte[] buffer = new byte[50];
    int maxReadCount = 10*1024*1024;
    boolean allAscii = true;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while ((totalReadCount < maxReadCount)
           && !detector.isDone()
           && ((readCount = is.read(buffer)) >= 0))
    {
      totalReadCount += readCount;
      detector.handleData(buffer, 0, readCount);
      baos.write(buffer, 0, readCount);
    }
    detector.dataEnd();
    String encoding = detector.getDetectedCharset();
    if (encoding != null) return encoding;

    // check if all all ascii
    byte[] bytes = baos.toByteArray();
    for (byte b : bytes) {
      if (((int)b) > 127) {
        allAscii = false;
        break;
      }
    }

    CharsetDetector cd = new CharsetDetector();
    cd.setText(bytes);
    CharsetMatch[] matches = cd.detectAll();
    if (matches == null) return null;
    if (matches.length == 0) return null;
    CharsetMatch bestMatch = null;
    for (CharsetMatch match : matches) {
      System.out.println(
          "POSSIBLE CHARACTER ENCODING / CONFIDENCE "
              + match.getName() + " / " + match.getConfidence());

      // get the first match as the best candidate
      if (bestMatch == null) {
        bestMatch = match;

        if (match.getName().toUpperCase().startsWith("UTF")) {
          // if it starts with UTF, then we can have no better match
          break;
        } else {
          // if not UTF, then check if the next one has equal confidence
          continue;
        }
      }
      // check confidence of this one versus the best match
      int c1 = bestMatch.getConfidence();
      int c2 = match.getConfidence();

      // if lower confidence then we are done
      if (c2 < c1) break;

      // check the name if this candidate match
      if (match.getName().toUpperCase().startsWith("UTF")) {
        bestMatch = match;
        break;
      }
    }
    if (bestMatch == null && allAscii) return "UTF-8";
    if (bestMatch == null) return null;
    if (bestMatch.getConfidence() < 50 && allAscii) return "UTF-8";
    return bestMatch.getName();
  }

  /**
   * Using the specified character encoding, this method will wraps the
   * specified {@link Reader} in a new {@link Reader} that will skip
   * the "byte order mark" (BOM) character at the beginning of the file for
   * UTF character encodings (e.g.: "UTF-8", "UTF-16" or "UTF-32").  If the
   * specified character encoding is not a "UTF" character encoding then it is
   * simply returned as-is.
   * @param src The source {@link Reader}.
   * @param encoding The character encoding.
   * @return The new {@link Reader} that will skip the byte-order mark.
   * @throws IOException If an I/O failure occurs.
   * @throws NullPointerException If either parameter is <tt>null</tt>.
   */
  public static Reader bomSkippingReader(Reader src, String encoding)
      throws IOException, NullPointerException
  {
    // check if encoding is null (illegal)
    if (encoding == null) {
      throw new NullPointerException(
          "Cannot skip byte order mark without specifying the encoding.");
    }

    // check if we have an encoding that is NOT a UTF encoding
    if (!encoding.toUpperCase().startsWith("UTF")) {
      // if not UTF encoding then there should not be a BOM to skip
      return src;
    }

    // create a pushback reader and peek at the first character
    PushbackReader result = new PushbackReader(src, 1);
    int first = result.read();

    // check if already at EOF
    if (first == -1) {
      // just return the source stream
      return src;
    }

    // check if we do NOT have a byte order mark
    if (first != 0xFEFF) {
      // push the character back on to the stream so it can be read
      result.unread(first);
    }

    // return the pushback reader
    return result;
  }

  /**
   * Creates the specified directory if it does not exist.
   *
   * @param dir The {@link File} representing the directory.
   *
   * @throws IOException If a failure occurs.
   */
  public static void createDirectoryIfMissing(File dir) throws IOException {
    if (!dir.exists()) {
      boolean created = dir.mkdirs();
      if (!created) {
        throw new IOException("Failed to create directory: " + dir);
      }
    }
  }

  /**
   * Recursively deletes the specified directory and returns the number of
   * files in the directory that failed to be deleted.
   *
   * @param dir The {@link File} representing the directory.
   *
   * @return The number of files in the directory that could not be deleted.
   *
   * @throws IOException If a serious failure occurs.
   */
  public static int recursiveDeleteDirectory(File dir) throws IOException {
    int[] failedCount = { 0 };

    Files.walkFileTree(
        dir.toPath(), new FileVisitor<java.nio.file.Path>() {
          public FileVisitResult preVisitDirectory(java.nio.file.Path path, BasicFileAttributes attrs) {
            return CONTINUE;
          }
          public FileVisitResult postVisitDirectory(java.nio.file.Path path, IOException e) {
            if (e != null) return CONTINUE;
            path.toFile().delete();
            return CONTINUE;
          }
          public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) {
            boolean result = path.toFile().delete();
            if (!result) {
              failedCount[0]++;
            }
            return CONTINUE;
          }
          public FileVisitResult visitFileFailed(java.nio.file.Path path, IOException e) {
            e.printStackTrace();
            return CONTINUE;
          }
        });

    try {
      Files.deleteIfExists(dir.toPath());
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
    return failedCount[0];
  }

  /**
   * Checks if two (2) files are different.
   *
   * @param file1 The first file.
   * @param file2 the second file.
   * @return <tt>true</tt> if the files differ, otherwise <tt>false</tt>
   */
  public static boolean checkFilesDiffer(File file1, File file2)
      throws IOException
  {
    return checkFilesDiffer(file1, file2, false);
  }

  /**
   * Checks if two (2) files are different.
   *
   * @param file1 The first file.
   * @param file2 the second file.
   * @param timestampSignificant <tt>true</tt> if timestamps need to be the
   *                             same, otherwise <tt>false</tt>
   * @return <tt>true</tt> if the files differ, otherwise <tt>false</tt>
   */
  public static boolean checkFilesDiffer(File     file1,
                                         File     file2,
                                         boolean  timestampSignificant)
      throws IOException
  {
    if (!file1.exists() && !file2.exists()) return false;
    if (!file1.exists() || !file2.exists()) return true;
    if (file1.length() != file2.length()) return true;

    if (timestampSignificant) {
      if (file1.lastModified() != file2.lastModified()) return true;
    }

    try (FileInputStream fis1 = new FileInputStream(file1);
         FileInputStream fis2 = new FileInputStream(file2);
         BufferedInputStream bis1 = new BufferedInputStream(fis1);
         BufferedInputStream bis2 = new BufferedInputStream(fis2))
    {
      int byte1, byte2;
      do {
        byte1 = bis1.read();
        byte2 = bis2.read();
        if (byte1 != byte2) return true;

      } while (byte1 != -1);

      // if we get here then they are identical
      return false;
    }
  }
}
