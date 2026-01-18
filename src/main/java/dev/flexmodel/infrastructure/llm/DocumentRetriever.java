package dev.flexmodel.infrastructure.llm;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * 如果使用Easy RAG，并不需要使用这个类
 * @author cjbi
 */
@ApplicationScoped
public class DocumentRetriever implements RetrievalAugmentor {

  private final RetrievalAugmentor augmentor;

  DocumentRetriever(EmbeddingStore store, EmbeddingModel model) {
    EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
      .embeddingModel(model)
      .embeddingStore(store)
      .maxResults(3)
      .build();
    augmentor = DefaultRetrievalAugmentor
      .builder()
      .contentRetriever(contentRetriever)
      .build();
  }

  @Override
  public AugmentationResult augment(AugmentationRequest augmentationRequest) {
    return augmentor.augment(augmentationRequest);
  }

}
