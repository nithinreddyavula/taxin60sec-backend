package com.taxin60sec.backend.ai;
import java.util.List; import java.util.Map;
public interface AIService {
    DocumentAnalysis analyze(DocumentInput input); Classification classify(DocumentInput input);
    record DocumentInput(String documentId,String contentType,String storageKey,Map<String,String> attributes){} record DocumentAnalysis(String documentId,String summary,Map<String,String> fields){} record Classification(String label,double confidence){} record PromptTemplate(String name,String version,String template){}
    interface LLMProvider { String name(); String complete(PromptTemplate template,Map<String,Object> variables); } interface EmbeddingProvider { List<Float> embed(String text); } interface OCRProvider { String extract(DocumentInput input); } interface PromptBuilder { PromptTemplate build(String name,Map<String,Object> variables); } interface VectorStore { void upsert(String id,List<Float> vector,Map<String,String> metadata); List<String> search(List<Float> vector,int limit); } interface DocumentAnalysisService { DocumentAnalysis analyze(DocumentInput input); } interface DocumentClassifier { Classification classify(DocumentInput input); }
}
