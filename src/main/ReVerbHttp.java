package main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


/* For representing a sentence that is annotated with pos tags and np chunks.*/
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/* String -> ChunkedSentence */
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;

/* The class that is responsible for extraction. */
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;

/* The class that is responsible for assigning a confidence score to an
 * extraction.
 */
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;

/* A class for holding a (arg1, rel, arg2) triple. */
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;


public class ReVerbHttp {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);
        HttpContext context=server.createContext("/", new MyHandler());
        context.getFilters().add(new ParameterFilter());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
    	
    	public MyHandler() throws ConfidenceFunctionException, IOException
    	{
    		super();
            chunker = new OpenNlpSentenceChunker();
            reverb = new ReVerbExtractor(0,true,true,true);
            confFunc = new ReVerbOpenNlpConfFunction();

    	}
    	
        public void handle(HttpExchange t) throws IOException {
        	Map params = (Map)t.getAttribute("parameters");
            String response = reverb(params.get("text").toString());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private String reverb(String sentence) throws IOException
        {
        	String sentStr = sentence;

            ChunkedSentence sent = chunker.chunkSentence(sentStr);

           String ret="";
            for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
                ret+=extr.getArgument1()+"\t"+extr.getRelation()+"\t"+extr.getArgument2()+"\n";
            }
            return ret;
        }
        private OpenNlpSentenceChunker chunker;
        private ReVerbExtractor reverb;
        private ConfidenceFunction confFunc;
    }

}