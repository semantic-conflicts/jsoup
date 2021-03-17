package org.jsoup.helper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;
/** 
 * Internal static utilities for handling data.
 */
public class DataUtil {
  public static Pattern charsetPattern=Pattern.compile("(?i)\\bcharset=\\s*(?:\"|')?([^\\s,;\"']*)");
  public static String defaultCharset="UTF-8";
  public static int bufferSize=0x20000;
  public DataUtil(){
  }
  /** 
 * Loads a file to a Document.
 * @param in file to load
 * @param charsetName character set of input
 * @param baseUri base URI of document, to resolve relative links against
 * @return Document
 * @throws IOException on IO error
 */
  public static Document load(  File in,  String charsetName,  String baseUri) throws IOException {
    ByteBuffer byteData=readFileToByteBuffer(in);
    return parseByteData(byteData,charsetName,baseUri,Parser.htmlParser());
  }
  /** 
 * Parses a Document from an input steam.
 * @param in input stream to parse. You will need to close it.
 * @param charsetName character set of input
 * @param baseUri base URI of document, to resolve relative links against
 * @return Document
 * @throws IOException on IO error
 */
  public static Document load(  InputStream in,  String charsetName,  String baseUri) throws IOException {
    ByteBuffer byteData=readToByteBuffer(in);
    return parseByteData(byteData,charsetName,baseUri,Parser.htmlParser());
  }
  /** 
 * Parses a Document from an input steam, using the provided Parser.
 * @param in input stream to parse. You will need to close it.
 * @param charsetName character set of input
 * @param baseUri base URI of document, to resolve relative links against
 * @param parser alternate {@link Parser#xmlParser() parser} to use.
 * @return Document
 * @throws IOException on IO error
 */
  public static Document load(  InputStream in,  String charsetName,  String baseUri,  Parser parser) throws IOException {
    ByteBuffer byteData=readToByteBuffer(in);
    return parseByteData(byteData,charsetName,baseUri,parser);
  }
  public static Document parseByteData(  ByteBuffer byteData,  String charsetName,  String baseUri,  Parser parser){
    String docData;
    Document doc=null;
    if (charsetName == null) {
      docData=Charset.forName(defaultCharset).decode(byteData).toString();
      doc=parser.parseInput(docData,baseUri);
      Element meta=doc.select("meta[http-equiv=content-type], meta[charset]").first();
      if (meta != null) {
        String foundCharset;
        if (meta.hasAttr("http-equiv")) {
          foundCharset=getCharsetFromContentType(meta.attr("content"));
          if (foundCharset == null && meta.hasAttr("charset")) {
            try {
              if (Charset.isSupported(meta.attr("charset"))) {
                foundCharset=meta.attr("charset");
              }
            }
 catch (            IllegalCharsetNameException e) {
              foundCharset=null;
            }
          }
        }
 else {
          foundCharset=meta.attr("charset");
        }
        if (foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)) {
          foundCharset=foundCharset.trim().replaceAll("[\"']","");
          charsetName=foundCharset;
          byteData.rewind();
          docData=Charset.forName(foundCharset).decode(byteData).toString();
          doc=null;
        }
      }
    }
 else {
      Validate.notEmpty(charsetName,"Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
      docData=Charset.forName(charsetName).decode(byteData).toString();
    }
    if (docData.length() > 0 && docData.charAt(0) == 65279) {
      byteData.rewind();
      docData=Charset.forName(defaultCharset).decode(byteData).toString();
      docData=docData.substring(1);
      charsetName=defaultCharset;
      doc=null;
    }
    if (doc == null) {
      doc=parser.parseInput(docData,baseUri);
      doc.outputSettings().charset(charsetName);
    }
    return doc;
  }
  /** 
 * Read the input stream into a byte buffer.
 * @param inStream the input stream to read from
 * @param maxSize the maximum size in bytes to read from the stream. Set to 0 to be unlimited.
 * @return the filled byte buffer
 * @throws IOException if an exception occurs whilst reading from the input stream.
 */
  public static ByteBuffer readToByteBuffer(  InputStream inStream,  int maxSize) throws IOException {
    Validate.isTrue(maxSize >= 0,"maxSize must be 0 (unlimited) or larger");
    final boolean capped=maxSize > 0;
    byte[] buffer=new byte[bufferSize];
    ByteArrayOutputStream outStream=new ByteArrayOutputStream(bufferSize);
    int read;
    int remaining=maxSize;
    while (true) {
      read=inStream.read(buffer);
      if (read == -1)       break;
      if (capped) {
        if (read > remaining) {
          outStream.write(buffer,0,remaining);
          break;
        }
        remaining-=read;
      }
      outStream.write(buffer,0,read);
    }
    ByteBuffer byteData=ByteBuffer.wrap(outStream.toByteArray());
    return byteData;
  }
  public static ByteBuffer readToByteBuffer(  InputStream inStream) throws IOException {
    return readToByteBuffer(inStream,0);
  }
  public static ByteBuffer readFileToByteBuffer(  File file) throws IOException {
    RandomAccessFile randomAccessFile=null;
    try {
      randomAccessFile=new RandomAccessFile(file,"r");
      byte[] bytes=new byte[(int)randomAccessFile.length()];
      randomAccessFile.readFully(bytes);
      return ByteBuffer.wrap(bytes);
    }
  finally {
      if (randomAccessFile != null)       randomAccessFile.close();
    }
  }
  /** 
 * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default will kick in.)
 * @param contentType e.g. "text/html; charset=EUC-JP"
 * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
 */
  public static String getCharsetFromContentType(  String contentType){
    if (contentType == null)     return null;
    Matcher m=charsetPattern.matcher(contentType);
    if (m.find()) {
      String charset=m.group(1).trim();
      charset=charset.replace("charset=","");
      if (charset.length() == 0)       return null;
      try {
        if (Charset.isSupported(charset))         return charset;
        charset=charset.toUpperCase(Locale.ENGLISH);
        if (Charset.isSupported(charset))         return charset;
      }
 catch (      IllegalCharsetNameException e) {
        return null;
      }
    }
    return null;
  }
}
