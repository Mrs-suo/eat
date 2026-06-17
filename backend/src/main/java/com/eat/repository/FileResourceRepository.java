package com.eat.repository;

import com.eat.entity.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
}
