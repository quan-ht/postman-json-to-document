package com.tao.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Tao
 * <p>
 * 接口文档
 */
@Getter
@Setter
@EqualsAndHashCode
public class Document {
	/**
	 * 接口名称
	 */
	private String name;

	/**
	 * 接口地址
	 */
	private String url;

	/**
	 * 请求头
	 */
	private List<Header> headers;

	/**
	 * 请求方式
	 */
	private String method;

	/**
	 * 参数类型
	 */
	private String parameterType;

	/**
	 * 请求参数示例
	 */
	private String requestExample;

	/**
	 * 请求参数
	 */
	private List<Parameter> requestParameters;

	/**
	 * 成功响应参数示例
	 */
	private String responseSuccessExample;

	/**
	 * 失败响应参数示例
	 */
	private String responseFailExample;

	/**
	 * 响应参数
	 */
	private List<Parameter> responseParameters;

}
