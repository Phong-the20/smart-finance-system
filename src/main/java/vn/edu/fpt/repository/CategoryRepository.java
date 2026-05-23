package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
