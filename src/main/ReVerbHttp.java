package main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import com.google.common.base.Joiner;
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
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
/* A class for holding a (arg1, rel, arg2) triple. */
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.normalization.NormalizedBinaryExtraction;
import edu.washington.cs.knowitall.normalization.BinaryExtractionNormalizer;


public class ReVerbHttp {

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);
        HttpContext context=server.createContext("/", new MyHandler());
        context.getFilters().add(new ParameterFilter());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Running");
    }

    static class MyHandler implements HttpHandler {
    	
    	public MyHandler() throws ConfidenceFunctionException, IOException
    	{
    		super();
            chunker = new OpenNlpSentenceChunker();
            extractor = new ReVerbExtractor(0,true,true,true);
            normalizer = new BinaryExtractionNormalizer();
    	}
    	
        public void handle(HttpExchange t) throws IOException {
        	@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>)t.getAttribute("parameters");
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
           for (ChunkedBinaryExtraction extr : extractor.extract(sent))
           {
                NormalizedBinaryExtraction extrNorm = normalizer
                        .normalize(extr);
                String arg1Norm = extrNorm.getArgument1Norm().toString();
                String relNorm = extrNorm.getRelationNorm().toString();
                String arg2Norm = extrNorm.getArgument2Norm().toString();
                ret+=Joiner.on("\t").join(extr.getArgument1(),extr.getRelation(),
                		extr.getArgument2(),arg1Norm,relNorm,arg2Norm)+"\n";
            }
            return ret;
            
        }
        private OpenNlpSentenceChunker chunker;
        private ReVerbExtractor extractor;
        private BinaryExtractionNormalizer normalizer;
    }

}