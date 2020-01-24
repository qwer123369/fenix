package com.blinkfox.fenix.repository;

import com.alibaba.fastjson.JSON;
import com.blinkfox.fenix.FenixTestApplication;
import com.blinkfox.fenix.entity.Book;
import com.blinkfox.fenix.helper.StringHelper;
import com.blinkfox.fenix.specification.FenixSpecification;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

/**
 * 基于 {@link BookRepository} 用来测试 {@link org.springframework.data.jpa.domain.Specification} 和 {} 功能的单元测试类.
 *
 * @author blinkfox on 2020-01-21.
 * @since v2.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FenixTestApplication.class)
public class BookRepositoryPredicateTest {

    private static final String ISBN = "9787111641247";

    private static final String ID_2 = "2";

    private static final int PAGE = 540;

    private static Map<String, Object> paramMap;

    @Value("/data/book.json")
    private Resource bookResource;

    @Autowired
    private BookRepository bookRepository;

    /**
     * 从资源文件的中读取数据，初始化保存起来，便于后续读取或操作.
     */
    @PostConstruct
    public void init() throws IOException {
        // 从 JSON 文件中初始化插入 10 条图书信息，用作测试动态查询时使用.
        bookRepository.saveAll(JSON.parseArray(
                new String(FileCopyUtils.copyToByteArray(bookResource.getFile())), Book.class));

        // 初始化一些上下文参数信息.
        paramMap = new HashMap<>();
        paramMap.put("isbn", ISBN);
        paramMap.put("id", ID_2);
        paramMap.put("totalPage", PAGE);
    }

    /**
     * 测试使用 {@code Specification} 的方式来等值查询图书信息.
     */
    @Test
    public void testEmpty() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder -> {
            Assert.assertNotNull(builder.getCriteriaBuilder());
            Assert.assertNotNull(builder.getCriteriaQuery());
            Assert.assertNotNull(builder.getFrom());
            return builder.build();
        }));
        Assert.assertEquals(10, books.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来等值查询图书信息.
     */
    @Test
    public void testEquals() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", paramMap.get("isbn"))
                .build()));
        Assert.assertEquals(1, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, StringHelper.isNotBlank(isbn))
                        .build()));
        Assert.assertEquals(1, books2.size());

        String isbn2 = (String) paramMap.get("isbn2");
        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn2, StringHelper.isNotBlank(isbn2))
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来等值查询图书信息.
     */
    @Test
    public void testOrEquals() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", paramMap.get("isbn"))
                        .orEquals("id", paramMap.get("id"))
                        .build()));
        Assert.assertEquals(2, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn)
                        .orEquals("id", paramMap.get("id"), false)
                        .build()));
        Assert.assertEquals(1, books2.size());
        Assert.assertEquals(ISBN, books2.get(0).getIsbn());

        String id = (String) paramMap.get("id");
        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, false)
                        .orEquals("id", id, StringHelper.isNotBlank(id))
                        .build()));
        Assert.assertEquals(1, books3.size());
        Assert.assertEquals(ID_2, books3.get(0).getId());
    }

    /**
     * 测试使用 {@code Specification} 的方式来等值查询图书信息.
     */
    @Test
    public void testNotEquals() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andNotEquals("isbn", paramMap.get("isbn"))
                        .build()));
        Assert.assertEquals(9, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andNotEquals("isbn", isbn, StringHelper.isNotBlank(isbn))
                        .build()));
        Assert.assertEquals(9, books2.size());

        String isbn2 = (String) paramMap.get("isbn2");
        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andNotEquals("isbn", isbn2, StringHelper.isNotBlank(isbn2))
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来等值查询图书信息.
     */
    @Test
    public void testOrNotEquals() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.orNotEquals("isbn", paramMap.get("isbn"))
                        .build()));
        Assert.assertEquals(9, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.orNotEquals("isbn", isbn, false)
                        .build()));
        Assert.assertEquals(10, books2.size());

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.orNotEquals("isbn", isbn, StringHelper.isNotBlank(isbn))
                        .build()));
        Assert.assertEquals(9, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来大于查询图书信息.
     */
    @Test
    public void testGreaterThan() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThan("totalPage", paramMap.get("totalPage"))
                        .build()));
        Assert.assertEquals(4, books.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() > PAGE));

        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThan("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(4, books2.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() > PAGE));

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThan("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来大于查询图书信息.
     */
    @Test
    public void testOrGreaterThan() {
        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", paramMap.get("isbn"))
                        .orGreaterThan("totalPage", totalPage)
                        .build()));
        Assert.assertEquals(5, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn)
                        .orGreaterThan("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(1, books2.size());

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, false)
                        .orGreaterThan("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(4, books3.size());
        books3.forEach(book -> Assert.assertTrue(book.getTotalPage() > PAGE));
    }

    /**
     * 测试使用 {@code Specification} 的方式来大于等于查询图书信息.
     */
    @Test
    public void testGreaterThanEqual() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThanEqual("totalPage", paramMap.get("totalPage"))
                        .build()));
        Assert.assertEquals(5, books.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() >= PAGE));

        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThanEqual("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(5, books2.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() >= PAGE));

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andGreaterThanEqual("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来大于等于查询图书信息.
     */
    @Test
    public void testOrGreaterThanEqual() {
        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("id", paramMap.get("id"))
                        .orGreaterThanEqual("totalPage", totalPage)
                        .build()));
        Assert.assertEquals(6, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn)
                        .orGreaterThanEqual("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(1, books2.size());

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, false)
                        .orGreaterThanEqual("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(5, books3.size());
        books3.forEach(book -> Assert.assertTrue(book.getTotalPage() >= PAGE));
    }

    /**
     * 测试使用 {@code Specification} 的方式来小于查询图书信息.
     */
    @Test
    public void testLessThan() {
        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThan("totalPage", totalPage)
                        .build()));
        Assert.assertEquals(5, books.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() < PAGE));

        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThan("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(5, books2.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() < PAGE));

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThan("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来小于查询图书信息.
     */
    @Test
    public void testOrLessThan() {
        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", paramMap.get("isbn"))
                        .orLessThan("totalPage", totalPage)
                        .build()));
        Assert.assertEquals(6, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn)
                        .orLessThan("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(1, books2.size());

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, false)
                        .orLessThan("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(5, books3.size());
        books3.forEach(book -> Assert.assertTrue(book.getTotalPage() < PAGE));
    }

    /**
     * 测试使用 {@code Specification} 的方式来小于等于查询图书信息.
     */
    @Test
    public void testLessThanEqual() {
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThanEqual("totalPage", paramMap.get("totalPage"))
                        .build()));
        Assert.assertEquals(6, books.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() <= PAGE));

        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThanEqual("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(6, books2.size());
        books.forEach(book -> Assert.assertTrue(book.getTotalPage() <= PAGE));

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andLessThanEqual("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(10, books3.size());
    }

    /**
     * 测试使用 {@code Specification} 的方式来小于等于查询图书信息.
     */
    @Test
    public void testOrLessThanEqual() {
        int totalPage = (Integer) paramMap.get("totalPage");
        List<Book> books = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("id", paramMap.get("id"))
                        .orLessThanEqual("totalPage", totalPage)
                        .build()));
        Assert.assertEquals(6, books.size());

        String isbn = (String) paramMap.get("isbn");
        List<Book> books2 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn)
                        .orLessThanEqual("totalPage", totalPage, false)
                        .build()));
        Assert.assertEquals(1, books2.size());

        List<Book> books3 = bookRepository.findAll(FenixSpecification.of(builder ->
                builder.andEquals("isbn", isbn, false)
                        .orLessThanEqual("totalPage", totalPage, totalPage > 0)
                        .build()));
        Assert.assertEquals(6, books3.size());
        books3.forEach(book -> Assert.assertTrue(book.getTotalPage() <= PAGE));
    }

}
