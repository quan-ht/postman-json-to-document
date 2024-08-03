package com.tao.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tao
 * <p>
 * 参数说明
 */
@Getter
@Setter
@EqualsAndHashCode
public class Parameter {
	/**
	 * 序号
	 */
	private String index;

	/**
	 * 字段名称
	 */
	private String key;

	/**
	 * 字段描述
	 */
	private String description;

	/**
	 * 字段类型
	 */
	private String type;

	/**
	 * 是否必填
	 */
	private String mandatory;

}
