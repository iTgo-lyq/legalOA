package cn.tgozzz.legal.utils;

import cn.tgozzz.legal.exception.CommonException;
import lombok.SneakyThrows;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordUtils {

    public static String baseImagePath = ClassPath.get("static/word/image/");
    public static String baseWordPath = ClassPath.get("static/word/doc/");
    public static String baseHtmlDocPath = ClassPath.get("static/word/htmlDoc/");
    public static String basePdfPath = ClassPath.get("static/word/pdf/");

    /**
     * word转html 返回文件路径
     *
     * @param wid         文件唯一id
     * @param sourceMedia 源文件类型
     * @param targetMedia 目标文件类型
     */
    public static String transform(String wid, String sourceMedia, String targetMedia) throws ParserConfigurationException, TransformerException, IOException {

        switch (sourceMedia + "-" + targetMedia) {
            case "doc-html":
                return docToHtml(wid);
            case "docx-html":
                return docxToHtml(wid);
            case "html-doc":
                return htmlToDoc(wid);
        }
        throw new CommonException("文件类型错误");
    }

    /**
     * 文档格式转换
     *
     * @param fileName 文件名不带后缀
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
     *
     * @param fileName 不包含文件后缀
     */
    private static String docToHtml(String fileName) throws TransformerException, IOException, ParserConfigurationException {

        String imagePath = baseImagePath;
        String sourceFilePath = baseWordPath + fileName + ".doc";
        String targetFilePath = baseHtmlDocPath + fileName + ".html";

        File imageDir = new File(URI.create("file://" + imagePath));
        File sourceFile = new File(URI.create("file://" + sourceFilePath));
        File targetFile = new File(URI.create("file://" + targetFilePath));
        if (!imageDir.exists()) {
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
     *
     * @param fileName 不包含后缀
     */
    private static String htmlToDoc(String fileName) throws IOException {

        String sourceFilePath = baseHtmlDocPath + fileName + ".html";
        String targetFilePath = baseWordPath + fileName + ".doc";

        File sourceFile = new File(URI.create("file://" + sourceFilePath));
        File targetFile = new File(URI.create("file://" + targetFilePath));

        StringBuilder buffer = new StringBuilder();
        BufferedReader bf;
        bf = new BufferedReader(new FileReader(sourceFile));

        String s;
        while ((s = bf.readLine()) != null) {//使用readLine方法，一次读一行
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
     * @param ctGraphicalObject 图片数据
     * @param deskFileName      图片描述
     * @param width             宽
     * @param height            高
     * @param leftOffset        水平偏移 left
     * @param topOffset         垂直偏移 top
     * @param behind            文字上方，文字下方
     */
    public static CTAnchor getAnchorWithGraphic(CTGraphicalObject ctGraphicalObject,
                                                String deskFileName, int width, int height,
                                                int leftOffset, int topOffset, boolean behind) {
        String anchorXML =
                "<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
                        + "simplePos=\"0\" relativeHeight=\"0\" behindDoc=\"" + ((behind) ? 1 : 0) + "\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
                        + "<wp:simplePos x=\"0\" y=\"0\"/>"
                        + "<wp:positionH relativeFrom=\"column\">"
                        + "<wp:posOffset>" + leftOffset + "</wp:posOffset>"
                        + "</wp:positionH>"
                        + "<wp:positionV relativeFrom=\"paragraph\">"
                        + "<wp:posOffset>" + topOffset + "</wp:posOffset>" +
                        "</wp:positionV>"
                        + "<wp:extent cx=\"" + width + "\" cy=\"" + height + "\"/>"
                        + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
                        + "<wp:wrapNone/>"
                        + "<wp:docPr id=\"1\" name=\"Drawing 0\" descr=\"" + deskFileName + "\"/><wp:cNvGraphicFramePr/>"
                        + "</wp:anchor>";

        CTDrawing drawing = null;
        try {
            drawing = CTDrawing.Factory.parse(anchorXML);
        } catch (XmlException e) {
            e.printStackTrace();
        }
        CTAnchor anchor = drawing.getAnchorArray(0);
        anchor.setGraphic(ctGraphicalObject);
        return anchor;
    }

    /**
     * 获取最后一张图片bytes
     */
    @SneakyThrows
    public static byte[] readLastDocxImage(File file) {
        byte[] data = new byte[0];
        FileInputStream fis = new FileInputStream(file);
        XWPFDocument document = new XWPFDocument(fis);
        List<XWPFPictureData> picList = document.getAllPictures();
        if (picList.size() > 0)
            data = picList.get(picList.size() - 1).getData();
        return data;
    }

    /**
     * 添加违反签名图片到文档
     */
    @SneakyThrows
    public static byte[] addWeiFanSignature(byte[] bfile, byte[] img) {
        InputStream imgIn = new ByteArrayInputStream(img);
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bfile));
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        // 设置图片
        run.setText("文档最终负责人（微泛）：");
        // 添加浮动图片
        run = paragraph.createRun();
        run.addPicture(imgIn, Document.PICTURE_TYPE_PNG, "WeiFan__signature", Units.toEMU(100), Units.toEMU(30));
        imgIn.close();
        // 获取到图片数据
        CTDrawing drawing = run.getCTR().getDrawingArray(0);
        CTGraphicalObject graphicalObject = drawing.getInlineArray(0).getGraphic();
        //拿到新插入的图片替换添加CTAnchor 设置浮动属性 删除inline属性
        CTAnchor anchor = getAnchorWithGraphic(graphicalObject, "WeiFan__signature",
                Units.toEMU(250), Units.toEMU(80),//图片大小
                Units.toEMU(100), Units.toEMU(0), true);//相对当前段落位置 需要计算段落已有内容的左偏移
        drawing.setAnchorArray(new CTAnchor[]{anchor});//添加浮动属性
        drawing.removeInline(0);//删除行内属性
        document.write(res);
        document.close();
        return res.toByteArray();
    }

    /**
     * 读取docx文件中的所有图片
     */
    public static String readDocxImage(String srcFile, String imageFile) {
        String path = srcFile;
        File file = new File(path);
        try {
            // 读取文件
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            // 用XWPFWordExtractor来获取文字
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            String text = xwpfWordExtractor.getText();
            //将获取到的文字存放到对应文件名中的txt文件中
            String temp[] = srcFile.split("\\/");
            String temp1 = temp[temp.length - 1];
            String temp3[] = temp1.split("\\.");
            String txtFileName = "D:/test/" + temp3[0] + ".txt";
            PrintStream ps = new PrintStream(txtFileName);
            ps.println(text);

            // 用XWPFDocument的getAllPictures来获取所有的图片
            List<XWPFPictureData> picList = document.getAllPictures();
            for (XWPFPictureData pic : picList) {
                System.out.println(pic.getPictureType() + "\n" + File.separator + "\n" + pic.suggestFileExtension() + "\n" + File.separator + "\n"
                        + pic.getFileName() + "\n");
                byte[] bytev = pic.getData();
                System.out.println(bytev.length);
                // 大于1000bites的图片我们才弄下来，消除word中莫名的小图片的影响
                if (bytev.length > 300) {
                    FileOutputStream fos = new FileOutputStream(imageFile + pic.getFileName());
                    fos.write(bytev);
                }
            }
            fis.close();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密获取摘要
     */
    @SneakyThrows
    public static String getContent(byte[] bfile) {
        StringBuffer sb = new StringBuffer();
        XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bfile)).getXWPFDocument();
        List<XWPFParagraph> paragraphList = document.getParagraphs();
        paragraphList.forEach(paragraph -> sb.append(paragraph.getText()));
        document.close();
        return sb.toString();
    }

    /**
     * 解析文件名字
     * Map<String,String> map = resolveName(fileName);
     * String media = map.get("media");
     * String name = map.get("name");
     */
    public static Map<String, String> resolveName(String fileName) {
        Map<String, String> map = new HashMap<>();
        int pointPos = fileName.lastIndexOf(".");
        String media = fileName.substring(pointPos + 1);
        String name = fileName.substring(0, pointPos);
        map.put("media", media);
        map.put("name", name);
        return map;
    }

    public static void main(String[] args) throws Exception {
//        String path = "D:\\temp\\test.docx";
//        File file = new File(path);
//        XWPFDocument document = new XWPFDocument(new FileInputStream(file));
////        document.createParagraph().createRun().setText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试");
////        ArrayList<XWPFParagraph> paragraphs = new ArrayList<>(document.getParagraphs());
////        for(XWPFParagraph paragraph: paragraphs) {
////            System.out.println(paragraph.getText());
////            System.out.println("————————————————————————————————————————————————————————————");
////        }
//        XWPFParagraph paragraph = document.createParagraph();
//        XWPFRun run = paragraph.createRun();
//        // 设置图片
//        run.setText("微泛电子签名: ");
//        // 添加浮动图片
//        run = paragraph.createRun();
//        InputStream in = new FileInputStream("D:\\temp\\image2.png");
//        run.addPicture(in, Document.PICTURE_TYPE_PNG, "weifan__signature", Units.toEMU(100), Units.toEMU(30));
//        in.close();
//        // 获取到图片数据
//        CTDrawing drawing = run.getCTR().getDrawingArray(0);
//        CTGraphicalObject graphicalobject = drawing.getInlineArray(0).getGraphic();
//
//        //拿到新插入的图片替换添加CTAnchor 设置浮动属性 删除inline属性
//        CTAnchor anchor = getAnchorWithGraphic(graphicalobject, "weifan__signature",
//                Units.toEMU(100), Units.toEMU(30),//图片大小
//                Units.toEMU(100), Units.toEMU(0), true);//相对当前段落位置 需要计算段落已有内容的左偏移
//        drawing.setAnchorArray(new CTAnchor[]{anchor});//添加浮动属性
//        drawing.removeInline(0);//删除行内属性
////        run.setText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试");
//        document.write(new FileOutputStream("D:\\temp\\test2.docx"));
//        document.close();
//
//        readDocxImage("D:\\temp\\test2.docx","");
        StringBuffer sb = new StringBuffer();
        XWPFDocument document = new XWPFDocument(new FileInputStream("D:\\temp\\test2.docx")).getXWPFDocument();
        List<XWPFParagraph> paragraphList = document.getParagraphs();
        paragraphList.forEach(paragraph -> {
            sb.append(paragraph.getText());
        });
        document.close();
        System.out.println(sb.toString());
    }
}
