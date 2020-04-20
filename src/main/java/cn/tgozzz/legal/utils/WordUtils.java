package cn.tgozzz.legal.utils;

import cn.tgozzz.legal.exception.CommonException;
import lombok.SneakyThrows;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class WordUtils {

    public static String baseImagePath = ClassPath.get("static/word/image/");
    public static String baseWordPath = ClassPath.get("static/word/doc/");
    public static String baseHtmlDocPath = ClassPath.get("static/word/htmlDoc/");
    public static String basePdfPath = ClassPath.get("static/word/pdf/");

    /**
     * word转html 返回文件路径
     * @param wid 文件唯一id
     * @param sourceMedia 源文件类型
     * @param targetMedia 目标文件类型
     */
    public static String transform(String wid,String sourceMedia, String targetMedia) throws ParserConfigurationException, TransformerException, IOException {

        switch (sourceMedia + "-" + targetMedia) {
            case "doc-html":
                return docToHtml(wid);
            case "docx-html":
                return docxToHtml(wid);
            case "html-doc":
                return htmlToDoc(wid);
        }
                throw  new CommonException("文件类型错误");
    }

    /**
     * 文档格式转换
     * @param fileName 文件名不带后缀
     * @return
     */
    @SneakyThrows
    private static String docxToHtml(String fileName) {

        String imagePath = baseImagePath;
        String sourceFilePath = baseWordPath + fileName + ".docx";
        String targetFilePath = baseHtmlDocPath + fileName + ".html";

        OutputStreamWriter outputStreamWriter = null;
        try {
            File sourceFile = new File(URI.create("file://" + sourceFilePath));
            File targetFile = new File(URI.create("file://" + targetFilePath));
            XWPFDocument document = new XWPFDocument(new FileInputStream(sourceFile));
            XHTMLOptions options = XHTMLOptions.create();        // 存放图片的文件夹
            options.setExtractor(new FileImageExtractor(new File(imagePath)));        // html中图片的路径
            options.URIResolver(new BasicURIResolver("image"));
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8);
            XHTMLConverter xhtmlConverter = (XHTMLConverter) XHTMLConverter.getInstance();
            xhtmlConverter.convert(document, outputStreamWriter, options);
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }
        return targetFilePath;
    }


    /**
     * 文档格式转换
     * @param fileName 不包含文件后缀
     * @return
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private static String docToHtml(String fileName) throws TransformerException, IOException, ParserConfigurationException {

        String imagePath = baseImagePath;
        String sourceFilePath = baseWordPath + fileName + ".doc";
        String targetFilePath = baseHtmlDocPath + fileName + ".html";

        File imageDir = new File(URI.create("file://" + imagePath));
        File sourceFile = new File(URI.create("file://" + sourceFilePath));
        File targetFile = new File(URI.create("file://" + targetFilePath));
        if(!imageDir.exists()) {
            imageDir.mkdirs();
        }
        HWPFDocument wordDocument;
        wordDocument = new HWPFDocument(new FileInputStream(sourceFile));
        org.w3c.dom.Document document;

        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(document);
        //保存图片，并返回图片的相对路径
        wordToHtmlConverter.setPicturesManager((content, pictureType, name, width, height) -> {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(imagePath + name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                assert out != null;
                out.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "image/" + name;
        });
        wordToHtmlConverter.processDocument(wordDocument);
        org.w3c.dom.Document htmlDocument = wordToHtmlConverter.getDocument();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(targetFile);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer;
        serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);

        return targetFilePath;
    }

    /**
     * 文档格式换砖
     * @param fileName 不包含后缀
     * @return
     * @throws IOException
     */
    private static String htmlToDoc(String fileName) throws IOException {

        String sourceFilePath= baseHtmlDocPath + fileName + ".html";
        String targetFilePath = baseWordPath + fileName + ".doc";

        File sourceFile = new File(URI.create("file://" + sourceFilePath));
        File targetFile = new File(URI.create("file://" + targetFilePath));

        StringBuffer buffer = new StringBuffer();
        BufferedReader bf;
        bf = new BufferedReader(new FileReader(sourceFile));

        String s;
        while((s = bf.readLine())!=null){//使用readLine方法，一次读一行
            buffer.append(s.trim());
        }
        String xml = buffer.toString();

        // 生成doc格式的word文档，需要手动改为docx
        byte[] by = xml.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(by);
        POIFSFileSystem poifs = new POIFSFileSystem();
        DirectoryEntry directory = poifs.getRoot();
        DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
        FileOutputStream ostream = new FileOutputStream(targetFile);
        poifs.writeFilesystem(ostream);
        bais.close();
        ostream.close();

        return targetFilePath;
    }

    /**
     * 解析文件名字
     *   Map<String,String> map = resolveName(fileName);
     *         String media = map.get("media");
     *         String name = map.get("name");
     */
    public static Map<String, String> resolveName(String fileName) {
        Map<String, String> map = new HashMap<>();
        int pointPos = fileName.lastIndexOf(".");
        String media = fileName.substring(pointPos + 1);
        String name = fileName.substring(0,pointPos);
        map.put("media", media);
        map.put("name", name);
        return map;
    }
}
