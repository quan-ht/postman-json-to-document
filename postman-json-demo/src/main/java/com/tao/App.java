package com.tao;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.tao.handler.MergeStrategyHandler;
import com.tao.vo.Document;
import com.tao.vo.Header;
import com.tao.vo.Parameter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class App {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) {
		// 输入Json文件路径
		String inFilePath = "C:\\Users\\67327\\Desktop\\test.postman_collection.json";
		String outFilePath = "C:\\Users\\67327\\Desktop\\test.xlsx";
		try {
			// 读取文件
			String jsonString = readFile(inFilePath);

			// 解析成json对象
			JsonNode itemNodes = mapper.readTree(jsonString).get("item");

			// 转换成实体类
			List<Document> documents = convertToDocument(itemNodes);

			// 填充模板
			fillExcel(documents, outFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fillExcel(List<Document> documents, String outFilePath) throws IOException {
		String templateFileName = Objects.requireNonNull(App.class.getClassLoader().getResource("template.xlsx")).getPath();
		byte[] templateBytes = createSheetFromTemplate(templateFileName, documents.size());
		try (ExcelWriter excelWriter = EasyExcel.write(outFilePath).withTemplate(new ByteArrayInputStream(templateBytes)).build()) {
			for (int i = 0; i < documents.size(); i++) {
				Document document = documents.get(i);
				WriteSheet writeSheet = EasyExcel.writerSheet(i)
						.registerWriteHandler(new MergeStrategyHandler())
						//.registerWriteHandler(new CustomCellWriteHeightConfig())
						.build();
				FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
				excelWriter.fill(document, writeSheet);
				excelWriter.fill(new FillWrapper("header", document.getHeaders()), fillConfig, writeSheet);
				excelWriter.fill(new FillWrapper("requestParameters", document.getRequestParameters()), fillConfig, writeSheet);
			}
		}

	}

	private static byte[] createSheetFromTemplate(String templateFileName, int size) throws IOException {
		byte[] bytes = null;
		String tempExcelPath = Objects.requireNonNull(App.class.getClassLoader().getResource("")).getPath() + "temp.xlsx";
		try (XSSFWorkbook workbook = new XSSFWorkbook(templateFileName);
		     FileOutputStream tempOut = new FileOutputStream(tempExcelPath);) {
			// 先将模板Excel复制一份作为临时文件
			workbook.write(tempOut);
		}
		//原模板只有一个sheet，通过poi复制出需要的sheet个数的模板，读取临时文件操作
		try (XSSFWorkbook workbook = new XSSFWorkbook(tempExcelPath);
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			//设置模板的第一个sheet的名称
			workbook.setSheetName(0, "sheet1");
			for (int i = 1; i < size; i++) {
				//复制模板，得到第i个sheet
				workbook.cloneSheet(0, "sheet" + (i + 1));
				//写到流里
				workbook.write(outputStream);
				bytes = outputStream.toByteArray();
			}
		} finally {
			// 最后删除临时文件
			File file = new File(tempExcelPath);
			if (file.exists()) {
				file.delete();
			}
		}
		return bytes;
	}

	private static List<Document> convertToDocument(JsonNode itemNodes) throws IOException {
		List<Document> documents = new ArrayList<>();
		for (JsonNode itemNode : itemNodes) {
			JsonNode requestNode = itemNode.get("request");
			String method = requestNode.get("method").asText();
			if ("GET".equals(method)) {
				continue;
			}
			Document document = new Document();
			// 获取接口名称
			document.setName(itemNode.get("name").asText());
			// 获取请求方法
			document.setMethod(method);
			// 获取接口地址
			JsonNode urlNode = requestNode.get("url");
			document.setUrl(urlNode.get("raw").asText());
			// 获取请求头
			document.setHeaders(mapper.readerForListOf(Header.class).readValue(requestNode.get("header")));
			JsonNode bodyNode = requestNode.get("body");
			String rawStr = bodyNode.get("raw").asText();
			if ("raw".equals(bodyNode.get("mode").asText())) {
				document.setRequestExample(rawStr);
				document.setParameterType(bodyNode.get("mode").asText() + ": " + bodyNode.get("options").get("raw").get("language").asText());
			}
			// 获取参数
			JsonNode rawNodes = mapper.readTree(rawStr);
			List<Parameter> parameters = new ArrayList<>();
			convertToParameter(rawNodes, parameters, "0");
			document.setRequestParameters(parameters);
			documents.add(document);
		}
		return documents;
	}

	private static void convertToParameter(JsonNode rawNodes, List<Parameter> parameters, String index) {
		Iterator<String> fieldNameIterator = rawNodes.fieldNames();
		while (fieldNameIterator.hasNext()) {
			String fieldName = fieldNameIterator.next();
			JsonNode rawNode = rawNodes.get(fieldName);
			int lastIndexOf = index.lastIndexOf('.');
			if (lastIndexOf == -1) {
				index = String.valueOf(Integer.parseInt(index) + 1);
			} else {
				index = index.substring(0, lastIndexOf + 1) + (Integer.parseInt(index.substring(lastIndexOf + 1)) + 1);
			}
			if (rawNode.isValueNode()) {
				buildParameter(parameters, fieldName, rawNode, index);
			} else {
				buildParameter(parameters, fieldName, rawNode, index);
				if (JsonNodeType.ARRAY == rawNode.getNodeType()) {
					// 如果是数组取第一个元素获取字段就可以
					convertToParameter(rawNode.get(0), parameters, index + ".0");
				} else {
					convertToParameter(rawNode, parameters, index + ".0");
				}
			}
		}
	}

	private static void buildParameter(List<Parameter> parameters, String fieldName, JsonNode rawNode, String index) {
		Parameter parameter = new Parameter();
		parameter.setIndex(index);
		parameter.setKey(fieldName);
		parameter.setType(rawNode.getNodeType().toString());
		parameters.add(parameter);
	}


	public static String readFile(String filePath) {
		// 读取文件内容
		try {
			return new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
