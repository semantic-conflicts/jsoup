package org.jsoup.helper;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TokenQueue;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
/** 
 * Implementation of  {@link Connection}.
 * @see org.jsoup.Jsoup#connect(String)
 */
public class HttpConnection implements Connection {
  public static String CONTENT_ENCODING="Content-Encoding";
  public static String CONTENT_TYPE="Content-Type";
  public static String MULTIPART_FORM_DATA="multipart/form-data";
  public static String FORM_URL_ENCODED="application/x-www-form-urlencoded";
  public static Connection connect(  String url){
    Connection con=new HttpConnection();
    con.url(url);
    return con;
  }
  public static Connection connect(  URL url){
    Connection con=new HttpConnection();
    con.url(url);
    return con;
  }
  public static String encodeUrl(  String url){
    if (url == null)     return null;
    return url.replaceAll(" ","%20");
  }
  public static String encodeMimeName(  String val){
    if (val == null)     return null;
    return val.replaceAll("\"","%22");
  }
  public Connection.Request req;
  public Connection.Response res;
  public HttpConnection(){
    req=new Request();
    res=new Response();
  }
  public Connection url(  URL url){
    req.url(url);
    return this;
  }
  public Connection url(  String url){
    Validate.notEmpty(url,"Must supply a valid URL");
    try {
      req.url(new URL(encodeUrl(url)));
    }
 catch (    MalformedURLException e) {
      throw new IllegalArgumentException("Malformed URL: " + url,e);
    }
    return this;
  }
  public Connection proxy(  Proxy proxy){
    req.proxy(proxy);
    return this;
  }
  public Connection proxy(  String host,  int port){
    req.proxy(host,port);
    return this;
  }
  public Connection userAgent(  String userAgent){
    Validate.notNull(userAgent,"User agent must not be null");
    req.header("User-Agent",userAgent);
    return this;
  }
  public Connection timeout(  int millis){
    req.timeout(millis);
    return this;
  }
  public Connection maxBodySize(  int bytes){
    req.maxBodySize(bytes);
    return this;
  }
  public Connection followRedirects(  boolean followRedirects){
    req.followRedirects(followRedirects);
    return this;
  }
  public Connection referrer(  String referrer){
    Validate.notNull(referrer,"Referrer must not be null");
    req.header("Referer",referrer);
    return this;
  }
  public Connection method(  Method method){
    req.method(method);
    return this;
  }
  public Connection ignoreHttpErrors(  boolean ignoreHttpErrors){
    req.ignoreHttpErrors(ignoreHttpErrors);
    return this;
  }
  public Connection ignoreContentType(  boolean ignoreContentType){
    req.ignoreContentType(ignoreContentType);
    return this;
  }
  public Connection validateTLSCertificates(  boolean value){
    req.validateTLSCertificates(value);
    return this;
  }
  public Connection data(  String key,  String value){
    req.data(KeyVal.create(key,value));
    return this;
  }
  public Connection data(  String key,  String filename,  InputStream inputStream){
    req.data(KeyVal.create(key,filename,inputStream));
    return this;
  }
  public Connection data(  Map<String,String> data){
    Validate.notNull(data,"Data map must not be null");
    for (    Map.Entry<String,String> entry : data.entrySet()) {
      req.data(KeyVal.create(entry.getKey(),entry.getValue()));
    }
    return this;
  }
  public Connection data(  String... keyvals){
    Validate.notNull(keyvals,"Data key value pairs must not be null");
    Validate.isTrue(keyvals.length % 2 == 0,"Must supply an even number of key value pairs");
    for (int i=0; i < keyvals.length; i+=2) {
      String key=keyvals[i];
      String value=keyvals[i + 1];
      Validate.notEmpty(key,"Data key must not be empty");
      Validate.notNull(value,"Data value must not be null");
      req.data(KeyVal.create(key,value));
    }
    return this;
  }
  public Connection data(  Collection<Connection.KeyVal> data){
    Validate.notNull(data,"Data collection must not be null");
    for (    Connection.KeyVal entry : data) {
      req.data(entry);
    }
    return this;
  }
  public Connection header(  String name,  String value){
    req.header(name,value);
    return this;
  }
  public Connection cookie(  String name,  String value){
    req.cookie(name,value);
    return this;
  }
  public Connection cookies(  Map<String,String> cookies){
    Validate.notNull(cookies,"Cookie map must not be null");
    for (    Map.Entry<String,String> entry : cookies.entrySet()) {
      req.cookie(entry.getKey(),entry.getValue());
    }
    return this;
  }
  public Connection parser(  Parser parser){
    req.parser(parser);
    return this;
  }
  public Document get() throws IOException {
    req.method(Method.GET);
    execute();
    return res.parse();
  }
  public Document post() throws IOException {
    req.method(Method.POST);
    execute();
    return res.parse();
  }
  public Connection.Response execute() throws IOException {
    res=Response.execute(req);
    return res;
  }
  public Connection.Request request(){
    return req;
  }
  public Connection request(  Connection.Request request){
    req=request;
    return this;
  }
  public Connection.Response response(){
    return res;
  }
  public Connection response(  Connection.Response response){
    res=response;
    return this;
  }
  public Connection postDataCharset(  String charset){
    req.postDataCharset(charset);
    return this;
  }
@SuppressWarnings({"unchecked"}) public static abstract class Base<T extends Connection.Base> implements Connection.Base<T> {
    URL url;
    Method method;
    Map<String,String> headers;
    Map<String,String> cookies;
    public Base(){
      headers=new LinkedHashMap<String,String>();
      cookies=new LinkedHashMap<String,String>();
    }
    public URL url(){
      return url;
    }
    public T url(    URL url){
      Validate.notNull(url,"URL must not be null");
      this.url=url;
      return (T)this;
    }
    public Method method(){
      return method;
    }
    public T method(    Method method){
      Validate.notNull(method,"Method must not be null");
      this.method=method;
      return (T)this;
    }
    public String header(    String name){
      Validate.notNull(name,"Header name must not be null");
      return getHeaderCaseInsensitive(name);
    }
    public T header(    String name,    String value){
      Validate.notEmpty(name,"Header name must not be empty");
      Validate.notNull(value,"Header value must not be null");
      removeHeader(name);
      headers.put(name,value);
      return (T)this;
    }
    public boolean hasHeader(    String name){
      Validate.notEmpty(name,"Header name must not be empty");
      return getHeaderCaseInsensitive(name) != null;
    }
    /** 
 * Test if the request has a header with this value (case insensitive).
 */
    public boolean hasHeaderWithValue(    String name,    String value){
      return hasHeader(name) && header(name).equalsIgnoreCase(value);
    }
    public T removeHeader(    String name){
      Validate.notEmpty(name,"Header name must not be empty");
      Map.Entry<String,String> entry=scanHeaders(name);
      if (entry != null)       headers.remove(entry.getKey());
      return (T)this;
    }
    public Map<String,String> headers(){
      return headers;
    }
    public String getHeaderCaseInsensitive(    String name){
      Validate.notNull(name,"Header name must not be null");
      String value=headers.get(name);
      if (value == null)       value=headers.get(name.toLowerCase());
      if (value == null) {
        Map.Entry<String,String> entry=scanHeaders(name);
        if (entry != null)         value=entry.getValue();
      }
      return value;
    }
    public Map.Entry<String,String> scanHeaders(    String name){
      String lc=name.toLowerCase();
      for (      Map.Entry<String,String> entry : headers.entrySet()) {
        if (entry.getKey().toLowerCase().equals(lc))         return entry;
      }
      return null;
    }
    public String cookie(    String name){
      Validate.notEmpty(name,"Cookie name must not be empty");
      return cookies.get(name);
    }
    public T cookie(    String name,    String value){
      Validate.notEmpty(name,"Cookie name must not be empty");
      Validate.notNull(value,"Cookie value must not be null");
      cookies.put(name,value);
      return (T)this;
    }
    public boolean hasCookie(    String name){
      Validate.notEmpty(name,"Cookie name must not be empty");
      return cookies.containsKey(name);
    }
    public T removeCookie(    String name){
      Validate.notEmpty(name,"Cookie name must not be empty");
      cookies.remove(name);
      return (T)this;
    }
    public Map<String,String> cookies(){
      return cookies;
    }
  }
public static class Request extends HttpConnection.Base<Connection.Request> implements Connection.Request {
    public Proxy proxy;
    public int timeoutMilliseconds;
    public int maxBodySizeBytes;
    public boolean followRedirects;
    public Collection<Connection.KeyVal> data;
    public boolean ignoreHttpErrors=false;
    public boolean ignoreContentType=false;
    public Parser parser;
    public boolean parserDefined=false;
    public boolean validateTSLCertificates=true;
    public String postDataCharset=DataUtil.defaultCharset;
    public Request(){
      timeoutMilliseconds=3000;
      maxBodySizeBytes=1024 * 1024;
      followRedirects=true;
      data=new ArrayList<Connection.KeyVal>();
      method=Method.GET;
      headers.put("Accept-Encoding","gzip");
      parser=Parser.htmlParser();
    }
    public Proxy proxy(){
      return proxy;
    }
    public Request proxy(    Proxy proxy){
      this.proxy=proxy;
      return this;
    }
    public Request proxy(    String host,    int port){
      this.proxy=new Proxy(Proxy.Type.HTTP,InetSocketAddress.createUnresolved(host,port));
      return this;
    }
    public int timeout(){
      return timeoutMilliseconds;
    }
    public Request timeout(    int millis){
      Validate.isTrue(millis >= 0,"Timeout milliseconds must be 0 (infinite) or greater");
      timeoutMilliseconds=millis;
      return this;
    }
    public int maxBodySize(){
      return maxBodySizeBytes;
    }
    public Connection.Request maxBodySize(    int bytes){
      Validate.isTrue(bytes >= 0,"maxSize must be 0 (unlimited) or larger");
      maxBodySizeBytes=bytes;
      return this;
    }
    public boolean followRedirects(){
      return followRedirects;
    }
    public Connection.Request followRedirects(    boolean followRedirects){
      this.followRedirects=followRedirects;
      return this;
    }
    public boolean ignoreHttpErrors(){
      return ignoreHttpErrors;
    }
    public boolean validateTLSCertificates(){
      return validateTSLCertificates;
    }
    public void validateTLSCertificates(    boolean value){
      validateTSLCertificates=value;
    }
    public Connection.Request ignoreHttpErrors(    boolean ignoreHttpErrors){
      this.ignoreHttpErrors=ignoreHttpErrors;
      return this;
    }
    public boolean ignoreContentType(){
      return ignoreContentType;
    }
    public Connection.Request ignoreContentType(    boolean ignoreContentType){
      this.ignoreContentType=ignoreContentType;
      return this;
    }
    public Request data(    Connection.KeyVal keyval){
      Validate.notNull(keyval,"Key val must not be null");
      data.add(keyval);
      return this;
    }
    public Collection<Connection.KeyVal> data(){
      return data;
    }
    public Request parser(    Parser parser){
      this.parser=parser;
      parserDefined=true;
      return this;
    }
    public Parser parser(){
      return parser;
    }
    public Connection.Request postDataCharset(    String charset){
      Validate.notNull(charset,"Charset must not be null");
      if (!Charset.isSupported(charset))       throw new IllegalCharsetNameException(charset);
      this.postDataCharset=charset;
      return this;
    }
    public String postDataCharset(){
      return postDataCharset;
    }
  }
public static class Response extends HttpConnection.Base<Connection.Response> implements Connection.Response {
    public static int MAX_REDIRECTS=20;
    public static SSLSocketFactory sslSocketFactory;
    public static String LOCATION="Location";
    public int statusCode;
    public String statusMessage;
    public ByteBuffer byteData;
    public String charset;
    public String contentType;
    public boolean executed=false;
    public int numRedirects=0;
    public Connection.Request req;
    public static Pattern xmlContentTypeRxp=Pattern.compile("(application|text)/\\w*\\+?xml.*");
    Response(){
      super();
    }
    public Response(    Response previousResponse) throws IOException {
      super();
      if (previousResponse != null) {
        numRedirects=previousResponse.numRedirects + 1;
        if (numRedirects >= MAX_REDIRECTS)         throw new IOException(String.format("Too many redirects occurred trying to load URL %s",previousResponse.url()));
      }
    }
    public static Response execute(    Connection.Request req) throws IOException {
      return execute(req,null);
    }
    public static Response execute(    Connection.Request req,    Response previousResponse) throws IOException {
      Validate.notNull(req,"Request must not be null");
      String protocol=req.url().getProtocol();
      if (!protocol.equals("http") && !protocol.equals("https"))       throw new MalformedURLException("Only http & https protocols supported");
      String mimeBoundary=null;
      if (!req.method().hasBody() && req.data().size() > 0) {
        serialiseRequestUrl(req);
      }
 else       if (req.method().hasBody()) {
        mimeBoundary=setOutputContentType(req);
      }
      HttpURLConnection conn=createConnection(req);
      Response res;
      try {
        conn.connect();
        if (conn.getDoOutput())         writePost(req,conn.getOutputStream(),mimeBoundary);
        int status=conn.getResponseCode();
        res=new Response(previousResponse);
        res.setupFromConnection(conn,previousResponse);
        res.req=req;
        if (res.hasHeader(LOCATION) && req.followRedirects()) {
          req.method(Method.GET);
          req.data().clear();
          String location=res.header(LOCATION);
          if (location != null && location.startsWith("http:/") && location.charAt(6) != '/')           location=location.substring(6);
          req.url(StringUtil.resolve(req.url(),encodeUrl(location)));
          for (          Map.Entry<String,String> cookie : res.cookies.entrySet()) {
            req.cookie(cookie.getKey(),cookie.getValue());
          }
          return execute(req,res);
        }
        if ((status < 200 || status >= 400) && !req.ignoreHttpErrors())         throw new HttpStatusException("HTTP error fetching URL",status,req.url().toString());
        String contentType=res.contentType();
        if (contentType != null && !req.ignoreContentType() && !contentType.startsWith("text/") && !xmlContentTypeRxp.matcher(contentType).matches())         throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml",contentType,req.url().toString());
        if (contentType != null && xmlContentTypeRxp.matcher(contentType).matches()) {
          if (req instanceof HttpConnection.Request && !((Request)req).parserDefined) {
            req.parser(Parser.xmlParser());
          }
        }
        res.charset=DataUtil.getCharsetFromContentType(res.contentType);
        if (conn.getContentLength() != 0) {
          InputStream bodyStream=null;
          try {
            bodyStream=conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
            if (res.hasHeaderWithValue(CONTENT_ENCODING,"gzip"))             bodyStream=new GZIPInputStream(bodyStream);
            res.byteData=DataUtil.readToByteBuffer(bodyStream,req.maxBodySize());
          }
  finally {
            if (bodyStream != null)             bodyStream.close();
          }
        }
 else {
          res.byteData=DataUtil.emptyByteBuffer();
        }
      }
  finally {
        conn.disconnect();
      }
      res.executed=true;
      return res;
    }
    public int statusCode(){
      return statusCode;
    }
    public String statusMessage(){
      return statusMessage;
    }
    public String charset(){
      return charset;
    }
    public String contentType(){
      return contentType;
    }
    public Document parse() throws IOException {
      Validate.isTrue(executed,"Request must be executed (with .execute(), .get(), or .post() before parsing response");
      Document doc=DataUtil.parseByteData(byteData,charset,url.toExternalForm(),req.parser());
      byteData.rewind();
      charset=doc.outputSettings().charset().name();
      return doc;
    }
    public String body(){
      Validate.isTrue(executed,"Request must be executed (with .execute(), .get(), or .post() before getting response body");
      String body;
      if (charset == null)       body=Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
 else       body=Charset.forName(charset).decode(byteData).toString();
      byteData.rewind();
      return body;
    }
    public byte[] bodyAsBytes(){
      Validate.isTrue(executed,"Request must be executed (with .execute(), .get(), or .post() before getting response body");
      return byteData.array();
    }
    public static HttpURLConnection createConnection(    Connection.Request req) throws IOException {
      final HttpURLConnection conn=(HttpURLConnection)(req.proxy() == null ? req.url().openConnection() : req.url().openConnection(req.proxy()));
      conn.setRequestMethod(req.method().name());
      conn.setInstanceFollowRedirects(false);
      conn.setConnectTimeout(req.timeout());
      conn.setReadTimeout(req.timeout());
      if (conn instanceof HttpsURLConnection) {
        if (!req.validateTLSCertificates()) {
          initUnSecureTSL();
          ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
          ((HttpsURLConnection)conn).setHostnameVerifier(getInsecureVerifier());
        }
      }
      if (req.method().hasBody())       conn.setDoOutput(true);
      if (req.cookies().size() > 0)       conn.addRequestProperty("Cookie",getRequestCookieString(req));
      for (      Map.Entry<String,String> header : req.headers().entrySet()) {
        conn.addRequestProperty(header.getKey(),header.getValue());
      }
      return conn;
    }
    /** 
 * Instantiate Hostname Verifier that does nothing. This is used for connections with disabled SSL certificates validation.
 * @return Hostname Verifier that does nothing and accepts all hostnames
 */
    public static HostnameVerifier getInsecureVerifier(){
      return new HostnameVerifier(){
        public boolean verify(        String urlHostName,        SSLSession session){
          return true;
        }
      }
;
    }
    /** 
 * Initialise Trust manager that does not validate certificate chains and add it to current SSLContext. <p/> please not that this method will only perform action if sslSocketFactory is not yet instantiated.
 * @throws IOException
 */
    public static synchronized void initUnSecureTSL() throws IOException {
      if (sslSocketFactory == null) {
        final TrustManager[] trustAllCerts=new TrustManager[]{new X509TrustManager(){
          public void checkClientTrusted(          final X509Certificate[] chain,          final String authType){
          }
          public void checkServerTrusted(          final X509Certificate[] chain,          final String authType){
          }
          public X509Certificate[] getAcceptedIssuers(){
            return null;
          }
        }
};
        final SSLContext sslContext;
        try {
          sslContext=SSLContext.getInstance("SSL");
          sslContext.init(null,trustAllCerts,new java.security.SecureRandom());
          sslSocketFactory=sslContext.getSocketFactory();
        }
 catch (        NoSuchAlgorithmException e) {
          throw new IOException("Can't create unsecure trust manager");
        }
catch (        KeyManagementException e) {
          throw new IOException("Can't create unsecure trust manager");
        }
      }
    }
    public void setupFromConnection(    HttpURLConnection conn,    Connection.Response previousResponse) throws IOException {
      method=Method.valueOf(conn.getRequestMethod());
      url=conn.getURL();
      statusCode=conn.getResponseCode();
      statusMessage=conn.getResponseMessage();
      contentType=conn.getContentType();
      Map<String,List<String>> resHeaders=createHeaderMap(conn);
      processResponseHeaders(resHeaders);
      if (previousResponse != null) {
        for (        Map.Entry<String,String> prevCookie : previousResponse.cookies().entrySet()) {
          if (!hasCookie(prevCookie.getKey()))           cookie(prevCookie.getKey(),prevCookie.getValue());
        }
      }
    }
    public static LinkedHashMap<String,List<String>> createHeaderMap(    HttpURLConnection conn){
      final LinkedHashMap<String,List<String>> headers=new LinkedHashMap<String,List<String>>();
      int i=0;
      while (true) {
        final String key=conn.getHeaderFieldKey(i);
        final String val=conn.getHeaderField(i);
        if (key == null && val == null)         break;
        i++;
        if (key == null || val == null)         continue;
        if (headers.containsKey(key))         headers.get(key).add(val);
 else {
          final ArrayList<String> vals=new ArrayList<String>();
          vals.add(val);
          headers.put(key,vals);
        }
      }
      return headers;
    }
    void processResponseHeaders(    Map<String,List<String>> resHeaders){
      for (      Map.Entry<String,List<String>> entry : resHeaders.entrySet()) {
        String name=entry.getKey();
        if (name == null)         continue;
        List<String> values=entry.getValue();
        if (name.equalsIgnoreCase("Set-Cookie")) {
          for (          String value : values) {
            if (value == null)             continue;
            TokenQueue cd=new TokenQueue(value);
            String cookieName=cd.chompTo("=").trim();
            String cookieVal=cd.consumeTo(";").trim();
            if (cookieName.length() > 0)             cookie(cookieName,cookieVal);
          }
        }
 else {
          if (values.size() == 1)           header(name,values.get(0));
 else           if (values.size() > 1) {
            StringBuilder accum=new StringBuilder();
            for (int i=0; i < values.size(); i++) {
              final String val=values.get(i);
              if (i != 0)               accum.append(", ");
              accum.append(val);
            }
            header(name,accum.toString());
          }
        }
      }
    }
    public static String setOutputContentType(    final Connection.Request req){
      boolean needsMulti=false;
      for (      Connection.KeyVal keyVal : req.data()) {
        if (keyVal.hasInputStream()) {
          needsMulti=true;
          break;
        }
      }
      String bound=null;
      if (needsMulti) {
        bound=DataUtil.mimeBoundary();
        req.header(CONTENT_TYPE,MULTIPART_FORM_DATA + "; boundary=" + bound);
      }
 else {
        req.header(CONTENT_TYPE,FORM_URL_ENCODED + "; charset=" + req.postDataCharset());
      }
      return bound;
    }
    public static void writePost(    final Connection.Request req,    final OutputStream outputStream,    final String bound) throws IOException {
      final Collection<Connection.KeyVal> data=req.data();
      final BufferedWriter w=new BufferedWriter(new OutputStreamWriter(outputStream,DataUtil.defaultCharset));
      if (bound != null) {
        for (        Connection.KeyVal keyVal : data) {
          w.write("--");
          w.write(bound);
          w.write("\r\n");
          w.write("Content-Disposition: form-data; name=\"");
          w.write(encodeMimeName(keyVal.key()));
          w.write("\"");
          if (keyVal.hasInputStream()) {
            w.write("; filename=\"");
            w.write(encodeMimeName(keyVal.value()));
            w.write("\"\r\nContent-Type: application/octet-stream\r\n\r\n");
            w.flush();
            DataUtil.crossStreams(keyVal.inputStream(),outputStream);
            outputStream.flush();
          }
 else {
            w.write("\r\n\r\n");
            w.write(keyVal.value());
          }
          w.write("\r\n");
        }
        w.write("--");
        w.write(bound);
        w.write("--");
      }
 else {
        boolean first=true;
        for (        Connection.KeyVal keyVal : data) {
          if (!first)           w.append('&');
 else           first=false;
          w.write(URLEncoder.encode(keyVal.key(),req.postDataCharset()));
          w.write('=');
          w.write(URLEncoder.encode(keyVal.value(),req.postDataCharset()));
        }
      }
      w.close();
    }
    public static String getRequestCookieString(    Connection.Request req){
      StringBuilder sb=new StringBuilder();
      boolean first=true;
      for (      Map.Entry<String,String> cookie : req.cookies().entrySet()) {
        if (!first)         sb.append("; ");
 else         first=false;
        sb.append(cookie.getKey()).append('=').append(cookie.getValue());
      }
      return sb.toString();
    }
    public static void serialiseRequestUrl(    Connection.Request req) throws IOException {
      URL in=req.url();
      StringBuilder url=new StringBuilder();
      boolean first=true;
      url.append(in.getProtocol()).append("://").append(in.getAuthority()).append(in.getPath()).append("?");
      if (in.getQuery() != null) {
        url.append(in.getQuery());
        first=false;
      }
      for (      Connection.KeyVal keyVal : req.data()) {
        if (!first)         url.append('&');
 else         first=false;
        url.append(URLEncoder.encode(keyVal.key(),DataUtil.defaultCharset)).append('=').append(URLEncoder.encode(keyVal.value(),DataUtil.defaultCharset));
      }
      req.url(new URL(url.toString()));
      req.data().clear();
    }
  }
public static class KeyVal implements Connection.KeyVal {
    public String key;
    public String value;
    public InputStream stream;
    public static KeyVal create(    String key,    String value){
      return new KeyVal().key(key).value(value);
    }
    public static KeyVal create(    String key,    String filename,    InputStream stream){
      return new KeyVal().key(key).value(filename).inputStream(stream);
    }
    public KeyVal(){
    }
    public KeyVal key(    String key){
      Validate.notEmpty(key,"Data key must not be empty");
      this.key=key;
      return this;
    }
    public String key(){
      return key;
    }
    public KeyVal value(    String value){
      Validate.notNull(value,"Data value must not be null");
      this.value=value;
      return this;
    }
    public String value(){
      return value;
    }
    public KeyVal inputStream(    InputStream inputStream){
      Validate.notNull(value,"Data input stream must not be null");
      this.stream=inputStream;
      return this;
    }
    public InputStream inputStream(){
      return stream;
    }
    public boolean hasInputStream(){
      return stream != null;
    }
    @Override public String toString(){
      return key + "=" + value;
    }
  }
}
