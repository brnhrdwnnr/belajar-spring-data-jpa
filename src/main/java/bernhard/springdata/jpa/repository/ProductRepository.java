package bernhard.springdata.jpa.repository;

import bernhard.springdata.jpa.entity.Category;
import bernhard.springdata.jpa.entity.Product;
import bernhard.springdata.jpa.model.ProductPrice;
import bernhard.springdata.jpa.model.SimpleProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findAllByCategory_Name(String name);

    List<Product> findAllByCategory_Name(String name, Sort sort);

    Page<Product> findAllByCategory_Name(String name, Pageable pageable);

    Long countByCategory_Name(String name);

    boolean existsByName(String name);

    @Transactional
    //tanpa ini unit test harus menggunakan transactionOperations.executeWithoutResult(
    int deleteByName(String name);

    List<Product> searchProductUsingName(@Param("name") String name);

    List<Product> searchProductUsingName(@Param("name") String name, Pageable pageable);

//    @Query(value = "select p from Product p where p.name like :name or p.category.name like :name")
//    List<Product> searchProduct(@Param("name") String name);

//    @Query(value = "select p from Product p where p.name like :name or p.category.name like :name")
//    List<Product> searchProduct(@Param("name") String name, Pageable pageable);

    @Query(
            value = "select p from Product p where p.name like :name or p.category.name like :name",
            countQuery =  "select count(p) from Product p where p.name like :name or p.category.name like :name"
    )
    Page<Product> searchProduct(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query("delete from Product p where p.name = :name")
    int deleteProductUsingName(@Param("name") String name);

    @Modifying
    @Query("update Product p set p.price = 0 where p.id = :id")
    int updateProductPriceToZero(@Param("id") Long id);

    Stream<Product> streamAllByCategory(Category category);

    Slice<Product> findAllByCategory(Category category, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findFirstById(Long id);

//    List<SimpleProduct> findAllByNameLike(String name);

   <T> List<T> findAllByNameLike(String name, Class<T> tClass);
}
