package dev.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Resource;

import java.util.List;

@ApplicationScoped
public class ResourceService {

  @Inject
  ResourceRepository resourceRepository;

  public List<Resource> findAll() {
    return resourceRepository.findAll();
  }

  public Resource findById(Long resourceId) {
    return resourceRepository.findById(resourceId);
  }

  public Resource create(Resource resource) {
    return resourceRepository.save(resource);
  }

  public Resource update(Resource resource) {
    Resource existingResource = resourceRepository.findById(resource.getId());
    if (existingResource == null) {
      throw new RuntimeException("Resource not found");
    }
    return resourceRepository.save(resource);
  }

  public void delete(Long resourceId) {
    resourceRepository.delete(resourceId);
  }
}
