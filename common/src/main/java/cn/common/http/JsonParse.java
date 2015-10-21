package cn.common.http;

import org.json.JSONException;

import java.io.Serializable;

/**
 * 描述：json解析
 *
 * @author Created by Administrator on 2015/9/4.
 */
public interface JsonParse extends Serializable {
    Object parse(String json);
}