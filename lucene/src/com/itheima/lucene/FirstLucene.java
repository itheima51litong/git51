package com.itheima.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.itheima.lucene.dao.BookDao;
import com.itheima.lucene.pojo.Book;

public class FirstLucene {
	@Test
	public void luceneIndexTest() throws IOException {
		BookDao bookDao = new BookDao();
		List<Book> books = bookDao.queryBookList();
		/*
		 * 1.采集数据 2.创建Document文档对象 3.创建分析器（分词器） 4.创建IndexWriterConfig配置信息类
		 * 5.创建Directory对象，声明索引库存储位置 6.创建IndexWriter写入对象 7.把Document写入到索引库中
		 * 8.释放资源
		 */
		// Analyzer analyzer = new StandardAnalyzer();
		Analyzer analyzer = new IKAnalyzer();
		// 5. 创建IndexWriteConfig对象，写入索引需要的配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		Directory directory = FSDirectory.open(new File("d:\\lucene\\index"));
		IndexWriter indexWriter = new IndexWriter(directory, config);
		for (Book book : books) {
			Integer id = book.getId();
			String desc = book.getDesc();
			String name = book.getName();
			String pic = book.getPic();
			Float price = book.getPrice();
			Document doc = new Document();
			doc.add(new StringField("id", book.getId().toString(), Store.YES));
			// 图书名称
			// 分词，索引，储存
			doc.add(new TextField("name", book.getName().toString(), Store.YES));
			// 图书价格
			// 分词，索引，储存
			doc.add(new FloatField("price", book.getPrice(), Store.YES));
			// 图书图片地址
			// 不分词，不索引，储存
			doc.add(new StoredField("pic", book.getPic().toString()));
			// 图书描述
			// 分词，索引，不储存
			doc.add(new TextField("desc", book.getDesc().toString(), Store.NO));
			indexWriter.addDocument(doc);
		}
		indexWriter.close();
	}

	// 查询索引
	@Test
	public void testQueryIndex() throws Exception {
		// 1. 创建Query搜索对象 精准查询
		Query query = new TermQuery(new Term("name", "java"));
		// 2. 创建Directory流对象,声明索引库位置
		Directory directory = FSDirectory.open(new File("D:\\lucene\\index"));
		// Directory directory = new RAMDirectory();
		// 3. 创建索引读取对象IndexReader
		IndexReader indexReader = DirectoryReader.open(directory);
		// 4. 创建索引搜索对象IndexSearcher
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TopDocs topDoc = indexSearcher.search(query, 5);
		ScoreDoc[] scoreDocs = topDoc.scoreDocs;
		System.out.println(Arrays.toString(scoreDocs) + "...");
		// 5. 使用索引搜索对象，执行搜索，返回结果集TopDocs
		for (ScoreDoc scoreDoc : scoreDocs) {
			// 6. 解析结果集
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id") + "id");
			System.out.println(doc.get("name") + "name");
			System.out.println(doc.get("pic") + "pic");
			System.out.println(doc.get("price") + "price");
			System.out.println(doc.get("desc") + "desc");
		}
		// 7. 释放资源
		indexReader.close();
	}

	// 删除
	@Test
	public void deleteAllIndex() throws Exception {
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		Directory directory = FSDirectory.open(new File("d:\\lucene\\index"));
		IndexWriter indexWriter = new IndexWriter(directory, config);
		indexWriter.deleteAll();
		indexWriter.close();
	}

	// 指定条件删除
	@Test
	public void testIndexDelete() throws IOException {
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		Directory directory = FSDirectory.open(new File("d:\\lucene\\index"));
		IndexWriter indexWriter = new IndexWriter(directory, config);
		indexWriter.deleteDocuments(new Term("name", "java"));
		indexWriter.close();
	}

	// 修改
	@Test
	public void testIndexUpdate() throws IOException {
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		Directory directory = FSDirectory.open(new File("d:\\lucene\\index"));
		IndexWriter indexWriter = new IndexWriter(directory, config);
		// 创建Document
		Document document = new Document();
		document.add(new TextField("idd", "1002", Store.YES));
		document.add(new TextField("name01", "lucene测试test 002", Store.YES));
		indexWriter.updateDocument(new Term("name","solr"), document);
		indexWriter.close();
	}
	//区间查询
	@Test
	public void queryIndex() throws Exception{
		Query query=NumericRangeQuery.newFloatRange("price", 60f, 70f, true, true);
		Directory directory = FSDirectory.open(new File("D:\\lucene\\index"));
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TopDocs docs = indexSearcher.search(query, 10);
		run(indexSearcher,docs);
	}
	//组合查询
	@Test
	public void queryIndex01() throws Exception{
		Query query1 = new TermQuery(new Term("name", "lucene"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 54f, 66f, false, true);
		BooleanQuery boolQuery = new BooleanQuery();
		boolQuery.add(query1, Occur.MUST_NOT);
		boolQuery.add(query2, Occur.MUST);
		Directory directory = FSDirectory.open(new File("D:\\lucene\\index"));
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TopDocs docs = indexSearcher.search(boolQuery, 10);
		run(indexSearcher,docs);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void run(IndexSearcher indexSearcher,TopDocs  topDoc) throws Exception{
		ScoreDoc[] scoreDocs = topDoc.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			// 6. 解析结果集
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id") + "id");
			System.out.println(doc.get("name") + "name");
			System.out.println(doc.get("pic") + "pic");
			System.out.println(doc.get("price") + "price");
			System.out.println(doc.get("desc") + "desc");
		}
		// 7. 释放资源
		indexSearcher.getIndexReader().close();
	}
}
