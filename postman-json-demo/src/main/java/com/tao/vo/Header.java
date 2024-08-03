package com.tao.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tao
 * <p>
 * 请求头
 */
@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {
	/**
	 * 请求头
	 */
	private String key;
	/**
	 * 内容
	 */
	private String value;
	/**
	 * 描述
	 */
	private String description;
}
