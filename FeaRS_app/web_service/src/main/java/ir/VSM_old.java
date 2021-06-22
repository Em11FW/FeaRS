package ir;

import com.google.common.collect.Sets;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

public class VSM_old {

	private static double vsm(Map<String, Double> v1, Map<String, Double> v2) {
		Set<String> both = Sets.newHashSet(v1.keySet());
		both.addAll(v2.keySet());
		double[] d1 = new double[both.size()];
		double[] d2 = new double[both.size()];
		int i = 0;

		for(Iterator var6 = both.iterator(); var6.hasNext(); ++i) {
			String key = (String)var6.next();
			d1[i] = 0.0D;
			d2[i] = 0.0D;
			if (v1.containsKey(key)) {
				d1[i] = (Double)v1.get(key);
			}

			if (v2.containsKey(key)) {
				d2[i] = (Double)v2.get(key);
			}
		}

		RealVector vector1 = new ArrayRealVector(d1);
		ArrayRealVector vector2 = new ArrayRealVector(d2);

		try {
			return vector1.cosine(vector2);
		} catch (MathArithmeticException var9) {
			return 0.0D / 0.0;
		}
	}

	public static double getAndroidSimilarity(String[] tokenizedCodeSnippet1, String[] tokenizedCodeSnippet2, Preprocessing preprocessing) {
		Set<String> first = new HashSet(Arrays.asList(tokenizedCodeSnippet1));
		Set<String> second = new HashSet(Arrays.asList(tokenizedCodeSnippet2));
		Set<String> both = new HashSet(first);
		both.addAll(second);
		double overall = 0.0D;
		double inCommon = 0.0D;
		Iterator var10 = both.iterator();

		while(true) {
			String term;
			do {
				if (!var10.hasNext()) {
					if (overall > 0.0D) {
						return inCommon / overall;
					}

					return 0.0D / 0.0;
				}

				term = (String)var10.next();
			} while(!preprocessing.androidAPIs.contains(term) && !preprocessing.androidClasses.contains(term) && !preprocessing.androidConstants.contains(term));

			++overall;
			if (first.contains(term) && second.contains(term)) {
				++inCommon;
			}
		}
	}
	
	private static Map<String, Double> applyTfIdf(Map<String, Double> text, Map<String, Double> idf){
		Map<String, Double> map = new HashMap<>();
		
		for (String key : text.keySet()) {
			map.put(key, idf.get(key) * text.get(key));
		}
		
		return map;
	}
	
	
	public static double computeTextualSimilarity(Map<String, Double> first, Map<String, Double> second)
	{
		return VSM_old.vsm(first, second);
	}
	
	public static Map<String, Double> computeIDF(Collection<Map<String, Double>> strings){
		List<Set<String>> documents = new ArrayList<>();
		ArrayList<String> words = new ArrayList<String>();
		
		for (Map<String, Double> doc : strings) {
			Set<String> document = new HashSet<>();
			for (String s : doc.keySet()) {
					document.add(s);
					if(!words.contains(s))
						words.add(s);
			}
			documents.add(document);
		}
		
		return computeIDF(words, documents);
	}
	
	private static Map<String, Double> computeIDF(ArrayList<String> words, List<Set<String>> documents) {
		Map<String, Double> idf = new HashMap<String, Double>();
		
		for (String word : words) {
			double occurrences = 0;
			for (Set<String> document : documents) {
				if (document.contains(word))
					occurrences++;
			}
			double value = (((double)documents.size()) / occurrences);
			idf.put(word, Math.log(value));
		}
		
		return idf;
	}
	
}
