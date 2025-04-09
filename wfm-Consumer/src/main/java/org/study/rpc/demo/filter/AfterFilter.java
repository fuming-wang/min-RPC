package org.study.rpc.demo.filter;

import org.study.rpc.Filter.ClientAfterFilter;
import org.study.rpc.Filter.FilterData;

/**
 * description:
 * Author: wangfuming
 */
public class AfterFilter implements ClientAfterFilter {

    @Override
    public void doFilter(FilterData filterData) {
        System.out.println("客户端后置处理器启动咯");
    }
}
