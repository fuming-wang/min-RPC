package org.study.rpc.Filter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.rpc.Filter.ClientBeforeFilter;
import org.study.rpc.Filter.FilterData;


/**
 * @description: 日志
 */
public class ClientLogFilter implements ClientBeforeFilter {

    private final Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);

    @Override
    public void doFilter(FilterData filterData) {
        logger.info(filterData.toString());
    }
}
