package com.alipay.remoting;

/**
 * Remoting address parser
 * <p>
 * Implement this to generate a {@link Url}
 *
 * @author xiaomin.cxm
 * @version $Id: RemotingAddressParser.java, v 0.1 Mar 11, 2016 5:56:55 PM xiaomin.cxm Exp $
 */
public interface RemotingAddressParser {
    /**
     * symbol :
     */
    public static final char COLON = ':';
    /**
     * symbol =
     */
    public static final char EQUAL = '=';
    /**
     * symbol &
     */
    public static final char AND = '&';
    /**
     * symbol ?
     */
    public static final char QUES = '?';

    /**
     * Parse a simple string url to get {@link Url}
     *
     * @param url
     * @return parsed {@link Url}
     */
    Url parse(String url);

    /**
     * Parse a simple string url to get a unique key of a certain address
     *
     * @param url
     * @return
     */
    String parseUniqueKey(String url);

    /**
     * Parse to get property value according to specified property key
     *
     * @param url
     * @param propKey
     * @return propValue
     */
    String parseProperty(String url, String propKey);

    /**
     * Initialize {@link Url} arguments
     *
     * @param url
     */
    void initUrlArgs(Url url);
}
