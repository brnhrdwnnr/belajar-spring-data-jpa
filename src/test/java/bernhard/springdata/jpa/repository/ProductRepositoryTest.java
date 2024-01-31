package bernhard.springdata.jpa.repository;

import bernhard.springdata.jpa.entity.Category;
import bernhard.springdata.jpa.entity.Product;
import bernhard.springdata.jpa.model.ProductPrice;
import bernhard.springdata.jpa.model.SimpleProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionOperations transactionOperations;

    @Test
    void createProduct() {
        Category category = categoryRepository.findById(1L).orElse(null);
        assertNotNull(category);

        {
            Product product = new Product();
            product.setName("Apple Iphone 14 Pro Max");
            product.setPrice(25_000_000L);
            product.setCategory(category);
            productRepository.save(product);
        }

        {
            Product product = new Product();
            product.setName("Apple Iphone 13 Pro Max");
            product.setPrice(18_000_000L);
            product.setCategory(category);
            productRepository.save(product);
        }
    }

    @Test
    void findProducts() {
        List<Product> products = productRepository
                .findAllByCategory_Name("GADGET MURAH");

        assertEquals(2, products.size());
        assertEquals("Apple Iphone 14 Pro Max", products.get(0).getName());
        assertEquals("Apple Iphone 13 Pro Max", products.get(1).getName());
    }

    @Test
    void sort() {
        Sort sort = Sort.by(Sort.Order.desc("id"));
        List<Product> products = productRepository
                .findAllByCategory_Name("GADGET MURAH",sort);

        assertEquals(2, products.size());
        assertEquals("Apple Iphone 13 Pro Max", products.get(0).getName());
        assertEquals("Apple Iphone 14 Pro Max", products.get(1).getName());
    }

    @Test
    void testFindProductWithPageable() {
        //page 0
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id")));
        Page<Product> products = productRepository.findAllByCategory_Name("GADGET MURAH", pageable);

        assertEquals(1, products.getContent().size());
        assertEquals(0, products.getNumber());
        assertEquals(2, products.getTotalElements());
        assertEquals(2, products.getTotalPages());
        assertEquals("Apple Iphone 13 Pro Max", products.getContent().get(0).getName());

        //page 1
        pageable = PageRequest.of(1, 1, Sort.by(Sort.Order.desc("id")));
        products = productRepository.findAllByCategory_Name("GADGET MURAH",pageable);

        assertEquals(1, products.getContent().size());
        assertEquals(1, products.getNumber());
        assertEquals(2, products.getTotalElements());
        assertEquals(2, products.getTotalPages());
        assertEquals("Apple Iphone 14 Pro Max", products.getContent().get(0).getName());
    }

    @Test
    void testCount(){
        Long count = productRepository.count();
        assertEquals(2L, count);

        count = productRepository.countByCategory_Name("GADGET MURAH");
        assertEquals(2L, count);

        count = productRepository.countByCategory_Name("IPHONE");
        assertEquals(0L, count);
    }

    @Test
    void testExists() {
        boolean exists = productRepository.existsByName("Apple Iphone 14 Pro Max");
        assertTrue(exists);

        //test not exists
        exists = productRepository.existsByName("Apple Iphone 14 Pro Maxx");
        assertFalse(exists);
    }

    @Test
    void testDeleteOld() {
       transactionOperations.executeWithoutResult(transactionStatus -> {
           Category category = categoryRepository.findById(1L).orElse(null);
           assertNotNull(category);

           Product product = new Product();
           product.setName("Samsung Galaxy S11");
           product.setPrice(10_000_000L);
           product.setCategory(category);
           productRepository.save(product);

           int delete = productRepository.deleteByName("Samsung Galaxy S11");
           assertEquals(1, delete);

           delete = productRepository.deleteByName("Samsung Galaxy S11");
           assertEquals(0, delete);
       });
    }

    @Test
    void testDeleteNew() {
            Category category = categoryRepository.findById(1L).orElse(null);
            assertNotNull(category);

            Product product = new Product();
            product.setName("Samsung Galaxy S11");
            product.setPrice(10_000_000L);
            product.setCategory(category);
            productRepository.save(product); //transaksi 1

            int delete = productRepository.deleteByName("Samsung Galaxy S11"); //transaksi 2
            assertEquals(1, delete);

            delete = productRepository.deleteByName("Samsung Galaxy S11"); //transaksi 3
            assertEquals(0, delete);
    }

    @Test
    void namedQuery() {
        List<Product> products = productRepository.searchProductUsingName("Apple Iphone 14 Pro Max");
        assertEquals(1, products.size());
        assertEquals("Apple Iphone 14 Pro Max", products.get(0).getName());
    }

    @Test
    void namedQueryWithPageable() {
        Pageable pageable = PageRequest.of(0, 1);
        List<Product> products = productRepository.searchProductUsingName("Apple Iphone 14 Pro Max", pageable);
        assertEquals(1, products.size());
        assertEquals("Apple Iphone 14 Pro Max", products.get(0).getName());
    }

