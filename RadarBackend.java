import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Small persistent REST API for the Student Opportunity Radar demo. */
public class RadarBackend {
    private static final Path STORE = Paths.get(System.getProperty("user.home"), ".student-opportunity-radar-backend.properties");
    private static final String[][] LISTINGS = {
        {"intern-1","Software Engineering Intern","Google","Internships","Paid · 12 weeks","Bengaluru / Hybrid","Nov 12, 2026","Java, Python, Data Structures","true"},
        {"intern-2","Product Design Intern","Adobe","Internships","Paid · 10 weeks","Noida / Hybrid","Nov 18, 2026","Figma, UX Research","false"},
        {"hack-1","Build for Tomorrow","Devfolio","Hackathons","₹2,00,000 prize pool","Online","Nov 08, 2026","AI, Web Development, Java","true"},
        {"work-1","Cloud & AI Bootcamp","AWS Student Community","Workshops","Free · Certificate","Online","Nov 15, 2026","Cloud, Python, AI","true"},
        {"comp-1","Smart India Hackathon","Government of India","Competitions","National · Team event","India","Nov 20, 2026","Java, IoT, Problem Solving","true"},
        {"sch-1","HDFC Bank Parivartan Scholarship","HDFC Bank","Scholarships","Up to ₹75,000","India","Dec 05, 2026","Undergraduate, Financial need","true"},
        {"place-1","Graduate Engineer Trainee","Tata Consultancy Services","Placements","Full-time · 6–9 LPA","India","Nov 28, 2026","Java, SQL, Communication","true"},
        {"intern-3","Data Analyst Intern","Flipkart","Internships","Paid · 6 months","Bengaluru","Nov 10, 2026","SQL, Python, Analytics","false"},
        {"hack-2","ClimateTech Innovation Sprint","Unstop","Hackathons","₹1,00,000 prize pool","Online","Nov 22, 2026","Sustainability, Web Development","false"},
        {"work-2","Resume & Interview Masterclass","NASSCOM FutureSkills","Workshops","Free · Live session","Online","Nov 06, 2026","Communication, Interview Prep","true"}
    };
    private static final Properties db = new Properties();
    public static void main(String[] args) throws Exception {
        load(); HttpServer s=HttpServer.create(new InetSocketAddress("0.0.0.0", Integer.parseInt(System.getenv().getOrDefault("PORT","8080"))),0);
        s.createContext("/api/", RadarBackend::handle); s.createContext("/", ex -> { byte[] b=Files.readAllBytes(Paths.get("index.html")); ex.getResponseHeaders().set("Content-Type","text/html; charset=utf-8"); ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.close(); });
        s.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); s.start(); System.out.println("Radar backend ready: http://localhost:"+s.getAddress().getPort());
    }
    private static synchronized void handle(HttpExchange x) throws IOException {
        headers(x); String path=x.getRequestURI().getPath(), method=x.getRequestMethod();
        if(method.equals("OPTIONS")){x.sendResponseHeaders(204,-1);return;}
        if(path.equals("/api/health")){send(x,200,"{\"ok\":true,\"service\":\"Student Opportunity Radar API\"}");return;}
        if(path.equals("/api/opportunities")&&method.equals("GET")){StringBuilder b=new StringBuilder("[");for(int i=0;i<LISTINGS.length;i++){if(i>0)b.append(',');String[] a=LISTINGS[i];b.append("{\"id\":").append(q(a[0])).append(",\"title\":").append(q(a[1])).append(",\"organization\":").append(q(a[2])).append(",\"category\":").append(q(a[3])).append(",\"type\":").append(q(a[4])).append(",\"location\":").append(q(a[5])).append(",\"deadline\":").append(q(a[6])).append(",\"skills\":").append(q(a[7])).append(",\"eligible\":").append(a[8]).append('}');}send(x,200,b.append(']').toString());return;}
        if(path.equals("/api/me")&&method.equals("GET")){send(x,200,"{\"saved\":"+array(db.getProperty("saved",""))+",\"applications\":"+map()+"}");return;}
        if((path.equals("/api/saved")||path.equals("/api/applications"))&&method.equals("POST")){String body=new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);String id=field(body,"id"),value=field(body,path.endsWith("saved")?"saved":"status");if(id.isBlank()){send(x,400,"{\"error\":\"id is required\"}");return;}if(path.endsWith("saved")){Set<String> ids=new TreeSet<>(Arrays.asList(db.getProperty("saved","").split(",")));ids.remove("");if(Boolean.parseBoolean(value))ids.add(id);else ids.remove(id);db.setProperty("saved",String.join(",",ids));}else{String k="status."+id;if(value.isBlank()||value.equals("Not applied"))db.remove(k);else db.setProperty(k,value);}persist();send(x,200,"{\"ok\":true}");return;}
        send(x,404,"{\"error\":\"not found\"}");
    }
    private static String map(){StringBuilder b=new StringBuilder("{");boolean first=true;for(String k:db.stringPropertyNames())if(k.startsWith("status.")){if(!first)b.append(',');first=false;b.append(q(k.substring(7))).append(':').append(q(db.getProperty(k)));}return b.append('}').toString();}
    private static String array(String s){StringBuilder b=new StringBuilder("[");boolean first=true;for(String id:s.split(","))if(!id.isBlank()){if(!first)b.append(',');first=false;b.append(q(id));}return b.append(']').toString();}
    private static String field(String s,String name){java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\\""+name+"\\\"\\s*:\\s*(?:\\\"((?:\\\\.|[^\\\"])*)\\\"|(true|false|null))").matcher(s);if(!m.find())return "";return m.group(1)!=null?m.group(1).replace("\\\"","\"").replace("\\\\","\\"):m.group(2);}
    private static String q(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";}
    private static void headers(HttpExchange x){x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");x.getResponseHeaders().set("Access-Control-Allow-Origin","*");x.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, OPTIONS");x.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type");}
    private static void send(HttpExchange x,int code,String body)throws IOException{byte[] b=body.getBytes(StandardCharsets.UTF_8);x.sendResponseHeaders(code,b.length);x.getResponseBody().write(b);x.close();}
    private static synchronized void load(){try(InputStream in=Files.newInputStream(STORE)){db.load(in);}catch(IOException ignored){}}
    private static synchronized void persist(){try(OutputStream out=Files.newOutputStream(STORE)){db.store(out,"Student Opportunity Radar backend data");}catch(IOException e){throw new UncheckedIOException(e);}}
}
