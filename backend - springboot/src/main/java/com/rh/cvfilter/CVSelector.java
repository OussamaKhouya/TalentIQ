package com.rh.cvfilter;

import java.util.*;
import java.util.stream.Collectors;

public class CVSelector {
    public List<String> selectTopCVs(List<String> cvContents, String jobOffer, CVAnalyzer analyzer) {
        Map<String, Double> scores = new HashMap<>();
        for (String cv : cvContents) {
            double score = analyzer.calculateRelevanceScore(cv, jobOffer);
            scores.put(cv, score);
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
} 