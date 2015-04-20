package jp.co.atware.elasticsearch.type.url.component;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.*;
import static org.elasticsearch.search.sort.SortOrder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class UrlComponentTypePluginTest {

    private static final String PATH_TO_MAPPING_FILE_BASEDIR = UrlComponentTypePluginTest.class.getPackage().getName().replaceAll("\\.", "/");
    private static final String TEST_INDEX_NAME = "test";
    private static final String TEST_TYPE_NAME = "test";

    private static ElasticsearchClusterRunner runner;

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void setUp() throws Exception {
        runner = new ElasticsearchClusterRunner();
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(int index, Builder settingsBuilder) {
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("index.number_of_replicas", 0);
            }
        }).build(newConfigs().ramIndexStore().numOfNode(1));
        runner.ensureYellow();
    }

    @Before
    public void initIndex() throws Exception {
        runner.createIndex(TEST_INDEX_NAME, ImmutableSettings.EMPTY);
        String pathToMappingFile = getPathToMappingFile();
        runner.createMapping(TEST_INDEX_NAME, TEST_TYPE_NAME, readSettingFile(pathToMappingFile, "UTF-8"));
    }

    @After
    public void cleanup() {
        runner.deleteIndex(TEST_INDEX_NAME);
    }

    @AfterClass
    public static void tearDown() {
        runner.clean();
        runner.close();
    }

    @Test
    public void testURLComponentType_hasHostName() throws Exception {

        index("1", "url", "http://localhost/foo");
        index("2", "url", "https://127.0.0.1/foo/bar/");
        index("3", "url", "http://localhost/?keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
        index("4", "url", "-");
        runner.refresh();

        String[] storedFields = { "url.raw", "url.protocol", "url.hostname", "url.path", "url.query" };
        SearchResponse response = searchAll(TEST_INDEX_NAME, TEST_TYPE_NAME, storedFields);
        System.out.println(response);
        SearchHit[] hits = response.getHits().hits();
        assertThat(hits.length, is(4));
        {
            SearchHit hit = hits[0];
            assertFieldValues(hit.field("url.raw"), "http://localhost/foo");
            assertFieldValues(hit.field("url.protocol"), "http");
            assertFieldValues(hit.field("url.hostname"), "localhost");
            assertFieldValues(hit.field("url.path"), "/foo");
            assertNull(hit.field("url.query"));
        }
        {
            SearchHit hit = hits[1];
            assertFieldValues(hit.field("url.raw"), "https://127.0.0.1/foo/bar/");
            assertFieldValues(hit.field("url.protocol"), "https");
            assertFieldValues(hit.field("url.hostname"), "127.0.0.1");
            assertFieldValues(hit.field("url.path"), "/foo/bar/");
            assertNull(hit.field("url.query"));
        }
        {
            SearchHit hit = hits[2];
            assertFieldValues(hit.field("url.raw"), "http://localhost/?keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
            assertFieldValues(hit.field("url.protocol"), "http");
            assertFieldValues(hit.field("url.hostname"), "localhost");
            assertFieldValues(hit.field("url.path"), "/");
            assertFieldValues(hit.field("url.query"), "keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
        }
        {
            SearchHit hit = hits[3];
            assertFieldValues(hit.field("url.raw"), "-");
            assertNull(hit.field("url.protocol"));
            assertNull(hit.field("url.hostname"));
            assertNull(hit.field("url.path"));
            assertNull(hit.field("url.query"));
        }
    }

    private void assertFieldValues(SearchHitField field, Object... expectedValues) {
        assertNotNull(field.values());
        assertThat(field.values(), is(Arrays.asList(expectedValues)));
    }

    @Test
    public void testURLComponentType_unspecifiedHostName() throws Exception {

        index("1", "url", "/");
        index("2", "url", "/foo/bar/");
        index("3", "url", "/foo/bar/hoge?keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
        runner.refresh();

        String[] storedFields = { "url.raw", "url.protocol", "url.hostname", "url.path", "url.query" };
        SearchResponse response = searchAll(TEST_INDEX_NAME, TEST_TYPE_NAME, storedFields);
        SearchHit[] hits = response.getHits().hits();

        assertThat(hits.length, is(3));
        {
            SearchHit hit = hits[0];
            assertFieldValues(hit.field("url.raw"), "/");
            assertNull(hit.field("protocol"));
            assertNull(hit.field("hostname"));
            assertFieldValues(hit.field("url.path"), "/");
            assertNull(hit.field("url.query"));
        }
        {
            SearchHit hit = hits[1];
            assertFieldValues(hit.field("url.raw"), "/foo/bar/");
            assertNull(hit.field("protocol"));
            assertNull(hit.field("hostname"));
            assertFieldValues(hit.field("url.path"), "/foo/bar/");
            assertNull(hit.field("url.query"));
        }
        {
            SearchHit hit = hits[2];
            assertFieldValues(hit.field("url.raw"), "/foo/bar/hoge?keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
            assertNull(hit.field("protocol"));
            assertNull(hit.field("hostname"));
            assertFieldValues(hit.field("url.path"), "/foo/bar/hoge");
            assertFieldValues(hit.field("url.query"), "keyword=%e6%83%85%e5%a0%b1%e6%a4%9c%e7%b4%a2&p=10&count=20&sort=1");
        }
    }

    @Test
    public void testURLPathType() throws Exception {

        index("1", "path", "/");
        index("2", "path", "/foo/bar/");
        index("3", "path", "/foo/bar/hoge");
        index("4", "path", "-");
        runner.refresh();

        String[] storedFields = { "path.self", "path.parents" };
        SearchResponse response = searchAll(TEST_INDEX_NAME, TEST_TYPE_NAME, storedFields);
        System.out.println(response);
        SearchHit[] hits = response.getHits().hits();

        assertThat(hits.length, is(4));
        {
            SearchHit hit = hits[0];
            assertFieldValues(hit.field("path.self"), "/");
            assertNull(hit.field("path.parents"));
        }
        {
            SearchHit hit = hits[1];
            assertFieldValues(hit.field("path.self"), "/foo/bar");
            assertFieldValues(hit.field("path.parents"), "/foo");
        }
        {
            SearchHit hit = hits[2];
            assertFieldValues(hit.field("path.self"), "/foo/bar/hoge");
            assertFieldValues(hit.field("path.parents"), "/foo/bar", "/foo");
        }
        {
            SearchHit hit = hits[3];
            assertFieldValues(hit.field("path.self"), "-");
            assertNull(hit.field("path.parents"));
        }
    }

    public void testURLQueryType() {
    }

    private final String format = "{ \"id\": \"%s\", \"%s\": \"%s\" }";

    private void index(String id, String fieldName, String fieldValue) {
        IndexRequestBuilder builder = runner.client().prepareIndex("test", "test");
        builder.setId(id);
        builder.setSource(String.format(format, id, fieldName, fieldValue));
        IndexResponse response = builder.execute().actionGet();
        if (!response.isCreated()) {
            throw new IllegalArgumentException();
        }
    }

    private String getPathToMappingFile() {
        return PATH_TO_MAPPING_FILE_BASEDIR + "/" + name.getMethodName() + ".json";
    }

    private static String readSettingFile(String path, String charset) throws Exception {
        return new String(Files.readAllBytes(Paths.get(UrlComponentTypePluginTest.class.getClassLoader().getResource(path).toURI())), charset);
    }

    private SearchResponse searchAll(String testIndexName, String testTypeName, String[] storedFields) {
        return runner.client().prepareSearch(TEST_INDEX_NAME).addSort("id", ASC).setTypes(TEST_TYPE_NAME).addFields(storedFields).execute().actionGet();
    }
}
