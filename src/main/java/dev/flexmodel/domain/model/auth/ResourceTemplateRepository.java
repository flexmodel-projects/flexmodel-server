package dev.flexmodel.domain.model.auth;

import dev.flexmodel.codegen.entity.ResourceTemplate;

import java.util.List;

public interface ResourceTemplateRepository {

  ResourceTemplate findById(Long templateId);

  List<ResourceTemplate> findAll();

  ResourceTemplate save(ResourceTemplate resourceTemplate);

  void delete(Long templateId);
}