//    @Test
//    void searchProductLike() {
//        List<Product> products = productRepository.searchProduct("%Iphone%");
//        assertEquals(2, products.size());
//
//        products = productRepository.searchProduct("%GADGET%");
//        assertEquals(2, products.size());
//    }
//
//    @Test
//    void searchProductWithPageable() {
//        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id")));
//        List<Product> products = productRepository.searchProduct("%Iphone%", pageable);
//        assertEquals(1, products.size());
//
//        products = productRepository.searchProduct("%GADGET%", pageable);
//        assertEquals(1, products.size());
//    }

    @Test
    void searchProductWithPageable() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id")));
        Page<Product> products = productRepository.searchProduct("%Iphone%", pageable);
        assertEquals(1, products.getContent().size());
        assertEquals(0, products.getNumber());
        assertEquals(2, products.getTotalPages());
        assertEquals(2, products.getTotalElements());


        products = productRepository.searchProduct("%GADGET%", pageable);
        assertEquals(1, products.getContent().size());
        assertEquals(0, products.getNumber());
        assertEquals(2, products.getTotalPages());
        assertEquals(2, products.getTotalElements());
    }

    @Test
    void modifying() {
        transactionOperations.executeWithoutResult(transactionStatus -> {
            int total = productRepository.deleteProductUsingName("Wrong");
            assertEquals(0, total);

            total = productRepository.updateProductPriceToZero(1L);
            assertEquals(1, total);

            Product product = productRepository.findById(1L).orElse(null);
            assertNotNull(product);
            assertEquals(0L, product.getPrice());
        });
    }

    @Test
    void stream() {
        transactionOperations.executeWithoutResult(transactionStatus -> {
            Category category = categoryRepository.findById(1L).orElse(null);
            assertNotNull(category);

            Stream<Product> stream = productRepository.streamAllByCategory(category);
            stream.forEach(product -> System.out.println(product.getId() + " : " + product.getName()));

        });
    }

    @Test
    void slice(){
        Pageable firstPage = PageRequest.of(0, 1);

        Category category = categoryRepository.findById(1L).orElse(null);
        assertNotNull(category);

        Slice<Product> slice = productRepository.findAllByCategory(category, firstPage);
        //do with content

        while (slice.hasNext()) {
            slice = productRepository.findAllByCategory(category, slice.nextPageable());
            //do with content
        }

    }

    @Test
    void lock1() {
        transactionOperations.executeWithoutResult(transactionStatus -> {
            try {
                Product product = productRepository.findFirstById(1L).orElse(null);
                assertNotNull(product);
                product.setPrice(30_000_000L);

                Thread.sleep(20_000L);
                productRepository.save(product);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void lock2() {
        transactionOperations.executeWithoutResult(transactionStatus -> {
                Product product = productRepository.findFirstById(1L).orElse(null);
                assertNotNull(product);
                product.setPrice(10_000_000L);
                productRepository.save(product);
        });
    }

    @Test
    void specification() {
        Specification<Product> specification = (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaQuery.where(
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("name"), "Apple Iphone 14 Pro Max"),
                            criteriaBuilder.equal(root.get("name"), "Apple Iphone 13 Pro Max")
                    )
            ).getRestriction();
        };

        List<Product> products = productRepository.findAll(specification);
        assertEquals(2, products.size());
    }

    @Test
    void projection() {
        List<SimpleProduct> simpleProducts = productRepository.findAllByNameLike("%Apple%", SimpleProduct.class);
        assertEquals(2, simpleProducts.size());

        List<ProductPrice> productPrices = productRepository.findAllByNameLike("%Apple%", ProductPrice.class);
        assertEquals(2, productPrices.size());
    }
}
