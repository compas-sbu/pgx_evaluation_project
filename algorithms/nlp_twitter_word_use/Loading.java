package oracle.pgx.demos;
import java.util.*;
import java.io.PrintWriter;
import oracle.pgx.api.CompiledProgram;
import oracle.pgx.api.Pgx;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.filter.*;
import oracle.pgx.api.PgxSession;
import oracle.pgx.api.*;
import oracle.pgx.common.types.PropertyType;
import oracle.pgx.api.internal.AnalysisResult;
import java.io.*;


public class Loading {

	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		PgxSession session = Pgx.createSession("my-session");
		PgxGraph pre_graph = session.readGraphWithProperties(args[0]); 
		long endTime1 = System.nanoTime();
		GraphChangeSet<Integer> changeSet = pre_graph.createChangeSet();
		
		System.out.println("nodes = "+pre_graph.getNumVertices());
		int num_vertices = 106220;
		int num_words = Integer.parseInt(args[1]);
		for(int i = 0; i < 133941; i++) {
			if(!pre_graph.hasVertex(i))
				changeSet.addVertex(i);
		}
		PgxGraph graph = changeSet.build();
		
		VertexProperty <Integer, Integer> vid = graph.createVertexProperty(PropertyType.INTEGER, "node_id");
		//TODO: node property - friends_count
		VertexProperty <Integer, Integer> deg = graph.getVertexProperty("friends_count");
		
		//TODO: node property - isWord
		VertexProperty <Integer, Boolean> is_word = graph.getVertexProperty("isWord");
		for(int i = 0; i < graph.getNumVertices(); i++)
			vid.set(graph.getVertex(i), i);
	
		//TODO: edge property - topic_0, topic_1, topic_2
		EdgeProperty<Double> topic_0 = graph.getEdgeProperty("topic_0");
		EdgeProperty<Double> topic_1 = graph.getEdgeProperty("topic_1");
		EdgeProperty<Double> topic_2 = graph.getEdgeProperty("topic_2");

		VertexProperty <Integer, PgxVect<Integer>> rnbrs = graph.createVertexVectorProperty(PropertyType.INTEGER, 400);
		VertexProperty <Integer, Integer> rnbrs_len = graph.createVertexProperty(PropertyType.INTEGER);
		
		VertexSet<Integer> r_nodes = graph.createVertexSet();
		//for(int i = 0; i < graph.getNumVertices(); i++)
		for(int i = 0; i < graph.getNumVertices(); i++)
			rnbrs_len.set(graph.getVertex(i), 0);
		try {
			BufferedReader in = new BufferedReader(new FileReader(args[2])); // random_nbrs.csv
			
			String str;
			while ((str = in.readLine()) != null){
				
				String[] nbrs_list = str.split(" ");
				PgxVertex<Integer> v = graph.getVertex(Integer.parseInt(nbrs_list[0]));
				r_nodes.add(v);
				PgxVect tmp = (PgxVect)rnbrs.get(v);
				int i = 1;
				
				for (i = 1; i< nbrs_list.length; i++) 
					tmp.set(Integer.valueOf(i-1), Integer.parseInt(nbrs_list[i]));
				
				rnbrs_len.set(v, i-1);
				
				for (; i< 400; i++)
					tmp.set(i-1, -1);
				
				rnbrs.set(v, tmp);
			}
			in.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	//	for(int i = 1; i < graph.getNumVertices(); i++)
	//		System.out.println(rnbrs_len.get(graph.getVertex(i)));
		long endTime2 = System.nanoTime();

		System.out.println("time to read graph = "+((endTime1-startTime)/1000000));
		System.out.println("time to initialize random nbrs = "+((endTime2-endTime1)/1000000));
		System.out.println("GM code...");
		
		CompiledProgram p = session.compileProgram("./lang_change.gm");
		AnalysisResult<Integer> result = p.run(graph, r_nodes, vid, is_word, deg, rnbrs, rnbrs_len, topic_0, topic_1, topic_2);
		long endTime3 = System.nanoTime();
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File(args[3]), true));
		pw.append(num_words+"- "+"\n");
		pw.append("load time = "+((endTime2-startTime)/1000000)+"\n");
		pw.append("return value = " + result.getReturnValue() +" (took " + result.getExecutionTimeMs() + "ms)\n\n");
		pw.close();
		System.out.println("return value = " + result.getReturnValue() +" (took " + result.getExecutionTimeMs() + "ms)");
		return;
	}

}
