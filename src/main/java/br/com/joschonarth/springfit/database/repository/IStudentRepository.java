package br.com.joschonarth.springfit.database.repository;

import br.com.joschonarth.springfit.database.model.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IStudentRepository extends JpaRepository<StudentEntity, UUID> {

    Optional<StudentEntity> findByEmail(String email);
}
